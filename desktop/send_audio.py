import socket
import ssl
import subprocess


def stream_desktop_audio_tls(host, port, device=None, certfile=None, keyfile=None, cafile=None):
    if device is None:
        device = "default"
    ffmpeg_cmd = [
        "ffmpeg",
        "-f",
        "pulse",
        "-i",
        device,
        "-f",
        "s16le",
        "-acodec",
        "pcm_s16le",
        "-ar",
        "44100",
        "-ac",
        "2",
        "-loglevel",
        "error",
        "-",
    ]
    buffer_size = 1024
    sock = None
    proc = None
    try:
        context = ssl.create_default_context(
            ssl.Purpose.SERVER_AUTH if cafile else ssl.Purpose.CLIENT_AUTH
        )
        if certfile and keyfile:
            context.load_cert_chain(certfile=certfile, keyfile=keyfile)
        if cafile:
            context.load_verify_locations(cafile=cafile)
        context.check_hostname = False
        context.verify_mode = ssl.CERT_NONE if not cafile else ssl.CERT_REQUIRED
        raw_sock = socket.create_connection((host, int(port)))
        sock = context.wrap_socket(raw_sock, server_hostname=host)
        sock.setsockopt(socket.IPPROTO_TCP, socket.TCP_NODELAY, 1)
        proc = subprocess.Popen(ffmpeg_cmd, stdout=subprocess.PIPE, bufsize=0)
        print(f"Streaming desktop audio to {host}:{port} over TLS in real-time ...")
        silence = b"\x00" * buffer_size
        while True:
            data = proc.stdout.read(buffer_size)
            if not data:
                sock.sendall(silence)
                continue
            sock.sendall(data)
    except KeyboardInterrupt:
        print("\nStopped by user. Sending silence for smooth stop...")
        silence = b"\x00" * int(44100 * 2 * 2 * 0.1)
        try:
            sock.sendall(silence)
        except Exception:
            pass
    except Exception as e:
        print(f"\nError: {e}")
    finally:
        if proc:
            try:
                proc.terminate()
                proc.wait(timeout=2)
            except Exception:
                pass
        if sock:
            try:
                sock.shutdown(socket.SHUT_RDWR)
                sock.close()
            except Exception:
                pass
        print("Done streaming.")


def main():
    import argparse

    parser = argparse.ArgumentParser(
        description="Stream desktop audio to Android device over TCP or TLS."
    )
    parser.add_argument("host", help="Destination host (IP or DNS)")
    parser.add_argument("port", type=int, help="Destination port")
    parser.add_argument("--device", help="PulseAudio/PipeWire source device name")
    parser.add_argument(
        "--tls", action="store_true", help="Enable TLS (recommended for production)"
    )
    parser.add_argument("--certfile", help="Path to TLS certificate file (PEM)")
    parser.add_argument("--keyfile", help="Path to TLS private key file (PEM)")
    parser.add_argument("--cafile", help="Path to CA certificate file (PEM)")
    args = parser.parse_args()

    if args.tls:
        stream_desktop_audio_tls(
            args.host, args.port, args.device, args.certfile, args.keyfile, args.cafile
        )
    else:
        stream_desktop_audio(args.host, args.port, args.device)


def stream_desktop_audio(host, port, device=None):
    # Use PulseAudio default monitor if device not specified
    # You can list devices with: pactl list short sources
    if device is None:
        device = "default"
        # For PulseAudio, use 'default' or e.g. 'alsa_output.pci-0000_00_1b.0.analog-stereo.monitor'
        # For PipeWire, similar monitor names apply

    ffmpeg_cmd = [
        "ffmpeg",
        "-f",
        "pulse",
        "-i",
        device,
        "-f",
        "s16le",
        "-acodec",
        "pcm_s16le",
        "-ar",
        "44100",
        "-ac",
        "2",
        "-loglevel",
        "error",
        "-",
    ]

    buffer_size = 1024

    sock = None
    proc = None
    try:
        sock = socket.create_connection((host, int(port)))
        sock.setsockopt(socket.IPPROTO_TCP, socket.TCP_NODELAY, 1)
        proc = subprocess.Popen(ffmpeg_cmd, stdout=subprocess.PIPE, bufsize=0)
        print(f"Streaming desktop audio to {host}:{port} in real-time ...")
        silence = b"\x00" * buffer_size
        while True:
            data = proc.stdout.read(buffer_size)
            if not data:
                # Instead of breaking, send silence to keep the stream alive
                sock.sendall(silence)
                continue
            sock.sendall(data)
    except KeyboardInterrupt:
        print("\nStopped by user. Sending silence for smooth stop...")
        # Send 0.5 seconds of silence (44100 samples/sec * 2 channels * 2 bytes/sample * 0.5)
        silence = b"\x00" * int(44100 * 2 * 2 * 0.1)
        try:
            sock.sendall(silence)
        except Exception:
            pass
    except Exception as e:
        print(f"\nError: {e}")
    finally:
        if proc:
            try:
                proc.terminate()
                proc.wait(timeout=2)
            except Exception:
                pass
        if sock:
            try:
                sock.shutdown(socket.SHUT_RDWR)
                sock.close()
            except Exception:
                pass
        print("Done streaming.")


if __name__ == "__main__":
    main()
