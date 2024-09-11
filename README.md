
# DrawingApp

## Project Overview

**DrawingApp** is an Android application that allows users to create drawings using different shapes, colors, and stroke widths. The app uses the **MVVM architecture** to manage the drawing state and ensure data persistence across screen rotations. The user can also modify the alpha (opacity) of the strokes and clear the canvas to start fresh.

This is the **first phase** of the project, which demonstrates basic drawing functionality and partial data persistence.

## Features

- **Draw Shapes**: Users can draw free-form lines or choose from pre-defined shapes like **round**, **square**, and **star**.
- **Change Stroke Color**: Users can select from three colors: **black**, **red**, and **blue**.
- **Adjust Stroke Width**: Users can control the thickness of the strokes using a **SeekBar**.
- **Adjust Alpha (Opacity)**: Users can change the transparency of the strokes with a **SeekBar**.
- **Clear Canvas**: A button is provided to clear the drawing and start fresh.
- **MVVM Architecture**: The app uses the **MVVM pattern** to manage the drawing state, ensuring lifecycle-awareness and better code separation.
- **Data Persistence**: The app preserves the drawing state during screen rotations but does not yet save the drawings between app restarts (this will be covered in Phase 2).

## Screens

1. **Drawing Screen**: Main screen where users can draw, modify pen attributes, and clear the canvas.

## Setup

### Prerequisites

To run this project, you'll need the following tools:
- **Android Studio** (Arctic Fox or later)
- **Java 8 or later**
- **Gradle 7.0 or later**

### How to Use

1. **Draw**: Tap and drag your finger on the screen to draw.
2. **Change Color**: Click on one of the color buttons (**Black**, **Red**, **Blue**) to change the pen color.
3. **Adjust Stroke Width**: Use the **SeekBar** to increase or decrease the thickness of the strokes.
4. **Adjust Alpha (Opacity)**: Use the **SeekBar** to modify the transparency of the strokes.
5. **Clear Canvas**: Click the **Clear** button to erase all drawings and start over.
6. **Shape Selection**: Choose a shape (**Round**, **Square**, or **Star**) from the provided buttons to draw that shape.

## Architecture

This app follows the **MVVM architecture**:
- **Model**: Holds the drawing data such as paths, paint configurations, and user-selected shapes.
- **ViewModel**: Manages the data and communicates with the view while being lifecycle-aware, retaining data on screen rotation.
- **View**: Displays the drawing canvas and handles user interactions like touch events, button clicks, and SeekBar adjustments.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
