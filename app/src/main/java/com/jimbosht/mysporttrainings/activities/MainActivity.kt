package com.jimbosht.mysporttrainings.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jimbosht.mysporttrainings.database.SQLite
import com.jimbosht.mysporttrainings.extra.*
import com.jimbosht.mysporttrainings.ui.theme.MySportTrainingsTheme

class MainActivity : ComponentActivity() {
    companion object {
        const val ACTIVITY_NAME = "ACTIVITY_NAME"
        const val IS_ADDING_EXTRA = "IS_ADDING_EXTRA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = SQLite(this)
        val list = db.getAllNewestActivities()

        setContent {
            Main(list)
        }
    }

    private fun onFABClick(): () -> Unit = {
        val navigate = Intent(this, AddActivity::class.java)
        startActivity(navigate)
    }


    @Composable
    private fun Main(list: List<ActivityDataClass>) {
        var clickedHeader by remember { mutableStateOf("") }

        MySportTrainingsTheme {
            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = onFABClick()
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Activity")
                    }
                }
            ) { padding ->
                LazyColumn(modifier = Modifier.padding(padding)) {
                    items(list) {
                        CustomActivityItem(
                            it,
                            clickedHeader,
                            onCardClick = {
                                clickedHeader = it.name
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun CustomActivityItem(
        activity: ActivityDataClass,
        clickedHeader: String,
        onCardClick: () -> Unit
    ) {
        var isClicked by remember { mutableStateOf(false) }

        if (clickedHeader != activity.name) {
            isClicked = false
        }

        ActivityItem(
            headline = activity.name,

            descriptionComposable = {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = DateHelper.convertMillisToDate(activity.date, "E, dd.MM.yyyy"),
                    )
                    Text(
                        text = repsListToString(activity.reps),
                    )
                }
            },
            additionalCentered = false,

            fullDescriptionComposable = {
                FilledTonalButtonWithIcon(
                    imageVector = Icons.Filled.List,
                    contentDescription = "Show All Dates",
                    text = "All Dates",
                    onClick = {
                        val navigate = Intent(this, AllDatesActivity::class.java)
                        navigate.putExtra(ACTIVITY_NAME, activity.name)
                        startActivity(navigate)
                    }
                )

                FilledTonalButtonWithIcon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Date",
                    text = "Add Extra Session",
                    onClick = {
                        val navigate = Intent(this, AddActivity::class.java)
                        navigate.putExtra(ACTIVITY_NAME, activity.name)
                        navigate.putExtra(IS_ADDING_EXTRA, true)
                        startActivity(navigate)
                    }
                )
            },

            isClicked = isClicked,
            onCardClick = {
                isClicked = !isClicked
                onCardClick()
            }
        )
    }
}

