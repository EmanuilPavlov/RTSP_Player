# 📱 RTSP Player (Android)

An Android application for streaming and controlling **RTSP feeds** using **LibVLC**.  
This project demonstrates how to build a **custom video player** with overlay controls, volume management, fullscreen toggle, and proper audio focus handling.

---

## ✨ Features

- 🎥 **RTSP Playback** – Play live RTSP streams with low latency  
- 🎛️ **Custom Overlay Controls** – Play, pause, fullscreen toggle, and volume slider  
- 🔊 **Audio Focus Handling** – Integrates with Android’s audio system for smooth playback  
- 📱 **Fullscreen Mode** – Immersive video playback with system bars hidden  
- 🔄 **Rotation Support** – Preserves aspect ratio when rotating (no stretching)  
- ⏱️ **Auto-Hide Controls** – Overlay disappears after a timeout and reappears on tap  
- ⚡ **Optimized VLC Options** – Network caching, hardware decoding, and audio resampling  
- 🐞 **Lifecycle Management** – Proper cleanup of VLC resources  

---

## 📂 Project Structure

\`\`\`
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
\`\`\`

---

## 🛠️ Tech Stack

**Language:**  
- Kotlin  

**Libraries:**  
- [LibVLC](https://wiki.videolan.org/LibVLC/) – RTSP playback  
- AndroidX libraries (AppCompat, Material Components, ViewBinding)

---

## 🖥️ Usage

- ▶️ **Play** – Tap *Play* to start streaming  
- ⏸️ **Pause** – Tap *Pause* to pause playback  
- 🔊 **Volume** – Tap the volume button to show/hide the slider and adjust system volume  
- ⛶ **Fullscreen** – Tap to toggle immersive mode  
- 👆 **Overlay Controls** – Tap on the video surface to show/hide overlay controls  

---

## 🚀 Installation

1. **Clone the repository:**
   \`\`\`bash
   git clone https://github.com/your-username/RTSP_Player.git
   \`\`\`

2. **Open** the project in **Android Studio**  
3. **Sync Gradle dependencies**  
4. **Add your RTSP URL** in \`res/values/strings.xml\`:
   \`\`\`xml
   <string name="media_url_rtsp">rtsp://username:password@ip_address:port/stream</string>
   \`\`\`
5. **Run the app** on a physical Android device *(recommended)*  

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).
