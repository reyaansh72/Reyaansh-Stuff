# 🚀 Reyaansh's Stuff

### One app. Lots of stuff. And there's always more coming!

[![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)](https://www.android.com/)
[![Java](https://img.shields.io/badge/Language-Java-orange?logo=openjdk)](https://www.java.com/)
[![Gradle](https://img.shields.io/badge/Build-Gradle-blue?logo=gradle)](https://gradle.org/)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase)](https://firebase.google.com/)

---

## 👋 What is Reyaansh's Stuff?

Ever wished you could have a bunch of different things in one app instead of installing 50 different apps?

Well, that's basically the idea behind **Reyaansh's Stuff**!

It's an Android application where different ideas, tools, experiments, and experiences come together. It isn't limited to one category or a particular purpose. It's a project that keeps growing as new ideas come along.

And here's the important part:

**This is a LONG-TERM project.**

Not a random weekend experiment. Not an app that gets two updates and disappears.

The goal is to keep building, improving, experimenting, fixing things, and adding new stuff over multiple releases. There's no fixed list of features that defines what this app can become.

Basically, there's always something more to build. 🚀

## 🎯 The Idea

Reyaansh's Stuff is built around a simple idea: keep different experiences together and make the application expandable.

Some of the broad areas it explores include:

- 🌐 Social and communication experiences
- 🤖 AI integrations and conversational interfaces
- 🛠️ Productivity and everyday utilities
- 🎮 Entertainment and interactive experiences
- 🎨 Customization and personalization

These aren't the limits of the project. They're just some of the directions it explores.

New ideas can become part of the app whenever development moves in that direction!

## 📲 Want to Use the App?

You don't need Android Studio, Java, Gradle, or any developer setup to use Reyaansh's Stuff.

Here's how to get started:

1. Open the [Releases page](https://github.com/reyaansh72/Reyaansh-Stuff/releases).
2. Download the latest available app release.
3. Install the APK on your Android device.
4. Open Reyaansh's Stuff.
5. Tap the **Sign Up** button.
6. Create your account and get started!

Already have an account? Just tap **Login** and sign in.

That's it. Have fun exploring!

## 🧑‍💻 Want to Develop It?

Want to experiment with the code, modify things, or build your own version?

You're welcome to explore the project!

### Requirements

- Android Studio or another compatible Android development environment
- Android SDK 36
- JDK 17 or newer
- Gradle Wrapper (included in the repository)
- Internet connection for online services

**Android versions:**
- Minimum: Android 7.1 (API 25)
- Target: Android 15 (API 35)

### 1. Clone the repository

```bash
git clone https://github.com/reyaansh72/Reyaansh-Stuff.git
cd Reyaansh-Stuff
```

### 2. Check Java

```bash
java -version
```

The project uses Android Gradle Plugin 8.9.1 and Java 17 source compatibility.

**Linux users:** The current `gradle.properties` contains machine-specific Windows Java paths. Remove or update `org.gradle.java.home` and `org.gradle.java.installations.paths` before building on Linux.

### 3. Build the APK

**Linux / macOS**

```bash
chmod +x gradlew
./gradlew assembleDebug
```

**Windows**

```bat
gradlew.bat assembleDebug
```

You can also use the included Windows helper:

```bat
build.bat
```

The helper script expects a configured Eclipse Adoptium JDK 21 installation.

### 4. Find your APK

After the build finishes:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Install it using ADB:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

Congratulations, you've built your own copy!

## 🔥 Firebase Configuration

The project uses Firebase Authentication and Firebase Realtime Database.

The repository includes a default Firebase configuration:

```text
app/google-services.json
```

### For normal users

Just download the app from Releases and sign up or log in through the application.

The default Firebase setup is configured to work with users authenticated through the app.

### For developers

The default Firebase configuration is intended for authenticated app users. If you're developing, testing, modifying authentication, or working on your own version, you'll need to configure your own Firebase project.

Here's what to do:

1. Create a Firebase project.
2. Register your own Android application in Firebase.
3. Enable Email/Password authentication.
4. Configure Firebase Realtime Database.
5. Download your own `google-services.json`.
6. Replace the existing file in `app/`.
7. Configure your database security rules for your development environment.

**Important:** Don't make your database publicly writable just to make development easier. Use appropriate authentication and security rules.

## 🤖 External Integrations

Some parts of the application communicate with external services.

### Ollama

AI-related functionality can connect to an Ollama server.

For Android emulators, the usual host address is:

```text
http://10.0.2.2:11434
```

For physical devices, configure the address of an Ollama server reachable over your local network.

### Open-Meteo

Weather-related functionality uses Open-Meteo for weather information and geocoding.

## 🧰 Tech Stack

A quick look at what's powering the project:

| Technology | What it's used for |
|---|---|
| Java | Main application logic |
| Android SDK | Native Android development |
| XML | UI layouts |
| Material Components | UI components |
| AndroidX | Android libraries |
| Firebase Authentication | User authentication |
| Firebase Realtime Database | Application data |
| AndroidX Media3 | Media playback |
| Ollama API | AI integration |
| Open-Meteo | Weather services |
| Gradle | Build system |

## 📁 Project Structure

```text
Reyaansh-Stuff/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       ├── res/
│   │       └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── google-services.json
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── build.bat
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle.kts
```

## 📌 Project Information

| Property | Value |
|---|---|
| Application | Reyaansh's Stuff |
| Package ID | `com.reyaansh72.reyaanshsstuff` |
| Version | 1.0 |
| Version Code | 1 |
| Minimum SDK | 25 |
| Target SDK | 35 |
| Compile SDK | 36 |
| Main Language | Java |
| Build System | Gradle |
| Project Type | Long-term Android project |

## 🔮 What's Next?

Who knows? That's part of the fun!

The project is meant to keep evolving. Future releases can bring new ideas, completely new sections, better integrations, redesigned interfaces, performance improvements, and plenty of experiments.

There is no permanently fixed feature list and no finish line.

**The plan is simple: keep building.**

## 🤝 Contributing

Found something broken? Have an interesting idea? Want to improve something?

Contributions and suggestions are welcome!

1. Fork the repository.
2. Create a branch for your changes.
3. Make your modifications.
4. Test your changes.
5. Submit a pull request.

Even small improvements are appreciated!

## 📜 License

Reyaansh's Stuff is licensed under the **MIT License**.

You're free to use, copy, modify, merge, publish, distribute, sublicense, and even sell copies of the software, subject to the license conditions.

Please read the [`LICENSE`](LICENSE) file in this repository for the complete license text and terms.

**Copyright © 2026 reyaansh72.**

## 👨‍💻 Developer

Made and maintained by **Reyaansh (@reyaansh72)**.

- GitHub: [@reyaansh72](https://github.com/reyaansh72)
- Repository: [Reyaansh's Stuff](https://github.com/reyaansh72/Reyaansh-Stuff)
- Releases: [Download the app](https://github.com/reyaansh72/Reyaansh-Stuff/releases)

---

<p align="center">

# 🎉 Reyaansh's Stuff

### Lots of stuff. One app. Always evolving.

**Thanks for checking out the project!**

</p>
