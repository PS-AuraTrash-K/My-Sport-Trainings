package com.jimbosht.mysporttrainings.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.jimbosht.mysporttrainings.database.SQLite
import com.jimbosht.mysporttrainings.extra.*
import com.jimbosht.mysporttrainings.ui.theme.MySportTrainingsTheme

class AllDatesActivity : ComponentActivity() {
    private var activityName by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityName = intent.extras.let {
            it?.getString(MainActivity.ACTIVITY_NAME).toString()
        }

        setContent {
            Main()
        }
    }


    data class DialogExtra(
        var showDialog: Boolean,
        var name: String?,
        var date: Long?
    )


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Main() {
        var clickedHeader by remember { mutableStateOf("") }
        var dialogExtra by remember {
            mutableStateOf(
                DialogExtra(
                    showDialog = false,
                    name = null,
                    date = null
                )
            )
        }
        var composableList by remember {
            val db = SQLite(this@AllDatesActivity)
            mutableStateOf(db.getAllActivityDates(activityName))
        }

        MySportTrainingsTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            titleContentColor = MaterialTheme.colorScheme.primary,
                        ),
                        title = {
                            Text("All Dates: $activityName")
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                val nav = Intent(this, MainActivity::class.java)
                                startActivity(nav)
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Localized description"
                                )
                            }
                        }
                    )
                }
            ) { padding ->
                LazyColumn(modifier = Modifier.padding(padding)) {
                    items(composableList) {
                        CustomActivityItem(
                            it,
                            clickedHeader,
                            onCardClicked = {
                                clickedHeader = it.date.toString()
                            },
                            onRemoveClick = {
                                dialogExtra = DialogExtra(
                                    true,
                                    name = it.name,
                                    date = it.date
                                )
                            }
                        )
                    }
                }
            }

            AlertDialogInit(
                dialogTitle = "Warning!",
                dialogText = "Are you sure you want to delete this date from database? It couldn't be recovered.",
                icon = Icons.Default.Info,
                iconDescription = "Dialog info icon",
                showDialog = dialogExtra.showDialog,
                onDismissRequest = { dialogExtra = DialogExtra(false, null, null) },
                onConfirmation = {
                    val db = SQLite(this@AllDatesActivity)
                    val result = db.removeDate(dialogExtra.name, dialogExtra.date)
                    dialogExtra = DialogExtra(false, null, null)
                    composableList = db.getAllActivityDates(activityName)

                    if (result) {
                        val nav = Intent(this, MainActivity::class.java)
                        startActivity(nav)
                    }
                }
            )
        }
    }

    @Composable
    private fun CustomActivityItem(
        activity: ActivityDataClass,
        clickedHeader: String,
        onCardClicked: () -> Unit,
        onRemoveClick: () -> Unit
    ) {
        var isClicked by remember { mutableStateOf(false) }

        if (clickedHeader != activity.date.toString()) {
            isClicked = false
        }

        ActivityItem(
            headline = DateHelper.convertMillisToDate(activity.date, "dd.MM.yyyy"),

            descriptionComposable = {
                Text(
                    text = repsListToString(activity.reps),
                )
            },

            fullDescriptionComposable = {
                FilledTonalButtonWithIcon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Remove Date",
                    text = "Remove",
                    onClick = onRemoveClick
                )
            },

            isClicked = isClicked,

            onCardClick = {
                isClicked = !isClicked
                onCardClicked()
            }
        )
    }
}

