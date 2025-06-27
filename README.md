# MediAdminApp

MediAdminApp is a modern Android application designed to provide an administrative dashboard for healthcare or pharmacy-related domains. Built with Jetpack Compose, Hilt, Retrofit, and leveraging modern Android architectural patterns, MediAdminApp enables administrators to efficiently manage users (doctors, patients, staff) and products (medicines, medical equipment, etc.) in a streamlined, reactive, and secure manner.

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Tech Stack & Dependencies](#tech-stack--dependencies)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

---

## Features

- **User Management:** View, approve, update, or delete users (doctors, patients, etc.).
- **Product Management:** Add, update, delete, and view medical products.
- **Approval Flows:** Approve or block users with real-time feedback.
- **Stateful UI:** Uses Kotlin StateFlow for reactive UI updates.
- **Navigation:** Jetpack Compose Navigation for seamless screen transitions.
- **Theme Support:** Material 3 theming with support for dark/light modes.
- **Error Handling:** Robust error and loading state management.

---

## Architecture

MediAdminApp is built using the MVVM (Model-View-ViewModel) pattern combined with modern Android best practices:

- **ViewModel:** Handles UI-related data using `StateFlow` and exposes state for Compose UI.
- **Repository:** Abstracts API interactions, providing suspend functions for network operations and managing loading/error/success states.
- **Network Layer:** Uses Retrofit for type-safe HTTP client and OkHttp for network logging/interception.
- **Dependency Injection:** Hilt is used for easy and scalable dependency injection.
- **Composable UI:** All screens are implemented as Composable functions using Jetpack Compose.

---

## Project Structure

```
Admin_Medi_App/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/saurabh/mediadminapp/
│   │   │   │   ├── repository/          # Repository pattern for data layer
│   │   │   │   ├── network/             # API services and providers
│   │   │   │   ├── ui/screens/          # Jetpack Compose UI screens
│   │   │   │   ├── ui/theme/            # Material 3 theme definitions
│   │   │   │   └── MyViewModel.kt       # Central ViewModel
│   │   │   └── res/
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## Tech Stack & Dependencies

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Dependency Injection:** Hilt
- **Networking:** Retrofit, OkHttp, Gson converter
- **Architecture:** MVVM, StateFlow, Coroutines
- **Other:** Navigation Compose, Kotlin Serialization

**Key Gradle Dependencies:**
- `androidx.compose:compose-bom`
- `androidx.lifecycle:lifecycle-runtime-ktx`
- `com.squareup.retrofit2:retrofit`
- `com.squareup.okhttp3:logging-interceptor`
- `com.google.dagger:hilt-android`
- `org.jetbrains.kotlinx:kotlinx-serialization-json`
- `androidx.navigation:navigation-compose`

---

## Getting Started

### Prerequisites

- Android Studio Flamingo or later
- JDK 11+
- Gradle 8.x+
- Android SDK 31+

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/It-Saurabh001/Admin_Medi_App.git
   cd Admin_Medi_App
   ```

2. **Open in Android Studio:**  
   File > Open > Select the `Admin_Medi_App` folder.

3. **Configure API Endpoint:**  
   Update the `BASE_URL` in `app/src/main/java/com/saurabh/mediadminapp/utils/BASE_URL.kt` to point to your backend server.

4. **Build and Run:**  
   Click `Run` or use:
   ```bash
   ./gradlew assembleDebug
   ```

---

## Usage

- **Login** as an admin user.
- Navigate between Dashboard, User Management, and Product Management screens.
- Approve or block new users.
- Add, update, or delete products.
- View application usage analytics (if implemented).

---

## Example Code

**Repository Pattern (Kotlin Flow):**
```kotlin
suspend fun getAllUsers(): Flow<ResultState<GetAllUserResponse>> = flow {
    emit(ResultState.Loading)
    try {
        val response = apiServices.getAllUser()
        if (response.isSuccessful && response.body() != null){
            emit(ResultState.Success(response.body()!!))
        } else {
            emit(ResultState.Error(Exception(response.errorBody()?.string())))
        }
    } catch (e: Exception) {
        emit(ResultState.Error(e))
    }
}
```

**Composable Screen Example:**
```kotlin
@Composable
fun HistoryScreen(){
    Scaffold {
        innerPadding ->
        Text(text = "History Screen", modifier = Modifier.padding(innerPadding))
    }
}
```

---

## Contributing

Contributions, feature requests, and bug reports are welcome!  
Feel free to open an issue or submit a pull request.

---

## License

This project is licensed under the MIT License.  
See [LICENSE](LICENSE) for details.

---

## Contact

Created by [It-Saurabh001](https://github.com/It-Saurabh001)  
For support or questions, please open an issue on GitHub.
