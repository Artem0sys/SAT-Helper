package com.example.sathelper

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key.Companion.Home
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation3.runtime.NavKey
import com.example.sathelper.ui.theme.SATHelperTheme
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

private const val USER_PREFERENCES_NAME = "user_settings"

val Context.dataStore by preferencesDataStore(name = USER_PREFERENCES_NAME)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()
            val isDarkMode by getIsDarkModeValue(context).collectAsState(initial = isSystemInDarkTheme())
            SATHelperTheme(darkTheme = isDarkMode) {
                NavigationHost(isDarkMode) {newValue ->
                    coroutineScope.launch {
                        saveIsDarkModeState(context, newValue)
                    }
                }
            }
        }
    }
}

@Composable
fun MathProblemScreen(isDarkMode: Boolean, onButtonClicked: (Any) -> Unit){
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val problems = problemsLogic("MathProblem")
    val problem : Problem? = problems.firstOrNull()
    if(problem == null){
        Box(
            modifier = Modifier.fillMaxSize().padding(top = 65.dp).background(MaterialTheme.colorScheme.background)
        ) {
            Text(
                text = "No problems currently available",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.width(300.dp).align(Center)
            )
        }
        DefaultHeader("Error", MathProblem) {key -> onButtonClicked(key) }
    }
    else{
        val prompt = """
                        Act as a friendly SAT instructor. Explain this problem:
                        "${problem.condition}"
                        
                        Options: ${problem.answers}
                        Correct choice: ${problem.correctAnswer}
                        
                        Explain why "${problem.correctAnswer}" is correct and why the other choices fail. Keep the explanation concise, easy to read, and tailored for a student. 
                        
                        Strict formatting rule: Output ONLY plain text. Do not use asterisks (**), hashtags, or any other special markdown symbols for emphasis. Use standard line breaks for paragraphs.
                    """.trimIndent()
        var selected by remember{ mutableStateOf("")}
        var isIncorrect by remember{ mutableStateOf(false)}
        var isLoading by remember{ mutableStateOf(false)}
        var response by remember{ mutableStateOf("")}
        val generativeModel = remember{ GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )}
        if(isLoading){
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(70.dp).align(Center),
                    color = MaterialTheme.colorScheme.background,
                    trackColor = MaterialTheme.colorScheme.secondary
                )
            }
        }
        else{
            if(response == ""){
                Box(
                    modifier = Modifier.fillMaxSize().padding(top = 65.dp).background(MaterialTheme.colorScheme.background)
                ){
                    MathText(
                        text = problem.condition,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(TopCenter).width(300.dp).padding(top = 40.dp)
                    )
                    Column(
                        modifier = Modifier.width(300.dp).align(Center).padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        problem.answers.forEach { answer ->
                            Box(
                                modifier = Modifier.height(60.dp).width(300.dp).padding(bottom = 10.dp)
                            ){
                                Row(
                                    modifier = Modifier.align(Center)
                                ) {
                                    Box(
                                        modifier = Modifier.height(60.dp)
                                    ) {
                                        RadioButton(
                                            selected = answer == selected,
                                            onClick = {
                                                selected = answer
                                                isIncorrect = false
                                            },
                                            modifier = Modifier.align(Center),
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = if(isDarkMode){
                                                    Color.White
                                                }
                                                else MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    }
                                    Box(
                                        modifier = Modifier.height(60.dp)
                                    ){
                                        MathText(
                                            text = answer,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.align(Center)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (isIncorrect){
                        Row(
                            modifier = Modifier
                                //.width(300.dp).height(50.dp)
                                .align(Alignment.BottomCenter).padding(bottom = 165.dp)
                        ) {
                            Text(
                                text = "Incorrect",
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 10.dp)
                            )
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        try{
                                            isLoading = true
                                            val result = generativeModel.generateContent(prompt)
                                            response = result.text ?: "No answer provided :("
                                        } catch (e: Exception){
                                            response = "Error: ${e.message}"
                                        } finally{
                                            isLoading = false
                                        }

                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.padding(start = 20.dp)
                            ) {
                                Text(
                                    text = "Ask AI"
                                )
                            }
                        }
                    }
                    else{
                        Row(
                            modifier = Modifier
                                //.width(300.dp).height(50.dp)
                                .align(Alignment.BottomCenter).padding(bottom = 165.dp)
                        ) {
                            Spacer(modifier = Modifier.height(50.dp))
                        }
                    }
                    Button(
                        onClick = { when{
                            problem.correctAnswer == selected -> {
                                coroutineScope.launch {
                                    ProblemPreferences.saveProblemPrefs(context, problem.id, true)
                                }
                                onButtonClicked(MathProblem)
                            }
                            else -> isIncorrect = true
                        } },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 105.dp)
                    ) {
                        Text(
                            text = "Submit"
                        )
                    }
                }
                DefaultHeader("Math problem", MathProblem) {key -> onButtonClicked(key)}
            }
            else{
                val scrollState = rememberScrollState()
                Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(top = 85.dp)
                ){
                    Box(
                        modifier = Modifier.width(300.dp).height(570.dp).align(Alignment.TopCenter).verticalScroll(scrollState).padding(25.dp)
                    ) {
                        MathText(
                            text = response,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                        )
                    }
                    Button(
                        onClick = { response = "" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 105.dp)
                    ) {
                        Text(
                            text = "Go back"
                        )
                    }
                }
                DefaultHeader("AI Chat", MathProblem) {key -> onButtonClicked(key) }
            }
        }
    }
}
@Composable
fun VerbalProblemScreen(isDarkMode: Boolean, onButtonClicked: (Any) -> Unit){
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val problems = problemsLogic(VerbalProblem)
    val problem : Problem? = problems.firstOrNull()
    if(problem == null){
        Box(
            modifier = Modifier.fillMaxSize().padding(top = 65.dp).background(MaterialTheme.colorScheme.background)
        ) {
            Text(
                text = "No problems currently available",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.width(300.dp).align(Center)
            )
        }
        DefaultHeader("Error", VerbalProblem) {key -> onButtonClicked(key) }
    }
    else{
        val prompt = """
                        Act as a friendly SAT instructor. Explain this problem:
                        "${problem.condition}"
                        
                        Options: ${problem.answers}
                        Correct choice: ${problem.correctAnswer}
                        
                        Explain why "${problem.correctAnswer}" is correct and why the other choices fail. Keep the explanation concise, easy to read, and tailored for a student. 
                        
                        Strict formatting rule: Output ONLY plain text. Do not use asterisks (**), hashtags, or any other special markdown symbols for emphasis. Use standard line breaks for paragraphs.
                    """.trimIndent()
        var selected by remember{ mutableStateOf("")}
        var isIncorrect by remember{ mutableStateOf(false)}
        var isLoading by remember{ mutableStateOf(false)}
        var response by remember{ mutableStateOf("")}
        val generativeModel = remember{ GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )}
        if(isLoading){
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(70.dp).align(Center),
                    color = MaterialTheme.colorScheme.background,
                    trackColor = MaterialTheme.colorScheme.secondary
                )
            }
        }
        else{
            if(response == ""){
                Box(
                    modifier = Modifier.fillMaxSize().padding(top = 65.dp).background(MaterialTheme.colorScheme.background)
                ){
                    Text(
                        text = problem.condition,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(TopCenter).width(300.dp).padding(top = 40.dp)
                    )
                    Column(
                        modifier = Modifier.width(300.dp).align(Center).padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        problem.answers.forEach { answer ->
                            Box(
                                modifier = Modifier.height(60.dp).width(300.dp).padding(bottom = 10.dp)
                            ){
                                Row(
                                    modifier = Modifier.align(Center)
                                ) {
                                    Box(
                                        modifier = Modifier.height(60.dp)
                                    ) {
                                        RadioButton(
                                            selected = answer == selected,
                                            onClick = {
                                                selected = answer
                                                isIncorrect = false
                                            },
                                            modifier = Modifier.align(Center),
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = if(isDarkMode){
                                                    Color.White
                                                }
                                                else MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    }
                                    Box(
                                        modifier = Modifier.height(60.dp)
                                    ){
                                        Text(
                                            text = answer,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.align(Center)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (isIncorrect){
                        Row(
                            modifier = Modifier
                                //.width(300.dp).height(50.dp)
                                .align(Alignment.BottomCenter).padding(bottom = 165.dp)
                        ) {
                            Text(
                                text = "Incorrect",
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 10.dp)
                            )
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        try{
                                            isLoading = true
                                            val result = generativeModel.generateContent(prompt)
                                            response = result.text ?: "No answer provided :("
                                        } catch (e: Exception){
                                            response = "Error: ${e.message}"
                                        } finally{
                                            isLoading = false
                                        }

                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.padding(start = 20.dp)
                            ) {
                                Text(
                                    text = "Ask AI"
                                )
                            }
                        }
                    }
                    else{
                        Row(
                            modifier = Modifier
                                //.width(300.dp).height(50.dp)
                                .align(Alignment.BottomCenter).padding(bottom = 165.dp)
                        ) {
                            Spacer(modifier = Modifier.height(50.dp))
                        }
                    }
                    Button(
                        onClick = { when{
                            problem.correctAnswer == selected -> {
                                coroutineScope.launch {
                                    ProblemPreferences.saveProblemPrefs(context, problem.id, true)
                                }
                                onButtonClicked(VerbalProblem)
                            }
                            else -> isIncorrect = true
                        } },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 105.dp)
                    ) {
                        Text(
                            text = "Submit"
                        )
                    }
                }
                DefaultHeader("Verbal problem", VerbalProblem) {key -> onButtonClicked(key)}
            }
            else{
                val scrollState = rememberScrollState()
                Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(top = 85.dp)
                ){
                    Box(
                        modifier = Modifier.width(300.dp).height(570.dp).align(Alignment.TopCenter).verticalScroll(scrollState)
                    ) {
                        Text(
                            text = response,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                        )
                    }
                    Button(
                        onClick = { response = "" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 105.dp)
                    ) {
                        Text(
                            text = "Go back"
                        )
                    }
                }
                DefaultHeader("AI Chat", VerbalProblem) {key -> onButtonClicked(key) }
            }
        }
    }
}
@Composable
fun problemsLogic(calledFrom: Any): List<Problem> {
    val context = LocalContext.current
    val rawProblems = remember { mutableStateListOf<Problem>() }
    val problemsStates by remember{
        ProblemPreferences.getAllStatusesFlow(context)
    }.collectAsState(initial = emptyMap())
    LaunchedEffect(Unit) {
        val data = getProblemsData(context, "problems.json")
        rawProblems.clear()
        rawProblems.addAll(data)
    }

    return when(calledFrom){
        VerbalProblem -> rawProblems.filter { problem: Problem ->
            val id: Int = problem.id
            val isSolved = problemsStates[id] ?: false
            !isSolved && problem.type == "Verbal"
        }

        else -> rawProblems.filter { problem: Problem ->
            val id: Int = problem.id
            val isSolved = problemsStates[id] ?: false
            !isSolved && problem.type == "Math"
        }

    }

}
@Composable
fun DefaultHeader(screenName: String, calledFrom: Any, onButtonClicked: (Any) -> Unit){
    Box(

        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .background(MaterialTheme.colorScheme.secondary)
    ){
        Text(
            text = screenName,
            modifier = Modifier.align(Center), color = Color.White
        )
        when(calledFrom){
            Settings -> {Spacer(modifier = Modifier
                .size(48.dp)
                .align(CenterEnd)
                .padding(end = 5.dp))
                IconButton(onClick = { onButtonClicked(BackAction)},
                    modifier = Modifier
                        .align(CenterStart)
                        .padding(start = 5.dp) ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        "Back to main page",
                        tint = Color.White)
                } }
            MathProblem, VerbalProblem -> {
                IconButton(onClick = { onButtonClicked(Settings)},
                    modifier = Modifier
                        .align(CenterEnd)
                        .padding(end = 5.dp) ) {
                    Icon(imageVector = Icons.Default.Settings,
                        "Settings",
                        tint = Color.White)
                }
                IconButton(onClick = { onButtonClicked(BackAction)},
                    modifier = Modifier
                        .align(CenterStart)
                        .padding(start = 5.dp) ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        "Back",
                        tint = Color.White)
                }
            }
            else -> {Spacer(modifier = Modifier
                .size(48.dp)
                .align(CenterStart)
                .padding(start = 5.dp))
                IconButton(onClick = { onButtonClicked(Settings)},
                    modifier = Modifier
                        .align(CenterEnd)
                        .padding(end = 5.dp) ) {
                    Icon(imageVector = Icons.Default.Settings,
                        "Settings",
                        tint = Color.White)
                } }
        }

    }
}

@Composable
fun MainScreen(isDarkMode:Boolean, onButtonClicked: (Any) -> Unit){
    Box(

    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
                .padding(65.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { onButtonClicked(MathProblem)},
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Math"
                )
            }
            Button(
                onClick = { onButtonClicked(VerbalProblem)},
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Verbal"
                )
            }
        }
        DefaultHeader("Home Page", Main) { key -> onButtonClicked(key)}
    }
}

@Composable
fun SettingsScreen(isDarkMode: Boolean, onToggleDarkMode: (Boolean) -> Unit, onButtonClicked: (Any) -> Unit){
    Box(

    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 65.dp)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.padding(top = 20.dp, start = 30.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(60.dp)
                        .width(350.dp)
                ){
                    Text(text = "Dark mode", color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(CenterStart)
                        )
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { isChecked ->
                            onToggleDarkMode(isChecked)
                        },
                        modifier = Modifier
                            .align(CenterStart)
                            .padding(start = 120.dp),
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = if (isDarkMode) Color.White else MaterialTheme.colorScheme.secondary,
                            checkedThumbColor = if (isDarkMode) MaterialTheme.colorScheme.background else Color.White,

                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
        DefaultHeader("Settings",  Settings) { key -> onButtonClicked(key)}
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MathText(text: String, color: Color, modifier: Modifier = Modifier) {
    val regex = "\\{([^/]+)/([^}]+)\\}".toRegex()
    val parts = text.split("(?=\\{)|(?<=\\})".toRegex())

    FlowRow(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        parts.forEach { part ->
            val match = regex.matchEntire(part)
            if (match != null) {
                val (num, den) = match.destructured
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    Text(text = num, color = color, fontSize = 12.sp, lineHeight = 12.sp)
                    Box(Modifier
                        .width((maxOf(num.length, den.length) * 8).dp)
                        .height(1.dp)
                        .background(color))
                    Text(text = den, color = color, fontSize = 12.sp, lineHeight = 12.sp)
                }
            } else {
                Text(text = part, color = color)
            }
        }
    }
}
