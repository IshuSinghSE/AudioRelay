# Aurynk Desktop Audio Sender

This utility streams your Linux desktop audio to the Aurynk Android app in real time, with low latency and optional TLS encryption.

## Features
- Streams system audio to your Android device
- Low latency (raw PCM over TCP or TLS)
- Secure (TLS) or plain TCP modes
- Works with PulseAudio/PipeWire

## Requirements
- Python 3.7+
- ffmpeg (must be in your PATH)
- Linux with PulseAudio or PipeWire

## Usage

### 1. Install requirements
- Python: Already installed on most Linux systems
- ffmpeg: `sudo apt install ffmpeg` (Debian/Ubuntu)

### 2. Generate TLS certificates (recommended for production)

#### a. Generate a CA (self-signed, for testing):
```sh
openssl genrsa -out ca.key 4096
openssl req -x509 -new -nodes -key ca.key -sha256 -days 3650 -out ca.pem -subj "/CN=MyTestCA"
```

#### b. Generate a server key and certificate signing request (CSR):
```sh
openssl genrsa -out key.pem 4096
openssl req -new -key key.pem -out server.csr -subj "/CN=YOUR_ANDROID_DEVICE_IP"
```

#### c. Sign the server certificate with your CA:
```sh
openssl x509 -req -in server.csr -CA ca.pem -CAkey ca.key -CAcreateserial -out cert.pem -days 365 -sha256
```

#### d. (Android only) Convert to PKCS12 for the app:
```sh
openssl pkcs12 -export -in cert.pem -inkey key.pem -out server.p12 -name audiorelay -CAfile ca.pem -caname root
```
- Place `server.p12` in your Android app's `assets/` directory.

### 3. Run the sender script

#### Secure (TLS, recommended):
```sh
python3 send_audio.py <android_ip> <port> --tls --certfile cert.pem --keyfile key.pem --cafile ca.pem --device <pulse_device>
```

#### Plain TCP (dev/advanced):
```sh
python3 send_audio.py <android_ip> <port> --device <pulse_device>
```

- `<android_ip>`: IP address of your Android device
- `<port>`: Port (default: 5000)
- `<pulse_device>`: PulseAudio/PipeWire monitor (see below)

### 4. Find your PulseAudio/PipeWire device
List available sources:
```sh
pactl list short sources
```
Use the monitor of your output device (e.g., `alsa_output.pci-0000_00_1b.0.analog-stereo.monitor`).

## Troubleshooting
- Make sure your Android device and PC are on the same WiFi network.
- The Android app must be running and show a foreground notification.
- If using TLS, ensure certs and passwords match on both sides.
- Use `adb logcat | grep AudioRelay` for Android logs.

## License
MIT
