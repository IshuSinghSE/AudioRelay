import logging
import socket
import ssl
import subprocess
import select
import time
from typing import Optional, List, Tuple


def create_ssl_context(
    cafile: Optional[str] = None,
    certfile: Optional[str] = None,
    keyfile: Optional[str] = None,
    verify: bool = True,
):
    """
    Create and return a configured SSLContext for use by AudioSender.

    - Enforces TLS 1.2+ when supported (sets minimum_version where available)
    - Disables TLSv1 and TLSv1.1 via context.options
    - Loads cert/key and cafile when provided
    - If verify=True, sets CERT_REQUIRED and enables hostname checking
      (uses system CAs when cafile is None). If verify=False, sets
      CERT_NONE (insecure) for compatibility/testing.
    """
    context = ssl.create_default_context(
        ssl.Purpose.SERVER_AUTH if cafile else ssl.Purpose.CLIENT_AUTH
    )
    # Prefer setting a minimum version (Python 3.7+). Prefer TLSv1.3 when
    # available, otherwise fall back to TLSv1.2. Only use the deprecated
    # OP_NO_TLS* flags when `minimum_version` is not supported.
    if hasattr(ssl, "TLSVersion") and hasattr(context, "minimum_version"):
        try:
            if hasattr(ssl.TLSVersion, "TLSv1_3"):
                context.minimum_version = ssl.TLSVersion.TLSv1_3
            else:
                context.minimum_version = ssl.TLSVersion.TLSv1_2
        except Exception:
            # If setting minimum_version fails, fall back to disabling older
            # TLS versions via options for compatibility.
            context.options |= ssl.OP_NO_TLSv1 | ssl.OP_NO_TLSv1_1
    else:
        # Older Python/OpenSSL: disable TLSv1 and TLSv1.1 using options
        context.options |= ssl.OP_NO_TLSv1 | ssl.OP_NO_TLSv1_1

    if certfile and keyfile:
        context.load_cert_chain(certfile=certfile, keyfile=keyfile)

    if verify:
        if cafile:
            context.load_verify_locations(cafile=cafile)
        context.verify_mode = ssl.CERT_REQUIRED
        context.check_hostname = True
    else:
        if cafile:
            context.load_verify_locations(cafile=cafile)
        # When disabling verification we must turn off hostname checking
        # before setting verify_mode to CERT_NONE; OpenSSL/Python enforces
        # that check_hostname is false when verify_mode is CERT_NONE.
        context.check_hostname = False
        context.verify_mode = ssl.CERT_NONE
    return context


def stream_desktop_audio_tls(
    host, port, device=None, certfile=None, keyfile=None, cafile=None, verify=True
):
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
        # Use the shared helper to create an SSLContext with TLS1.2+ enforced
        context = create_ssl_context(
            cafile=cafile, certfile=certfile, keyfile=keyfile, verify=verify
        )
        if not verify:
            # Warn at runtime when verification is disabled
            logging.getLogger(__name__).warning(
                "TLS verification is disabled (--no-verify). This is insecure and should only be used for testing or on trusted networks."
            )
        # Debug: resolve host and show addrinfo so we know what IP will be used
        try:
            addrs = socket.getaddrinfo(host, port, proto=socket.IPPROTO_TCP)
            print(f"Resolved {host}:{port} -> {addrs}")
        except Exception as e:
            print(f"Warning: getaddrinfo failed for {host}:{port}: {e}")

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
        # If it's a routing error, give a hint to the user
        if isinstance(e, OSError) and getattr(e, "errno", None) == 113:
            print(
                "Hint: errno 113 = No route to host. Check that the host is reachable, correct IP is used, and both devices are on the same network/subnet."
            )
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
    parser.add_argument("host", help="Destination host (IP or DNS) or use 'discover' with --discover")
    parser.add_argument("port", type=int, nargs="?", help="Destination port")
    parser.add_argument("--discover", action="store_true", help="Discover receiver devices on the local network and select one interactively")
    # Allow passing the capture device either as an optional flag or as
    # an optional positional argument so both styles work:
    parser.add_argument("--device", help="PulseAudio/PipeWire source device name")
    parser.add_argument(
        "device_pos",
        nargs="?",
        help="(optional) PulseAudio/PipeWire source device name (positional)",
    )
    parser.add_argument(
        "--tls", action="store_true", help="Enable TLS (recommended for production)"
    )
    parser.add_argument("--certfile", help="Path to TLS certificate file (PEM)")
    parser.add_argument("--keyfile", help="Path to TLS private key file (PEM)")
    parser.add_argument("--cafile", help="Path to CA certificate file (PEM)")
    parser.add_argument(
        "--no-verify",
        action="store_true",
        help="Disable certificate verification (insecure, use only for testing)",
    )
    args = parser.parse_args()

    # Prefer explicit --device; fall back to positional device if provided.
    device_arg = args.device if getattr(args, "device", None) else getattr(args, "device_pos", None)

    if args.discover:
        # Perform network discovery to find available receivers
        devices = discover_receivers(timeout=3)
        if not devices:
            print("No receivers found via discovery. Try providing host manually.")
            return
        print("Discovered receivers:")
        for i, (addr, port, name) in enumerate(devices):
            print(f"[{i}] {name} - {addr}:{port}")
        choice = 0
        try:
            choice_input = input("Select device index (default 0): ")
            if choice_input.strip() != "":
                choice = int(choice_input.strip())
        except Exception:
            choice = 0
        host, port, _ = devices[choice]
        if args.tls:
            stream_desktop_audio_tls(
                host,
                port,
                device_arg,
                args.certfile,
                args.keyfile,
                args.cafile,
                verify=not args.no_verify,
            )
        else:
            stream_desktop_audio(host, port, device_arg)
        return

    if args.tls:
        stream_desktop_audio_tls(
            args.host,
            args.port,
            device_arg,
            args.certfile,
            args.keyfile,
            args.cafile,
            verify=not args.no_verify,
        )
    else:
        stream_desktop_audio(args.host, args.port, device_arg)


def discover_receivers(timeout: int = 3, discovery_port: int = 5002) -> List[Tuple[str, int, str]]:
    """
    Broadcast a discovery packet and collect responses for `timeout` seconds.
    Returns a list of (ip, port, name).
    """
    msg = b"AURYNK_DISCOVER"
    responses: List[Tuple[str, int, str]] = []
    # Create UDP socket for broadcast
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as sock:
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
        sock.settimeout(0.5)
        try:
            # Send to global broadcast
            sock.sendto(msg, ("255.255.255.255", discovery_port))
        except Exception:
            pass
        # Also try subnet-directed broadcast on common local addresses
        try:
            sock.sendto(msg, ("<broadcast>", discovery_port))
        except Exception:
            pass

        # Listen for replies for `timeout` seconds
        end = time.time() + timeout
        while time.time() < end:
            try:
                ready = select.select([sock], [], [], max(0, end - time.time()))
                if ready[0]:
                    data, addr = sock.recvfrom(1024)
                    text = data.decode(errors="ignore").strip()
                    # Expect format: AURYNK_RESPONSE;5000;Aurynk
                    if text.startswith("AURYNK_RESPONSE"):
                        parts = text.split(";")
                        if len(parts) >= 3:
                            try:
                                resp_port = int(parts[1])
                            except Exception:
                                resp_port = 5000
                            name = parts[2]
                            ip = addr[0]
                            if (ip, resp_port, name) not in responses:
                                responses.append((ip, resp_port, name))
            except socket.timeout:
                continue
            except Exception:
                continue
    return responses


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
        try:
            addrs = socket.getaddrinfo(host, port, proto=socket.IPPROTO_TCP)
            print(f"Resolved {host}:{port} -> {addrs}")
        except Exception as e:
            print(f"Warning: getaddrinfo failed for {host}:{port}: {e}")

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
