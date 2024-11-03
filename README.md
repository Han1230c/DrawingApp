# DrawingApp

## Project Overview

**DrawingApp** is an Android application that allows users to create and share drawings. It features Firebase authentication and a custom Ktor backend server.

## Key Features

### Drawing Features
- **Multiple Drawing Tools**: Round brush, Square, Star, and Ball mode (with accelerometer support)
- **Basic Colors**: Black, Red, and Blue
- **Brush Customization**: Adjustable width and opacity
- **Clear Canvas**: One-click reset

### Social Features
- **User Authentication**: Email/password login via Firebase
- **Drawing Sharing**: Users can share their artwork
- **Online Sync**: Automatic refresh of shared works
- **Offline Support**: Local storage with Room database

## Technical Architecture

### App Architecture (MVVM)
- **Model**: 
  - Room database (Drawing, DrawingDao)
  - Repository pattern for data handling
- **ViewModel**: DrawingViewModel managing UI states
- **View**: 
  - Custom DrawingView for drawing functionality
  - Compose UI for main interface

### Network Layer
- OkHttp client
- API service
- Firebase UID based authentication

### Backend Server (Ktor)
- RESTful API endpoints
- H2 in-memory database
- Token-based authentication system

## API Endpoints
- POST /drawings            - Create new drawing
- GET  /drawings/{id}      - Get specific drawing
- POST /drawings/{id}/share - Share drawing
- GET  /public/drawings/shared - Get all shared drawings

## Development Setup

### Requirements
- Android Studio
- Firebase project configuration
- Ktor backend server

### Configuration Steps
1. Configure Firebase (`google-services.json`)
2. Update `MainActivity.SERVER_IP` with backend server address (default 10.0.2.2)
3. Run backend server
4. Launch Android app

## Major Dependencies
```gradle
- Firebase Authentication
- Room Database
- Jetpack Compose
- OkHttp3
- Gson
- Kotlin Coroutines
