package io.github.aurynk.audiorelay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.*
import java.io.IOException
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList

class AudioCaptureService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var mediaProjection: MediaProjection? = null
    private var audioRecord: AudioRecord? = null
    private var serverSocket: ServerSocket? = null
    private var isStreaming = false

    // Store connected client output streams safely
    private val clientOutputStreams = CopyOnWriteArrayList<OutputStream>()

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_RESULT_DATA = "EXTRA_RESULT_DATA"
        const val NOTIFICATION_ID = 1002
        const val CHANNEL_ID = "AudioCaptureChannel"
        const val TAG = "AudioCaptureService"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_RESULT_DATA)
                }

                if (resultData != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ServiceCompat.startForeground(
                            this,
                            NOTIFICATION_ID,
                            createNotification(),
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                            else 0
                        )
                    } else {
                        startForeground(NOTIFICATION_ID, createNotification())
                    }

                    val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    val projection = projectionManager.getMediaProjection(android.app.Activity.RESULT_OK, resultData)
                    if (projection != null) {
                         startCapture(projection)
                    } else {
                         Log.e(TAG, "MediaProjection is null")
                         stopSelf()
                    }
                }
            }
            ACTION_STOP -> {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startCapture(projection: MediaProjection) {
        mediaProjection = projection

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val config = AudioPlaybackCaptureConfiguration.Builder(projection)
                .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                .addMatchingUsage(AudioAttributes.USAGE_GAME)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(48000)
                .setChannelMask(AudioFormat.CHANNEL_IN_STEREO)
                .build()

            try {
                audioRecord = AudioRecord.Builder()
                    .setAudioFormat(audioFormat)
                    .setAudioPlaybackCaptureConfig(config)
                    .build()

                audioRecord?.startRecording()
                isStreaming = true

                startStreamingServer()
                startAudioBroadcaster() // Start the dedicated broadcasting coroutine

            } catch (e: SecurityException) {
                Log.e(TAG, "Security Exception starting AudioRecord: ${e.message}")
                stopSelf()
            } catch (e: Exception) {
                Log.e(TAG, "Error starting AudioRecord: ${e.message}")
                stopSelf()
            }
        }

        // Check for permission again if needed, though service should have it.
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
             Log.e(TAG, "Missing RECORD_AUDIO permission")
             stopSelf()
             return
        }
    }

    private fun startStreamingServer() {
        serviceScope.launch {
            try {
                serverSocket = ServerSocket(5000)
                Log.d(TAG, "Server started on port 5000")

                while (isActive && isStreaming) {
                    try {
                        val client = serverSocket?.accept()
                        Log.d(TAG, "Client connected: ${client?.inetAddress}")
                        client?.let {
                             val outputStream = it.getOutputStream()
                             clientOutputStreams.add(outputStream)
                             // We don't block here reading/writing, just add to list
                             // We might need to handle client disconnection individually?
                             // But for simple broadcasting, let the broadcaster handle write errors
                        }
                    } catch (e: IOException) {
                        if (isStreaming) Log.e(TAG, "Error accepting client: ${e.message}")
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error starting server: ${e.message}")
            }
        }
    }

    private fun startAudioBroadcaster() {
        serviceScope.launch {
             val bufferSize = 1024 * 4
             val buffer = ByteArray(bufferSize)

             while (isActive && isStreaming) {
                 val read = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                 if (read > 0) {
                     // Iterate over all connected clients and write the same buffer
                     val iter = clientOutputStreams.iterator()
                     while (iter.hasNext()) {
                         val stream = iter.next()
                         try {
                             stream.write(buffer, 0, read)
                         } catch (e: IOException) {
                             Log.e(TAG, "Client disconnected or write error, removing client")
                             clientOutputStreams.remove(stream)
                             try {
                                 stream.close()
                             } catch (ex: Exception) { /* ignore */ }
                         }
                     }
                 }
             }
        }
    }

    override fun onDestroy() {
        isStreaming = false
        serviceJob.cancel()
        try {
            serverSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        clientOutputStreams.forEach {
            try { it.close() } catch(e: Exception) {}
        }
        clientOutputStreams.clear()

        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        mediaProjection?.stop()
        mediaProjection = null

        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Capture Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Aurynk Audio Capture")
            .setContentText("Capturing and streaming system audio...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
