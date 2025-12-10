use cpal::traits::{DeviceTrait, HostTrait, StreamTrait};
use lazy_static::lazy_static;
use std::net::UdpSocket;
use std::sync::{Arc, Mutex};

uniffi::setup_scaffolding!();

// Wrapper around cpal::Stream to allow sending it between threads.
struct SendStream(cpal::Stream);
unsafe impl Send for SendStream {}
unsafe impl Sync for SendStream {}

struct AudioState {
    stream: Option<SendStream>,
    is_running: bool,
}

lazy_static! {
    static ref AUDIO_STATE: Arc<Mutex<AudioState>> = Arc::new(Mutex::new(AudioState {
        stream: None,
        is_running: false,
    }));
}

#[uniffi::export]
pub fn start_stream(target_ip: String) {
    let mut state = AUDIO_STATE.lock().unwrap();
    if state.is_running {
        println!("Stream already running");
        return;
    }

    println!("Starting stream to {}", target_ip);

    let socket = Arc::new(UdpSocket::bind("0.0.0.0:0").expect("couldn't bind to address"));
    socket.set_broadcast(true).expect("failed to enable broadcast");

    let target_addr = if target_ip.contains(":") {
        target_ip
    } else {
        format!("{}:5000", target_ip)
    };

    if let Err(e) = socket.connect(&target_addr) {
         eprintln!("Failed to connect UDP socket: {}", e);
         return;
    }
    println!("UDP socket connected to {}", target_addr);

    let host = cpal::default_host();
    let device = match find_input_device(&host) {
        Some(d) => d,
        None => {
            eprintln!("No suitable audio input device found!");
            return;
        }
    };
    println!("Using audio device: {}", device.name().unwrap_or("Unknown".to_string()));

    let config = match device.default_input_config() {
        Ok(c) => c,
        Err(e) => {
            eprintln!("Failed to get default input config: {}", e);
            return;
        }
    };

    let err_fn = move |err| {
        eprintln!("an error occurred on stream: {}", err);
    };

    let socket_clone = socket.clone();

    let stream = match config.sample_format() {
        cpal::SampleFormat::F32 => device.build_input_stream(
            &config.into(),
            move |data: &[f32], _: &_| write_audio_data(data, &socket_clone),
            err_fn,
            None,
        ),
        cpal::SampleFormat::I16 => device.build_input_stream(
            &config.into(),
            move |data: &[i16], _: &_| write_audio_data(data, &socket_clone),
            err_fn,
            None,
        ),
        cpal::SampleFormat::U16 => device.build_input_stream(
            &config.into(),
            move |data: &[u16], _: &_| write_audio_data(data, &socket_clone),
            err_fn,
            None,
        ),
        _ => return,
    };

    match stream {
        Ok(s) => {
            s.play().unwrap();
            state.stream = Some(SendStream(s));
            state.is_running = true;
            println!("Stream started successfully");
        }
        Err(e) => {
            eprintln!("Failed to build input stream: {}", e);
        }
    }
}

#[uniffi::export]
pub fn stop_stream() {
    let mut state = AUDIO_STATE.lock().unwrap();
    if state.is_running {
        println!("Stopping stream");
        state.stream = None;
        state.is_running = false;
    }
}

fn find_input_device(host: &cpal::Host) -> Option<cpal::Device> {
    let devices = host.input_devices().ok()?;
    for device in devices {
        let name = device.name().unwrap_or_default().to_lowercase();
        if name.contains("monitor") || name.contains("analog stereo") {
             return Some(device);
        }
    }
    host.default_input_device()
}

fn write_audio_data<T>(input: &[T], socket: &UdpSocket)
where
    T: cpal::Sample,
{
    let size = std::mem::size_of::<T>();
    let byte_len = input.len() * size;
    let bytes = unsafe {
        std::slice::from_raw_parts(input.as_ptr() as *const u8, byte_len)
    };

    const CHUNK_SIZE: usize = 1400;
    for chunk in bytes.chunks(CHUNK_SIZE) {
        if let Err(_e) = socket.send(chunk) {
            // Ignore errors
        }
    }
}
