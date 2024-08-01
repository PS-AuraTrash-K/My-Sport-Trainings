package com.jimbosht.mysporttrainings

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jimbosht.mysporttrainings.ui.theme.MySportTrainingsTheme

class AddActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MySportTrainingsTheme {

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Greeting() {
    Scaffold(topBar = {
        TopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            title = {
                Text("Add Activity")
            },
            navigationIcon = {
                IconButton(onClick = { /* do something */ }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Localized description"
                    )
                }
            },
            actions = {
                ElevatedButton(onClick = { }) {
                    Text("Add")
                }
            }
        )
    }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp),
        ) {
            var name by remember { mutableStateOf("") }
            var date by remember { mutableStateOf("") }
            var count by remember { mutableIntStateOf(3) }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = name,
                onValueChange = {
                    name = it
                },
                label = { Text("Name") }
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = date,
                    readOnly = true,
                    onValueChange = {
                        date = it
                    },
                    label = { Text("Date") },
                )
                FilledTonalIconButton(
                    onClick = {},
                    modifier = Modifier
                        .padding(start = 16.dp, top = 8.dp)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Localized description"
                    )
                }
            }

            Column(modifier = Modifier.padding(top = 24.dp)) {
                Text("Repetitions", color = MaterialTheme.colorScheme.secondary)
                Reps(count)
                Row (modifier = Modifier.align(Alignment.End).padding(top = 8.dp)) {
                    RepsAddRemoveButtons(
                        count
                    )
                }
            }
        }
    }
}

@Composable
fun RepsAddRemoveButtons(count: Int) {
    FilledTonalIconButton(
        onClick = {},
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add Reps"
        )
    }
    FilledTonalIconButton(
        onClick = {},
        modifier = Modifier.padding(start = 8.dp).size(48.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_remove_24),
            contentDescription = "Remove Reps"
        )
    }
}


object RepsCount {
    fun getString(count: Int): String = when (count) {
        1 -> "First Rep"
        2 -> "Second Rep"
        3 -> "Third Rep"
        4 -> "Fourth Rep"
        5 -> "Fifth Rep"
        6 -> "Sixth Rep"
        else -> {
            ""
        }
    }
}


@Composable
fun Reps(count: Int) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (i in 1..3) {
            var text by remember { mutableStateOf("") }

            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                },
                maxLines = 1,
                label = {
                    Text(
                        RepsCount.getString(i),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
            )
        }
    }
    Row(modifier = Modifier.fillMaxWidth()) {

    }
}


@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GreetingPreview() {
    MySportTrainingsTheme {
        Greeting()

    }
}