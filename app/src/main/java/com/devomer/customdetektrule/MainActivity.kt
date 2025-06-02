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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
fun AccessibilityTestComponents(modifier: Modifier) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AccessibilityComponent("Image Button") {
            Button(
                onClick = {},
                modifier = Modifier.padding(8.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.baseline_share_24),
                    colorFilter = ColorFilter.tint(Color.White),
                    contentDescription = "Paylaş"
                )
            }
        }

        AccessibilityComponent("IconButton") {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Build, contentDescription = "Ayarlar")
            }
        }



        AccessibilityComponent("TextField, OutlinedTextField") {
            TextField(
                modifier = Modifier.semantics { contentDescription = "Kullanıcı adı" },
                onValueChange = {},
                value = ""
            )
        }


        AccessibilityComponent("Clickable without onClickLabel") {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clickable() { }
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color.Red, CircleShape))

                Text("arama yap")
            }
        }

        val sliderState =
            remember { SliderState(value = 10f, valueRange = 0f..100f, onValueChangeFinished = {}) }

        AccessibilityComponent("Slider") {
            Slider(
                state = sliderState,
                modifier = Modifier.semantics { contentDescription = "Kredi yuzdesi" })
        }


        var isChecked by remember { mutableStateOf(false) }
        AccessibilityComponent("Checkbox") {
            Checkbox(checked = isChecked, onCheckedChange = {
                isChecked = it
            })
        }

        AccessibilityComponent("Radio Button") {

        }
    }
}

@Composable
fun ClickableSubComponent() {
    Text("arama yap")
}


@Composable
fun AccessibilityComponent(title: String, component: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title)

        component()
    }
}