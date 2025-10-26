# Dartscape - Professional Darts Scoring Application

A comprehensive Android darts scoring application featuring multiple game modes, advanced statistics tracking, and professional tournament support. **Was available on the Google Play Store** as a complete, finished product.

---

## 🚀 Project Overview

**Dartscape** is a **fully featured darts scoring application** designed for both casual players and professional tournament use. This **completed project** has been successfully published on the **Android Play Store** and includes:

- **Multiple game modes**: 501/301/701/901/1001/1201/1501, Cricket, Shanghai, Baseball, and Killer
- **Advanced player management**: Support for up to 4 players, teams, and AI bot opponents
- **Comprehensive statistics**: Track averages, high finishes, doubles percentage, and game history
- **Tournament support**: Legs, sets, and match play with customizable formats
- **Professional features**: Sound effects, themes, data backup/restore, and Pro version with ads removal
- **Responsive UI**: Portrait-optimized interface with smooth animations and professional design

This application demonstrates **complete Android development proficiency** with complex game logic, data persistence, and polished user experience.

---

## 🏗️ Tech Stack

**Android Development:**

- Java with Android SDK (API 22-34)
- AndroidX libraries and Material Design components
- Custom fragment-based navigation system
- SQLite database with custom ORM implementation
- Shared preferences for settings and game state persistence

**Key Dependencies:**

- Material Design Components for modern UI
- Gson for JSON serialization
- Android GIF Drawable for animations
- Google Play Services (Ads, Billing, In-App Updates)
- Guava for enhanced collections and utilities
- Constraint Layout for responsive design

**Features & Architecture:**

- MVVM pattern with custom fragment management
- Modular code structure with separate packages for data, UI, logic, and utilities
- Custom sound system with theme-based audio
- Advanced game logic supporting multiple rule variations
- Persistent data storage with backup/restore functionality

---

## 🎯 Game Modes & Features

**X01 Games (501, 301, 701, 901, 1001, 1201, 1501):**

- Straight/Double/Triple/Master In and Out options
- Customizable leg and set formats
- Automatic checkout suggestions and validation

**Cricket & Cut-Throat Cricket:**

- Traditional and cut-throat scoring
- Point spread limits and open scoring mode
- Real-time number closing tracking

**Specialty Games:**

- **Shanghai**: Sequential number targeting with bonus scoring
- **Baseball**: 9-inning format with bases and runs
- **Killer**: Elimination-style gameplay with customizable lives

**Professional Features:**

- Tournament brackets with legs and sets
- Comprehensive player statistics and averages
- Game history with detailed throw tracking
- Team play support with automatic turn rotation
- AI bot opponents with difficulty settings

---

## 🔥 Key Highlights

- **Production-ready application**: Successfully published and maintained on Google Play Store
- **Complex game logic**: Handles multiple dart game variations with accurate scoring rules
- **Advanced statistics**: Real-time calculation of averages, finishes, and performance metrics
- **Data persistence**: Robust backup/restore system with custom file format
- **Professional UI/UX**: Polished interface with custom animations and sound effects
- **Monetization**: Integrated Google Ads and Pro version with in-app billing
- **Performance optimized**: Efficient memory usage and smooth 60fps animations

---

## 🗂️ Project Structure

This Android project follows standard Android architecture patterns:

- **`/app/src/main/java/com/gworks/dartscape/`** - Main application source code
  - **`data/`** - Game data models and player information
  - **`database/`** - SQLite database management and player statistics
  - **`fragments/`** - UI fragments for different screens and game modes
  - **`logic/`** - Core game logic and scoring algorithms
  - **`main/`** - Main activity and application initialization
  - **`ui/`** - Custom UI components and views
  - **`util/`** - Utility classes for sounds, themes, and helper functions

### How It Works

**For Players:**

- Select game mode and configure settings (`HomeFragment.java`)
- Add players, teams, or bots (`PlayersFragment.java`)
- Play games with real-time scoring (`GameFragment.java`)
- View detailed statistics and history (`StatsFragment.java`)

**For Developers:**

- Game logic is centralized in `GameLogic.java` with data models in `/data/`
- UI components are modular fragments managed by `FragManager.java`
- Database operations handled by `PlayerDatabase.java` for statistics persistence
- Custom themes and sounds managed through utility classes

**Architecture Benefits:**

- Clean separation between game logic, UI, and data layers
- Modular fragment system allows easy addition of new game modes
- Centralized data management ensures consistency across all features
- Professional code structure suitable for team development and maintenance
