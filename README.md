# 📺 RTSP Player (Android)

An Android application for streaming and controlling RTSP feeds using **LibVLC**.  
This project demonstrates how to build a custom video player with **custom overlay controls**, **volume management**, **fullscreen toggle**, and **audio focus handling**.

---

## ✨ Features

- 🎥 **RTSP Playback** – play live RTSP streams with low latency
- 🎛️ **Custom Overlay Controls** – play, pause, fullscreen toggle, and volume slider
- 🔊 **Audio Focus Handling** – integrates with Android’s audio system for smooth playback
- 📱 **Fullscreen Mode** – immersive video playback with system bars hidden
- 🔄 **Rotation Support** – preserves aspect ratio when rotating device, no stretching
- ⏱️ **Auto‑Hide Controls** – overlay disappears after a timeout and reappears on tap
- ⚡ **Optimized VLC Options** – network caching, hardware decoding, and audio resampling
- 🐞 **Lifecycle Management** – proper cleanup of VLC resources

---

## 📂 Project Structure

```plaintext
RTSP_Player/
 ├── app/
 │   ├── java/com/example/rtsp_player/
 │   │    └── MainActivity.kt        # Core player logic + custom controllers
 │   ├── res/layout/
 │   │    ├── activity_main.xml      # Root layout with video surface
 │   │    └── custom_controllers.xml # Overlay UI with play/pause, volume, fullscreen
 │   ├── res/values/
 │   │    └── strings.xml            # Contains RTSP URL and app strings
 │   └── AndroidManifest.xml
 ├── build.gradle
 ├── settings.gradle
 └── README.md

---

🛠️ Tech Stack
Language: Kotlin

Libraries:

  LibVLC – RTSP playback
  AndroidX libraries (AppCompat, Material Components, ViewBinding)

---

## 🖥️ Usage

  Tap the Play button ▶️ to start streaming.
  Tap Pause ⏸️ to pause playback.
  Use the Volume button 🔊 to show/hide the slider and adjust system volume.
  Tap the Fullscreen button ⛶ to toggle immersive mode.
  Tap on the video surface to show/hide the custom overlay controls.

---

## 🚀 Installation
Clone the repository:

bash
git clone https://github.com/your-username/RTSP_Player.git
Open the project in Android Studio.

Sync Gradle dependencies.

Add your RTSP URL in res/values/strings.xml:
  xml
  <string name="media_url_rtsp">rtsp://username:password@ip_address:port/stream</string>

Run the app on a physical device (recommended).
