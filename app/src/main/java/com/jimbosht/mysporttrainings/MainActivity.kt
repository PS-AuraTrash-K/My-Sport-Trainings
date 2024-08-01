package com.jimbosht.mysporttrainings

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.glance.GlanceModifier
import com.jimbosht.mysporttrainings.ui.theme.MySportTrainingsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Main(onFABClick())
        }
    }

    private fun onFABClick(): () -> Unit = {
        val navigate = Intent(this, AddActivity::class.java)
        startActivity(navigate)
    }


    @Composable
    private fun Main(onClick: () -> Unit) {
        MySportTrainingsTheme {
            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = onClick
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Activity")
                    }
                }
            ) { padding ->
                Box(Modifier.padding(padding))
                {
                }
            }

        }
    }


    @Preview(showBackground = true)
    @Composable
    private fun DefaultPreview() {
        MySportTrainingsTheme {
            Main(onFABClick())
        }
    }
}

