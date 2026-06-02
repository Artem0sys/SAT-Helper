package com.example.sathelper

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

@Serializable
data object Main: NavKey
@Serializable
data object Settings: NavKey
@Serializable
data object MathProblem: NavKey
@Serializable
data object VerbalProblem: NavKey
@Serializable
data class Problem(val id: Int, val type: String, val condition: String, val answers: List<String>, val correctAnswer: String)
@Serializable
data class AIPage(val response: String): NavKey
@Serializable
data object BackAction: NavKey

@Composable
fun NavigationHost(isDarkMode: Boolean, onToggleDarkMode: (Boolean) -> Unit){
    val backStack = remember{ mutableStateListOf<Any>(Main) }
    NavDisplay(
        backStack = backStack,
        onBack = {backStack.removeLastOrNull()},
        entryProvider = entryProvider {
            entry<Main>{
                MainScreen(isDarkMode) { key ->
                    when(key){
                        BackAction -> backStack.removeLastOrNull()
                        else -> backStack.add(key)
                    }
                }
            }
            entry<VerbalProblem> {
                VerbalProblemScreen(isDarkMode) { key ->
                    when (key) {
                        BackAction -> backStack.removeLastOrNull()
                        VerbalProblem -> {
                            backStack.removeLastOrNull()
                            backStack.add(key)
                        }
                        else -> backStack.add(key)
                    }
                }
            }
            entry<MathProblem>{
                MathProblemScreen(isDarkMode) { key ->
                    when (key) {
                        BackAction -> backStack.removeLastOrNull()
                        MathProblem -> {
                            backStack.removeLastOrNull()
                            backStack.add(key)
                        }
                        else -> backStack.add(key)
                    }
                }
            }
            entry<Settings>{
                SettingsScreen(isDarkMode, onToggleDarkMode) {key ->
                    when(key){
                        BackAction -> backStack.removeLastOrNull()
                        else -> backStack.add(key)
                    }
                }
            }
        },
        transitionSpec = { slideInHorizontally(
            animationSpec = tween(300),
            initialOffsetX = { fullWidth -> fullWidth }
        ) togetherWith slideOutHorizontally(
            animationSpec = tween(300),
            targetOffsetX = { fullWidth -> -fullWidth }
        )},
        popTransitionSpec = {slideInHorizontally(
            animationSpec = tween(300),
            initialOffsetX = { fullWidth -> -fullWidth }
        ) togetherWith slideOutHorizontally(
            animationSpec = tween(300),
            targetOffsetX = { fullWidth -> fullWidth }
        )}
    )
}
