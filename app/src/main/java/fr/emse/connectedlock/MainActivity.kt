package fr.emse.connectedlock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import fr.emse.connectedlock.data.Badge
import fr.emse.connectedlock.data.Door
import fr.emse.connectedlock.data.User
import fr.emse.connectedlock.data.sampleBadges
import fr.emse.connectedlock.data.sampleDoors
import fr.emse.connectedlock.data.sampleUser
import fr.emse.connectedlock.ui.theme.ConnectedLockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConnectedLockTheme {
                ConnectedLockApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun ConnectedLockApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentDestination) {
                    AppDestinations.HOME -> HomeScreen(user = sampleUser)
                    AppDestinations.BADGES -> BadgesScreen(badges = sampleBadges)
                    AppDestinations.DOORS -> DoorsScreen(doors = sampleDoors)
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    BADGES("Badges", Icons.Filled.CreditCard),
    DOORS("Doors", Icons.Default.Lock),
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier, user: User) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome, ${user.name}", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(user.email, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun BadgesScreen(modifier: Modifier = Modifier, badges: List<Badge>) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(badges) { badge ->
            BadgeItem(badge = badge)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun BadgeItem(badge: Badge) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.CreditCard, contentDescription = "Badge")
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = badge.type, style = MaterialTheme.typography.headlineSmall)
                Text(text = "ID: ${badge.id}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Expires: ${badge.expiryDate}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun DoorsScreen(modifier: Modifier = Modifier, doors: List<Door>) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(doors) { door ->
            DoorItem(door = door)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun DoorItem(door: Door) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Lock, contentDescription = "Door")
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = door.name, style = MaterialTheme.typography.headlineSmall)
                Text(text = "ID: ${door.id}", style = MaterialTheme.typography.bodyMedium)
                Text(text = door.location, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ConnectedLockTheme {
        HomeScreen(user = sampleUser)
    }
}
