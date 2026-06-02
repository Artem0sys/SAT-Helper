# SAT Helper / AI Tutor

An Android app that helps high school students prepare for the SAT exam. I built this app from scratch using Kotlin and Jetpack Compose. It includes a built-in question bank and an AI assistant that explains difficult problems when a student gets them wrong.

## ✨ Key Features

*   **100 SAT Questions:** The app contains 50 Verbal and 50 Math questions. They are stored directly on the phone inside a JSON file, so the app works offline.
*   **AI Explanations:** If you select the wrong answer, you can tap a button to ask the AI Tutor. The app automatically sends the question details to the Gemini API, and the AI generates a clear, step-by-step explanation without any messy code symbols.
*   **Progress Saving:** The app remembers which questions you have already solved, so you won't lose your progress when you close it.
*   **Smart Dark Mode Support:** On the very first launch, the app automatically detects your system's theme and matches it. After that, users can manually switch between Light and Dark themes, and the app will remember their choice for future launches.
*   **Smart Screen Flow:** When you finish all 100 questions, the app shows a "No more questions" screen with a simple back button to return to the main menu.

## 🛠️ Technologies Used

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (for building the user interface)
*   **Navigation:** Jetpack Compose Navigation v3 (Nav3) — using the latest Android standards to handle smooth transitions between screens.
*   **Asynchronous Programming:** Kotlin Coroutines (to keep the app smooth while waiting for AI responses)
*   **Data Storage:** Local JSON parsing and Key-Value storage (to save themes and completed status)

## 📱 How It Works Under the Hood

Instead of hardcoding static messages, the app dynamically builds a prompt for the AI at runtime. It takes the specific question text, the available answers, and the correct option, and wraps them into a structured prompt. This guarantees that the AI always provides an accurate, helpful explanation tailored to that exact problem.
