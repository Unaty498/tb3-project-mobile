package fr.emse.connectedlock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
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
import fr.emse.connectedlock.ui.login.LoginScreen
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
    var loggedIn by rememberSaveable { mutableStateOf(false) }

    if (loggedIn) {
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
                        AppDestinations.HOME -> HomeScreen()
                        AppDestinations.BADGES -> BadgesScreen()
                        AppDestinations.DOORS -> DoorsScreen()
                    }
                }
            }
        }
    } else {
        LoginScreen(onLoginClick = { loggedIn = true })
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    BADGES("Badges", Icons.Default.CreditCard),
    DOORS("Doors", Icons.Default.Lock),
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Home Screen - User Information")
    }
}

@Composable
fun BadgesScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Badges Screen")
    }
}

@Composable
fun DoorsScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Doors Screen")
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ConnectedLockTheme {
        HomeScreen()
    }
}
