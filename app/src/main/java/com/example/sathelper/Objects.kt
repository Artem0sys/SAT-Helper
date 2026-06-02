package com.example.sathelper

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.js.ExperimentalJsFileName

object PreferencesKeys{
    val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
}

suspend fun saveIsDarkModeState(context: Context, isDarkMode: Boolean){
    context.dataStore.edit { preferences -> preferences[PreferencesKeys.IS_DARK_MODE] = isDarkMode }
} fun getIsDarkModeValue(context: Context): Flow<Boolean> {
    val systemMode = (context.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
    return context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_DARK_MODE] ?: systemMode
    }
}

suspend fun getProblemsData(context: Context, fileName: String): List<Problem>{
    return withContext(Dispatchers.IO){
        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val jsonParser = Json { ignoreUnknownKeys = true }
        val questionsList = jsonParser.decodeFromString<List<Problem>>(jsonString)
        questionsList
    }

}

object ProblemPreferences{
    suspend fun saveProblemPrefs(context: Context, id: Int, isSolved: Boolean){
        val key = booleanPreferencesKey("problem_$id")
        context.dataStore.edit { preferences -> preferences[key] = isSolved }
    }
    fun getProblemPrefs(context: Context, id: Int): Flow<Boolean>{
        val key = booleanPreferencesKey("problem_$id")
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: false
        }
    }
    fun getAllStatusesFlow(context: Context): Flow<Map<Int, Boolean>> {
        return context.dataStore.data.map { preferences ->
            val statusMap = mutableMapOf<Int, Boolean>()
            preferences.asMap().forEach { (key, value) ->
                if (key.name.startsWith("problem_") && value is Boolean) {
                    val id = key.name.removePrefix("problem_").toIntOrNull()
                    if (id != null) {
                        statusMap[id] = value
                    }
                }
            }
            statusMap
        }
    }
}