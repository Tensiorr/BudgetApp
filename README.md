# BudgetApp

A modern personal budget tracking app for Android built with Kotlin and Jetpack Compose.

![Platform](https://img.shields.io/badge/platform-Android-green.svg)
![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)
![Kotlin](https://img.shields.io/badge/kotlin-2.0.21-purple.svg)
![Compose](https://img.shields.io/badge/compose-latest-blue.svg)

---

## Features

- **Transaction Management** - Track expenses, income, and savings
- **Categories & Tags** - Organize transactions in hierarchical structure
- **Savings Goals** - Create and track financial goals with progress bars
- **Cloud Synchronization** - Firebase Firestore sync across devices
- **Authentication** - Email/Password and Google Sign-In
- **Guest Mode** - Use app without account, sync later
- **Automatic Sync** - Background sync every hour via WorkManager
- **Smart Filtering** - Filter by type, category, tag, date range, or savings goal
- **Monthly Summaries** - Detailed breakdowns with statistics
- **Dark Mode** - Light, dark, or system theme
- **Date Formats** - Multiple date format options (DD.MM.YYYY, MM/DD/YYYY, YYYY-MM-DD)
- **In-App Updates** - Automatic update checking via GitHub Releases

---

## Tech Stack

**Core**
- Kotlin 2.0.21
- Jetpack Compose (Material 3)
- MVVM Architecture
- Kotlin Coroutines + Flow

**Database & Storage**
- Room Database with migrations
- DataStore Preferences
- Firebase Firestore (cloud sync)

**Authentication & Backend**
- Firebase Authentication (Email/Password + Google Sign-In)
- Firebase Firestore
- WorkManager (background sync)

**Networking**
- Retrofit 2 + OkHttp3
- GitHub API integration

**Build**
- Gradle 9.2.1
- ProGuard/R8 optimization
- Min SDK 26, Target SDK 36

---

## Project Structure
```
app/src/main/java/com/tensiorr/budgetapp/
├── data/
│   ├── api/           # GitHub API for updates
│   ├── dao/           # Room DAOs
│   ├── database/      # Database & migrations
│   ├── entity/        # Room entities
│   ├── preferences/   # DataStore
│   ├── repository/    # Data repositories
│   └── worker/        # Background sync workers
├── domain/
│   └── model/         # Domain models
├── ui/
│   ├── dialogs/       # Reusable dialogs
│   ├── screens/       # App screens
│   │   └── auth/      # Login & Register screens
│   ├── theme/         # Material theme
│   └── viewmodel/     # ViewModels
├── util/              # Utilities
└── MainActivity.kt
```

---

## Database Schema

**Entities:**
- `Transaction` - Financial transactions (expense/income/saving)
- `Category` - Transaction categories
- `Tag` - Detailed classification within categories
- `SavingsGoal` - Savings targets with progress tracking
- `TransactionTagCrossRef` - Many-to-many relationship

**Transaction Types:**
- `EXPENSE` - Money spent
- `INCOME` - Money received
- `SAVING` - Money saved towards a goal

**Cloud Structure (Firestore):**
```
/users/{userId}/
  ├── transactions/{transactionId}
  ├── categories/{categoryId}
  ├── tags/{tagId}
  ├── savings_goals/{goalId}
  ├── transaction_tag_refs/{refId}
  └── metadata/sync_info
```

---

## Privacy & Security

### Data Storage
- **Local:** Room SQLite database (encrypted on device)
- **Cloud:** Firebase Firestore (user-isolated collections)
- **Sync:** Automatic conflict resolution (last-write-wins)

### Authentication
- Email/Password with Firebase Auth
- Google Sign-In (One Tap)
- Guest mode (local-only, no account required)

### Security Features
- User data isolation (users can only access their own data)
- ProGuard obfuscation in release builds
- Package verification for APK updates
- SHA-1 fingerprint validation

### Privacy
- No analytics or tracking
- No advertising
- No third-party data sharing
- Data only stored in Firebase (Google Cloud, EU/US regions)

---

## Requirements

- Android 8.0 (API 26) or higher
- ~15 MB storage space
- Internet connection (for cloud sync and Google Sign-In)
- Google Play Services (for Google Sign-In)

---

## License

MIT License

Copyright (c) 2025 Tensiorr

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

---

## Author

**Tensiorr**  
[GitHub](https://github.com/Tensiorr) • [Repository](https://github.com/Tensiorr/BudgetApp)