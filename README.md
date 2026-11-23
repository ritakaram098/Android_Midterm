# Pet Care Routine and Reminder Log 🐾

**Pet Care Routine and Reminder Log** is a native Android application built with **Kotlin** and **Jetpack Compose**. It provides pet owners with a modern, elegant, and easy-to-use platform to create pet profiles, manage care tasks (such as feeding, grooming, medication, and vet visits), maintain a history of completed tasks, and receive local notifications for upcoming routines.

---

## 📋 Project Overview

Pet owners often struggle to keep track of daily and recurring routines for multiple pets, such as medication schedules, flea and tick treatments, grooming routines, and vet appointments. Missing a medication dose or delaying a treatment can affect a pet's health and well-being.

**Pet Care Routine and Reminder Log** solves this problem by offering:
- Centralized pet profile management for dogs, cats, birds, rabbits, fish, reptiles, and other companions.
- Pet-specific task tracking categorized by routine type.
- Automated local notification reminders using Android WorkManager.
- Activity history tracking to log completed care events with exact date and time timestamps.

---

## ✨ Main Features

- 🐶 **Pet Registration & Profiles**: Register pets with details including name, pet type (Dog, Cat, Bird, Rabbit, Fish, Reptile, etc.), breed, sex, birth date, and avatar illustrations.
- 📋 **Care Task Management**: Create, view, edit, and delete care tasks tailored to each pet.
- 🏷️ **Categorized Routines**: Tasks can be assigned to categories such as *Feeding*, *Grooming*, *Bathing*, *Flea & Tick Treatment*, *Medication*, *Exercise*, *Vet Appointment*, or custom categories.
- 🗓️ **Flexible Schedules & Reminders**: Set due dates, due times, repeat frequencies (*Does not repeat*, *Daily*, *Weekly*, *Monthly*), and optional notes for each task.
- 🔍 **Dynamic Task Filtering**: Instantly filter tasks by **All Tasks**, **Pending**, or **Completed**.
- ✅ **Interactive Task Completion**: Mark tasks as done with animated visual feedback. Completed tasks automatically move to completion history.
- 📜 **Completion History Timeline**: View a chronological history log of all completed care tasks for each pet.
- 🔔 **Scheduled Local Notifications**: Automated reminders powered by Android **WorkManager** that trigger notifications even when the app is closed or after system reboots.
- 🔒 **Android 13+ Notification Permission**: Built-in runtime permission prompt for `POST_NOTIFICATIONS`.

---

## 🛠️ Technologies Used

| Category | Technology / Library | Description |
| :--- | :--- | :--- |
| **Language** | [Kotlin](https://kotlinlang.org/) (v2.2.10) | Primary programming language |
| **UI Toolkit** | [Jetpack Compose](https://developer.android.com/jetpack/compose) (M3 BOM 2026.02.01) | Declarative UI framework using Material 3 |
| **Architecture** | MVVM + Repository Pattern | Clean separation of concerns |
| **Local Database** | [Room Database](https://developer.android.com/training/data-storage/room) (v2.8.5) | SQLite object mapping with KSP |
| **Background Work** | [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) (v2.9.0) | Background job scheduling for reminders |
| **Navigation** | [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) (v2.7.7) | Declarative screen routing |
| **Image Loading** | [Coil Compose](https://coil-kt.github.io/coil/) (v2.7.0) | Asynchronous image loading for pet photos |
| **Annotation Compiler** | [KSP](https://developer.android.com/build/migrate-to-ksp) (v2.2.10-2.0.2) | Kotlin Symbol Processing for Room DAOs |

---

## 🏛️ Project Architecture

The application strictly follows the recommended **MVVM (Model-View-ViewModel)** pattern and **Repository Pattern**:

```
com.example.petcare/
├── MainActivity.kt                      # Main Activity hosting Navigation NavHost
├── data/                                # Data Layer (Entities, DAOs, Room DB, Repositories)
│   ├── AppDatabase.kt                   # Room Database definition & schema migrations
│   ├── Pet.kt                           # Pet Room Entity
│   ├── CareTask.kt                      # Care Task Room Entity
│   ├── CareHistory.kt                   # Care Task History Room Entity
│   ├── PetDao.kt                        # Data Access Object for Pet operations
│   ├── CareTaskDao.kt                   # Data Access Object for Care Tasks & History
│   ├── PetRepository.kt                 # Repository layer for Pet data
│   └── CareTaskRepository.kt            # Repository layer for Care Task data
├── ui/                                  # Presentation Layer (Compose Screens & ViewModels)
│   ├── PetListScreen.kt                 # Pet Dashboard & Profile List
│   ├── AddPetScreen.kt                  # Add New Pet Screen
│   ├── CareTaskScreen.kt                # Care Tasks & Activity History Screen
│   ├── AddCareTaskScreen.kt             # Add & Edit Care Task Form Screen
│   ├── PetViewModel.kt                  # ViewModel for Pet State Management
│   ├── CareTaskViewModel.kt             # ViewModel for Care Task & Reminder State Management
│   └── theme/                           # Color, Theme, and Typography definitions
└── worker/                              # Background Scheduling Layer
    └── ReminderWorker.kt                # WorkManager CoroutineWorker for local notifications
```

---

## ⚙️ Prerequisites

Before building and running the application, make sure your development environment meets the following requirements:

- **Android Studio**: Android Studio Ladybug (2024.2.1) or higher.
- **JDK Version**: Java Development Kit (JDK) 11.
- **Android SDK**:
  - Compile SDK: `37`
  - Target SDK: `37`
  - Minimum SDK: `24` (Android 7.0 Nougat or higher)
- **Gradle**: Gradle 8.x (managed via included Gradle Wrapper).
- **Device**: Android Emulator (AVD) or physical Android device running Android 7.0 (API level 24) or higher.

---

## 📥 Installation and Setup

Follow these step-by-step instructions to download, configure, build, and run the project locally:

### Step 1: Clone the Repository
Open your terminal or command prompt and clone the repository:

```bash
git clone https://github.com/ritakaram098/Pet-care-routine-and-reminder-log.git
cd Pet-care-routine-and-reminder-log
```

### Step 2: Open in Android Studio
1. Launch **Android Studio**.
2. Click **Open** (or `File` > `Open...`).
3. Select the cloned `Pet-care-routine-and-reminder-log` project folder.

### Step 3: Wait for Gradle Synchronization
Android Studio will automatically detect the Gradle build files and sync dependencies. Wait for the sync process to finish completely.

### Step 4: Verify SDK Components
Ensure that Android SDK Platform 37 is installed via **Tools** > **SDK Manager** > **SDK Platforms**.

### Step 5: Build the Application
To compile the project without running it, execute in the terminal:

```bash
./gradlew assembleDebug
```
Or click **Build** > **Make Project** in Android Studio.

---

## 🚀 How to Run the Application

1. **Connect a Device or Launch an Emulator**:
   - Start an Android Virtual Device (AVD) via **Tools** > **Device Manager**.
   - Or connect a physical Android device via USB and enable **USB Debugging** in Developer Options.
2. **Select Run Configuration**:
   - In the top toolbar, select `app` from the run configuration dropdown.
3. **Run the Project**:
   - Click the green **Run** button (or press `Shift` + `F10`).
4. **Grant Permissions**:
   - Upon adding a task with a notification enabled on Android 13+ (API 33+), accept the **Notification Permission** prompt.
5. **Explore the App**:
   - Tap **+** to register your first pet profile.
   - Tap on the pet card to view and manage care tasks.
   - Add feeding, grooming, or medication routines with due times and reminders.
   - Complete tasks and view the recorded history timeline.

---

## 💾 Database Architecture

The application uses **Room Database** over SQLite for offline data persistence.

- **Database Name**: `pet_care_database`
- **Database Version**: `5`
- **Schema & Tables**:
  - `pets`: Stores pet profiles (`id`, `name`, `type`, `breed`, `sex`, `birthDate`, `weight`, `notes`, `photoUri`).
  - `care_tasks`: Stores scheduled tasks (`id`, `petId`, `title`, `description`, `category`, `frequency`, `dueDate`, `dueTime`, `isCompleted`, `completedAt`, `reminderDateTime`, `reminderEnabled`, `createdAt`, `updatedAt`).
  - `care_history`: Stores completed activity logs (`id`, `taskId`, `petId`, `taskTitle`, `category`, `completedAt`).
- **Migrations**: `MIGRATION_4_5` ensures non-destructive schema upgrades without deleting user or pet data.
- **Setup**: Created automatically on first app launch via `AppDatabase.getDatabase(context)`. No manual database setup is required.

---

## 🔔 Notifications and Permissions

- **Local Reminders**: Scheduled using `WorkManager` with `OneTimeWorkRequestBuilder<ReminderWorker>()` and `enqueueUniqueWork` (`REPLACE` policy).
- **Notification Channel**: `pet_care_reminders` ("Pet Care Reminders", High Importance).
- **Permissions**:
  - `android.permission.POST_NOTIFICATIONS` is declared in `AndroidManifest.xml`.
  - Automatically requested on Android 13+ (API 33+) when opening the care tasks screen.

---

## 🛠️ Troubleshooting

### 1. Gradle Sync Fails
- **Solution**: Go to **File** > **Invalidate Caches...** > **Invalidate and Restart**. Ensure Android Studio JDK is set to JDK 11 (**Settings** > **Build, Execution, Deployment** > **Build Tools** > **Gradle** > **Gradle JDK**).

### 2. Missing Android SDK Platform 37
- **Solution**: Open **Tools** > **SDK Manager** > **SDK Platforms**, check **Android 15.0 ("VanillaIceCream") / API Level 37**, and click **Apply**.

### 3. Notifications Are Not Showing
- **Solution**:
  1. Verify that notifications are enabled in your phone's system settings under **Apps** > **Pet Care** > **Notifications**.
  2. Ensure the reminder date/time set for the task is in the future.

### 4. Build Errors Related to KSP / Room
- **Solution**: Clean and rebuild the project via `./gradlew clean assembleDebug` or **Build** > **Clean Project**.

---

## 📂 Repository Directory Structure

```text
Pet-care-routine-and-reminder-log/
├── app/
│   ├── build.gradle.kts                   # Module-level Gradle configuration
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml        # Application manifest & permissions
│       │   ├── java/com/example/petcare/  # Source Kotlin files (data, ui, worker)
│       │   └── res/                       # Resource drawables, mipmaps, XMLs
│       └── test/                          # Unit tests
├── gradle/
│   ├── libs.versions.toml                 # Centralized Version Catalog
│   └── wrapper/                           # Gradle Wrapper files
├── build.gradle.kts                       # Top-level build script
├── settings.gradle.kts                    # Settings script
└── README.md                              # Documentation file
```

---

## 🔮 Future Improvements

- ☁️ **Cloud Sync & Backup**: Integrate Firebase Firestore to sync pet data across multiple devices.
- 📄 **Export Vet Logs**: Generate downloadable PDF reports of care history for veterinarian visits.
- 🔔 **Snooze Reminders**: Add interactive notification buttons to snooze or mark tasks complete directly from the notification tray.
- 📊 **Health & Weight Analytics**: Graphical charts tracking pet weight, feeding routines, and symptom logs over time.
