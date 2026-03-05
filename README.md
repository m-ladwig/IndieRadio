# IndieRadio 

An Android radio streaming app featuring my favorite human-curated, independent and alternative radio stations from across the US and internationally.

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" />
</p>

##  Features

- **Stream 9 curated radio stations** - Indie, alternative, and underground music from my personal picks: KEXP, Radio Milwaukee, and more
- **Favorite stations** - Quick access to your preferred stations with persistent storage
- **Background playback** - Continue listening while using other apps with media controls in notification
- **Dark mode support** - Automatic system theme detection with Material 3 dynamic colors
- **Modern UI** - Clean, responsive interface built entirely with Jetpack Compose

## Why I Built This

Over the past year or so, I've been trying to get away from algorithm-derived music discovery and focus more
on human-curated radio. I loved community focused stations in places I'd lived like Radio Milwaukee (my home) and the Colorado Sound,
as well as national stalwarts like KEXP, but I didn't like having to download different apps for different stations.

So I built IndieRadio - all my favorite human-curated stations in one app.

## ️ Architecture

This app follows **MVVM (Model-View-ViewModel)** architecture with clear separation of concerns:
```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                   │
│                    StationsScreen.kt                    │
│                                                         │
│  • Observes StateFlow for UI updates                    │
│  • Handles user interactions                            │
│  • No business logic                                    │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ collectAsState()
                     ↓
┌─────────────────────────────────────────────────────────┐
│                  ViewModel Layer                        │
│                 StationsViewModel.kt                    │
│                                                         │
│  • Manages UI state (StateFlow)                         │
│  • Handles business logic                               │
│  • Coordinates between Repository and UI                │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ calls
                     ↓
┌─────────────────────────────────────────────────────────┐
│                   Data Layer                            │
│                StationRepository.kt                     │
│                                                         │
│  • Provides stations list                               │
│  • Manages favorites with Room + Flow                   │
│  • Single source of truth for data                      │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ uses
                     ↓
┌─────────────────────────────────────────────────────────┐
│              Local Data (Room Database)                 │
│            FavoriteStationDao.kt                        │
│                                                         │
│  • Reactive Flow-based queries                          │
│  • Automatic UI updates on data changes                 │
└─────────────────────────────────────────────────────────┘
```

### Key Architecture Decisions

**Dependency Injection via ViewModelFactory**
- Enables constructor injection for testability
- Provides default implementations for production
- Allows mock injection for unit tests

**Reactive State Management**
- Room Flow automatically emits on database changes
- ViewModel collects and transforms to UI state
- UI recomposes automatically via StateFlow

**Media3 Integration**
- Uses MediaController/MediaSession pattern (not direct service access)
- Proper separation between UI and playback service
- Follows Android's recommended media architecture

## Tech Stack

### Core
- **Kotlin** - 100% Kotlin codebase
- **Jetpack Compose** - Modern declarative UI
- **Material 3** - Latest Material Design with dynamic theming
- **Coroutines & Flow** - Asynchronous programming and reactive streams

### Architecture Components
- **ViewModel** - UI state management with lifecycle awareness
- **Room** - Local database for favorites persistence
- **StateFlow** - Reactive state container for UI

### Media Playback
- **Media3 ExoPlayer** - Audio streaming engine
- **Media3 Session** - Background playback with media controls
- **MediaController** - Clean separation between UI and service

### Testing
- **JUnit** - Unit testing framework
- **MockK** - Mocking framework for Kotlin
- **Coroutines Test** - Testing suspend functions and Flow
- **Turbine** - Flow testing utilities
- **Architecture Components Test** - ViewModel testing support

## Testing

Comprehensive test coverage on business logic -  learned proper testing patterns while building out Repository and the ViewModel tests:

### Repository Tests (7 tests)
```kotlin
- getStations returns correct number of stations
- getStations includes specific stations
- toggleFavorite adds when not favorited
- toggleFavorite removes when already favorited
- isFavorite returns correct boolean
- getFavoriteStationIds transforms entities to IDs
```

### ViewModel Tests (8 tests)
```kotlin
- initial state has correct stations
- initial state has no favorites
- onFavoriteClicked calls repository
- favorite IDs update when repository emits
- currentStation updates on selection
- isPlaying state management
- isBuffering state management
```

**Run tests:**
```bash
./gradlew test
```

## Stations Included

**Midwest/Fresh Coast:**
- **88Nine Radio Milwaukee** (Milwaukee, WI) 
- **Riverwest Radio** (Milwaukee, WI) 
- **The Current** (Minneapolis, MN)

**Denver (formerly home):**
- **The Colorado Sound** (Denver, CO) 

**East Coast:**
- **WFMU** (Jersey City, NJ)
- **WXPN** (Philadelphia, PA) 

**West Coast:**
- **KEXP** (Seattle, WA) 
- **KCRW** (Santa Monica, CA)

**International:**
- **NTS Radio** (London, UK)

## Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- Android SDK 26+ (Android 8.0+)
- Kotlin 2.0+

### Installation

1. Clone the repository:
```bash
git clone https://github.com/m-ladwig/IndieRadio.git
```

2. Open in Android Studio

3. Sync Gradle files

4. Run on device or emulator (API 26+)

## Design Highlights

- **Edge-to-edge UI** - Modern immersive design with proper insets handling
- **Dynamic color theming** - Adapts to user's wallpaper on Android 12+
- **Responsive grid layout** - Two-column station cards optimized for all screen sizes
- **Custom station colors** - Each station has its own accent color for visual identity
- **Smooth animations** - Compose transitions for state changes

## What I Learned:

Building this app taught me way more than I expected about Android development. Here are the biggest challenges I faced:

### Technical Challenges Solved

**1. Transitioning from In-App Playback to Background Service**
- Initially attempted playback directly in ViewModel using ExoPlayer
- Discovered playback stopped when app went to background or screen turned off
- Researched Android's media architecture and learned about Services and foreground requirements
- Implemented MediaSessionService with proper lifecycle management
- Added MediaController to communicate between UI and background service
- Handled service lifecycle (onCreate, onDestroy, onTaskRemoved)
- Successfully implemented continuous playback in background with proper resource cleanup and no memory leaks

**2. Understanding Room + Flow Reactive Data**
- Learned how Room's Flow automatically emits on database changes
- Implemented reactive favorite system with no manual refresh needed
- Discovered proper data transformation pattern (Entity → Repository → ViewModel → UI)
- Understood difference between `suspend` functions and Flow-based queries
- Result: UI updates automatically when user favorites/unfavorites stations

**3. Removing Anti-Patterns for Testability**
- Discovered `RadioPlaybackService.getInstance()` singleton was blocking unit tests
- Refactored to use Media3's MediaController exclusively for playback control
- Eliminated direct service references in ViewModel
- Implemented ViewModelFactory for dependency injection
- Result: Fully testable ViewModel with mockable dependencies

**4. Testing Asynchronous Code**
- Learned to test suspend functions using `runTest` and `StandardTestDispatcher`
- Implemented MockK for mocking DAO and Repository layers
- Used `advanceUntilIdle()` to control coroutine execution in tests
- Discovered `InstantTaskExecutorRule` for synchronous StateFlow testing
- Result: 15 passing unit tests covering Repository and ViewModel logic


**Finding Working Stream URLs: Harder Than Expected**

I naively thought radio stations (especially public/nonprofits) might just list their stream URLs somewhere for public access.
Nope. Most hide them behind custom web players.

My process became: 
First, try finding the stream in the list of station streams using radio-browser's API (https://de1.api.radio-browser.info/).
Shout out to them, they were an awesome resource. If I couldn't find the URL there, I would open the station's website, hit F12 to open DevTools,
go to the Network tab, click play, and dig through network requests to
find the actual .mp3 or .aac endpoint.

Then I'd test each URL in my browser. If it opened and played back successfully, I'd add it to
the app and test for around 10 minutes, making sure the stream stayed stable with good quality. If the URL was http rather than https, I'd have to update my network rules
to accomodate -- fortunately, this was only needed with Riverwest Radio.

The most frustrating part? I really wanted to add one of my favorite local Milwaukee stations, WMSE to the list, but for the life of me I just couldn't find
a working stream URL for them. Nothing in dev tools, nothing in the API.

If I did this again, I'd probably add a "Report Broken Station" button and
store backup URLs for each station.

## Future Enhancements

Potential features for v2.0:
- [ ] Android Auto integration for listening in the car
- [ ] Sleep timer for automatic playback stop, for bedtime listening
- [ ] Adding your own favorite stations via radio-browser's API

## License

This project is open source and available under the MIT License.

## Author

**Mitch Ladwig**
- GitHub: [@m-ladwig](https://github.com/m-ladwig)
- LinkedIn: [linkedin.com/in/mitchell-ladwig-4a050271](https://www.linkedin.com/in/mitchell-ladwig-4a050271/)

---

Built with love (and plenty of coffee) in Milwaukee, WI