# Speech to Text - Cross-Platform Voice Recognition App

<div align="center">

[![Kotlin](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF?logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-4285F4?logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Platform](https://img.shields.io/badge/Platform-iOS%20%7C%20Android-lightgrey)]()
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A modern, cross-platform voice recognition application built with Compose Multiplatform, delivering a seamless speech-to-text experience on both iOS and Android platforms.

[Features](#-features) • [Demo](#-demo) • [Architecture](#-architecture) • [Installation](#-installation) • [Usage](#-usage) • [Contributing](#-contributing)

</div>

## ✨ Features

### Core Features
- 📱 Single codebase for iOS and Android platforms
- 🎙️ Real-time speech recognition with high accuracy
- 🌍 Support for 40+ languages worldwide
- 📋 One-tap text copying to clipboard
- 🎨 Modern, intuitive user interface
- 🌓 Light/Dark theme support
- 🔒 Secure microphone permission handling
- ⚡ High-performance native implementation

### Platform-Specific Features
- 🤖 **Android**
    - Seamless integration with native Speech Recognition API
    - Background audio processing
    - Runtime permission management
    - Optimized for various Android versions

- 🍎 **iOS**
    - Native Speech Framework integration
    - Real-time audio buffer processing with AVAudioEngine
    - High-quality voice recognition
    - Optimized for iOS devices

## 🛠️ Technology Stack

### Core Technologies
- **Kotlin Multiplatform Mobile (KMM)**
    - Share business logic between platforms
    - Platform-specific implementations where needed
    - Efficient code sharing strategy

- **Jetpack Compose Multiplatform**
    - Modern declarative UI
    - Consistent design across platforms
    - Rich component library
    - Custom composables for specific needs

- **Dependency Injection**
    - Koin for dependency management
    - Clean architecture support
    - Easy testing capabilities

### Platform Integration
- **Android Implementation**
    - `SpeechRecognizer` API for native voice recognition
    - Android Jetpack libraries
    - Material Design 3 components
    - Lifecycle-aware components

- **iOS Implementation**
    - `Speech Framework` for native voice processing
    - `AVFoundation` for audio handling
    - SwiftUI interoperability
    - Native iOS UI components

## 🏗️ Architecture

The project follows Clean Architecture principles with MVVM pattern:

```
├── commonMain
│   ├── data
│   │   ├── models
│   ├── presentation
│   │   ├── viewmodel
│   │   └── components
│   ├── platform 
│   ├── di
│   │   └── modules
├── androidMain
│   └── platform
└── iosMain
    └── platform
```

## 🚀 Installation

### Prerequisites
- Android Studio Arctic Fox or later
- Xcode 13 or later
- JDK 11 or later
- Kotlin Multiplatform Mobile plugin

### Setup Steps

1. Clone the repository:
```bash
git clone https://github.com/[username]/speech-to-text-cmp.git
cd speech-to-text-cmp
```

2. Configure platform-specific settings:

#### Android Setup
```bash
# Build Android app
./gradlew :composeApp:assembleDebug

# Run Android app
./gradlew :composeApp:installDebug
```

#### iOS Setup
```bash
# Install CocoaPods dependencies
cd iosApp
pod install
# Open Xcode workspace and run
```

## 📱 Usage Guide

1. **Initial Setup**
    - Launch the application
    - Grant microphone permissions when prompted
    - Select your preferred language from 40+ options

2. **Voice Recognition**
    - Tap the microphone button to start recording
    - Speak clearly into your device
    - Watch as your speech is converted to text in real-time
    - Tap again to stop recording

3. **Text Management**
    - Review the converted text
    - Copy text to clipboard with one tap
    - Start a new recording session anytime

4. **Customization**
    - Support light/dark themes
    - Change recognition language
    - Adjust UI preferences

## 🤝 Contributing

We welcome contributions! Here's how you can help:

1. Fork the repository
2. Create your feature branch:
```bash
git checkout -b feature/amazing-feature
```
3. Commit your changes:
```bash
git commit -m 'feat: Add amazing feature'
```
4. Push to the branch:
```bash
git push origin feature/amazing-feature
```
5. Open a Pull Request

### Development Guidelines
- Follow Kotlin coding conventions
- Write unit tests for new features
- Update documentation as needed
- Ensure cross-platform compatibility

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Thanks to the Compose Multiplatform team

---

## 🎥 Demo

Watch our application in action on both platforms! These demos showcase the key features including real-time speech recognition, multiple language support, and the seamless user interface.

<div align="center">
  <table>
    <tr>
      <td align="center" width="50%">
        <strong>Android Demo</strong><br/>
        <video src="demo/android-demo.mp4" width="270" height="600" controls/>
        <br/>
        <em>Android demo showing real-time speech recognition and material design UI</em>
      </td>
      <td align="center" width="50%">
        <strong>iOS Demo</strong><br/>
        <video src="demo/ios-demo.mp4" width="270" height="600" controls/>
        <br/>
        <em>iOS demo featuring native speech recognition and SwiftUI integration</em>
      </td>
    </tr>
  </table>
</div>
