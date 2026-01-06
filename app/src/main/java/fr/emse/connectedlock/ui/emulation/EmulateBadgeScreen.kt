package fr.emse.connectedlock.ui.emulation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.emse.connectedlock.data.Badge
import fr.emse.connectedlock.service.BadgeEmulationState

@Composable
fun EmulateBadgeScreen(badge: Badge, onStopEmulation: () -> Unit) {

    // Activate emulation when this screen is displayed
    DisposableEffect(Unit) {
        BadgeEmulationState.activeBadgeId = badge.badgeNumber
        BadgeEmulationState.isEmulating = true
        onDispose {
            BadgeEmulationState.activeBadgeId = null
            BadgeEmulationState.isEmulating = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Emulating Badge",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = badge.type,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = badge.badgeNumber,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Hold your phone near the reader to open the door.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onStopEmulation) {
            Text("Stop Emulation")
        }
    }
}

