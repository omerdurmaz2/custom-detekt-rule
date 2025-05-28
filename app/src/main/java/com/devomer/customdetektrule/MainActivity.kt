package com.devomer.customdetektrule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.devomer.customdetektrule.ui.theme.CustomDetektRuleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CustomDetektRuleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AccessibilityTestComponents(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AccessibilityTestComponents(modifier: Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AccessibilityComponent("Clickable without onClickLabel") {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.Red)
                    .clickable(onClickLabel = "kayıt butonu") { }
            )
        }

        AccessibilityComponent("TextField, OutlinedTextField without label or contentDescription") {
            TextField(
                onValueChange = {},
                value = "",
                label = {
                    Text("label text")
                }
            )
        }

        AccessibilityComponent("Button with only image content") {
            Button(onClick = {}) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_background),
                    contentDescription = "deneme buton"
                )
            }
        }

        AccessibilityComponent("Button with only image content") {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Build, contentDescription = null)
            }
        }
    }
}


@Composable
fun AccessibilityComponent(title: String, component: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title)

        component()
    }
}