package com.example.neonpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NeonPulseTheme {
                NeonPulseApp()
            }
        }
    }
}

@Composable
fun NeonPulseTheme(content: @Composable () -> Unit) {
    val neonGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF00FFFF), Color(0xFF8A2BE2), Color(0xFFFF1493)),
    )
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF00FFFF),
            secondary = Color(0xFFFF1493),
            background = Color(0xFF1A1A1A)
        )
    ) {
        Box(modifier = Modifier.background(neonGradient)) {
            content()
        }
    }
}

data class Task(val id: Int, val title: String, val completed: Boolean = false)

class TaskViewModel : ViewModel() {
    private val _tasks = mutableStateListOf<Task>()
    val tasks: List<Task> get() = _tasks

    fun addTask(title: String) {
        val newId = if (_tasks.isEmpty()) 1 else _tasks.maxOf { it.id } + 1
        _tasks.add(Task(newId, title))
    }

    fun toggleTaskCompletion(taskId: Int) {
        val task = _tasks.find { it.id == taskId }
        task?.let {
            _tasks[_tasks.indexOf(it)] = it.copy(completed = !it.completed)
        }
    }
}

@Composable
fun NeonPulseApp(viewModel: TaskViewModel = viewModel()) {
    var showAddTask by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var timerRunning by remember { mutableStateOf(false) }
    var elapsedTime by remember { mutableStateOf(0L) }

    LaunchedEffect(timerRunning) {
        while (timerRunning) {
            delay(1000)
            elapsedTime += 1
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NeonPulse", fontWeight = FontWeight.Bold, fontSize = 24.sp) },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTask = true },
                containerColor = Color(0xFFFF1493),
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(viewModel.tasks) { task ->
                    TaskItem(task, onToggle = { viewModel.toggleTaskCompletion(task.id) })
                }
            }
            NeonTimer(elapsedTime, timerRunning, onToggle = { timerRunning = !timerRunning })
        }

        AnimatedVisibility(
            visible = showAddTask,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            AddTaskDialog(onDismiss = { showAddTask = false }, onAdd = { title ->
                viewModel.addTask(title)
                showAddTask = false
            })
        }

        if (showSettings) {
            SettingsDialog(onDismiss = { showSettings = false })
        }
    }
}

@Composable
fun TaskItem(task: Task, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2A2A2A))
            .shadow(4.dp, ambientColor = Color(0xFF00FFFF))
            .clickable { onToggle() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.completed,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00FFFF))
        )
        Text(
            text = task.title,
            color = if (task.completed) Color.Gray else Color.White,
            fontSize = 18.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun NeonTimer(elapsedTime: Long, running: Boolean, onToggle: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2A2A2A))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Timer: ${elapsedTime / 60}:${String.format("%02d", elapsedTime % 60)}",
            color = Color(0xFF00FFFF),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onToggle,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A2BE2))
        ) {
            Text(if (running) "Pause" else "Start", color = Color.White)
        }
    }
}

@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var taskTitle by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Task", color = Color(0xFF00FFFF)) },
        text = {
            TextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                label = { Text("Task Title") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF2A2A2A),
                    unfocusedContainerColor = Color(0xFF2A2A2A)
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { if (taskTitle.isNotBlank()) onAdd(taskTitle) }) {
                Text("Add", color = Color(0xFFFF1493))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1A1A1A)
    )
}

@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings", color = Color(0xFF00FFFF)) },
        text = { Text("Customize your NeonPulse experience here.", color = Color.White) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Save", color = Color(0xFFFF1493))
            }
        },
        containerColor = Color(0xFF1A1A1A)
    )
}