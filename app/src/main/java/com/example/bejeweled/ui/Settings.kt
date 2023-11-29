package com.example.bejeweled.ui

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Insert
import com.example.bejeweled.R
import com.example.bejeweled.data.ScoreboardDetails
import com.example.bejeweled.ui.navigation.NavigationDestination

object SettingsDestination : NavigationDestination {
    override val route = "settings"
    override val titleRes = R.string.settings_title
}
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    scoreboardDetails: ScoreboardDetails,
    onValueChange: (ScoreboardDetails) -> Unit = {},
    enabled: Boolean = true
) {

    var notificationEnabled by remember { mutableStateOf(true) }
    var themeMode by remember { mutableIntStateOf(0) }

    val themeModes = listOf("Light", "Dark", "System Default")

    Scaffold(


    ) {
        LazyColumn(
            modifier = modifier.background(color = Color(0xFFE5E5E5))
            ) {
            item {
                    InsertNameForm(
                        scoreboardDetails = scoreboardDetails,
                        onValueChange = onValueChange,
                        enabled = enabled
                    )
            }
            item {
                SettingsItem("Notifications") {
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = { notificationEnabled = it }
                    )
                }
            }
            item {
                SettingsItem("Theme Mode") {
                    Text(
                        text = themeModes[themeMode],
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                themeMode = (themeMode + 1) % themeModes.size
                            }
                    )
                    }
                }
            }
        }
    }



@Composable
fun SettingsItem(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsertNameForm(
    scoreboardDetails: ScoreboardDetails,
    modifier: Modifier = Modifier,
    onValueChange: (ScoreboardDetails) -> Unit = {},
    enabled: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        TextField(
            value = scoreboardDetails.name,
            onValueChange = { onValueChange(scoreboardDetails.copy(name = it)) },
            label = { "name" },
            placeholder = { Text("Enter your name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true
        )

    }

}

