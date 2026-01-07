package fr.emse.connectedlock

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import fr.emse.connectedlock.data.AccessRule
import fr.emse.connectedlock.data.Badge
import fr.emse.connectedlock.data.Door
import fr.emse.connectedlock.data.User
import fr.emse.connectedlock.data.sampleUser
import fr.emse.connectedlock.ui.emulation.EmulateBadgeScreen
import fr.emse.connectedlock.ui.login.LoginScreen
import fr.emse.connectedlock.ui.theme.ConnectedLockTheme
import fr.emse.connectedlock.viewmodel.MainViewModel
import fr.emse.connectedlock.utils.BadgeActivationHelper

class MainActivity : ComponentActivity(), NfcAdapter.ReaderCallback {

    private val viewModel: MainViewModel by viewModels()
    private var nfcAdapter: NfcAdapter? = null
    private var badgeToWrite: Badge? = null

    fun isNfcEnabled(): Boolean {
        return nfcAdapter?.isEnabled == true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        enableEdgeToEdge()
        setContent {
            ConnectedLockTheme {
                ConnectedLockApp(viewModel = viewModel)
            }
        }
    }

    override fun onTagDiscovered(tag: Tag?) {
        tag?.let {
            badgeToWrite?.let { badge ->
                val success = BadgeActivationHelper.writeToNfcTag(it, badge.badgeNumber)
                runOnUiThread {
                    if (success) {
                        Toast.makeText(this, "Badge wrote successfully!", Toast.LENGTH_LONG).show()
                        viewModel.activateBadge(badge.id)
                        stopNfcWrite()
                    } else {
                        Toast.makeText(this, "Failed to write badge.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    fun startNfcWrite(badge: Badge) {
        if (!isNfcEnabled()) {
            Toast.makeText(this, "NFC is not enabled", Toast.LENGTH_LONG).show()
            return
        }
        badgeToWrite = badge
        val options = Bundle()
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
        nfcAdapter?.enableReaderMode(this, this,
            NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V or
            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            options)
        Toast.makeText(this, "Tap badge to write", Toast.LENGTH_SHORT).show()
    }

    private fun stopNfcWrite() {
        nfcAdapter?.disableReaderMode(this)
        badgeToWrite = null
    }

    override fun onPause() {
        super.onPause()
        stopNfcWrite()
    }
}

@Composable
fun ConnectedLockApp(viewModel: MainViewModel) {
    if (viewModel.isAuthenticated.value) {
        MainContent(viewModel)
    } else {
        LoginScreen(
            onLoginClick = { username, password ->
                viewModel.login(username, password)
            },
            errorMessage = viewModel.loginError.value
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(viewModel: MainViewModel) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val isRefreshing by viewModel.isRefreshing
    val isActivating by viewModel.isActivating

    var showActivateDialog by remember { mutableStateOf(false) }
    var selectedBadge by remember { mutableStateOf<Badge?>(null) }
    var showEmulationScreen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Local variable for smart cast
    val badgeToEmulate = selectedBadge

    if (showEmulationScreen && badgeToEmulate != null) {
         EmulateBadgeScreen(
             badge = badgeToEmulate,
             onStopEmulation = {
                 showEmulationScreen = false
                 selectedBadge = null
             }
         )
         // Return immediately to show only the emulation screen overlay
         return
    }

    if (showActivateDialog && badgeToEmulate != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showActivateDialog = false },
            title = { Text("Activate Badge") },
            text = { Text("This badge is inactive. Do you want to activate it?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showActivateDialog = false
                        Log.d("BadgeActivation", "Activating badge: ${badgeToEmulate.badgeNumber}")
                        Log.d("BadgeActivation", "Badge type: ${badgeToEmulate.type}")
                        if (badgeToEmulate.type.equals("PHYSICAL", true)) {
                            (context as? MainActivity)?.startNfcWrite(badgeToEmulate)
                        } else {
                            // Use phone as tag (Emulation)
                            showEmulationScreen = true
                            selectedBadge = badgeToEmulate // Ensure context keeps track
                        }
                    }
                ) {
                    Text("Activate")
                }
            },
            dismissButton = {
                Row {
                    if (badgeToEmulate.type.equals("PHYSICAL", true)) {
                        TextButton(onClick = {
                            showActivateDialog = false
                            Toast.makeText(context, "Debug: Simulating NFC Write Success", Toast.LENGTH_SHORT).show()
                            viewModel.activateBadge(badgeToEmulate.id)
                        }) {
                            Text("Debug: Simulate")
                        }
                    }
                    TextButton(onClick = { showActivateDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isActivating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f)
                    .clickable(enabled = false) {}, // Block clicks
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

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
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.padding(innerPadding)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (currentDestination) {
                            AppDestinations.HOME -> viewModel.user.value?.let { HomeScreen(user = it, onLogout = { viewModel.logout() }) }
                            AppDestinations.BADGES -> BadgesScreen(
                                badges = viewModel.badges.value,
                                onBadgeClick = { badge ->
                                    val activity = context as? MainActivity
                                    if (activity?.isNfcEnabled() != true) {
                                        Toast.makeText(context, "NFC is required. Please enable it.", Toast.LENGTH_LONG).show()
                                    } else {
                                        if (badge.type.equals("Mobile", true)) {
                                            selectedBadge = badge
                                            showEmulationScreen = true
                                        } else if (!badge.physicallyMapped) {
                                            selectedBadge = badge
                                            showActivateDialog = true
                                        }
                                    }
                                }
                            )
                            AppDestinations.DOORS -> DoorsScreen(
                                doors = viewModel.doors.value,
                                accessRules = viewModel.accessRules.value
                            )
                        }
                    }
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
fun HomeScreen(modifier: Modifier = Modifier, user: User, onLogout: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome, ${user.firstName}", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        user.email?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
        Spacer(modifier = Modifier.height(8.dp))

        if (user.role.isNotEmpty()) {
            Text("Role: ${user.role}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Account activity : Red pill for inactive and green for active
        Surface(
            color = if (user.active) Color(0xFF4CAF50) else Color(0xFFF44336),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = if (user.active) "Active" else "Inactive",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onLogout) {
            Text("Logout")
        }
    }
}

@Composable
fun BadgesScreen(modifier: Modifier = Modifier, badges: List<Badge>, onBadgeClick: (Badge) -> Unit) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(badges) { badge ->
            BadgeItem(badge = badge, onClick = { onBadgeClick(badge) })
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun BadgeItem(badge: Badge, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.CreditCard, contentDescription = "Badge")
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = badge.badgeNumber, style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = badge.type,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(text = "ID: ${badge.id}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Expires: ${badge.expiryDate}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun DoorsScreen(modifier: Modifier = Modifier, doors: List<Door>, accessRules: List<AccessRule>) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(doors) { door ->
            val doorRules = accessRules.filter { it.doorId == door.id }
            DoorItem(door = door, rules = doorRules)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun DoorItem(door: Door, rules: List<AccessRule>) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Lock, contentDescription = "Door")
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = door.name, style = MaterialTheme.typography.headlineSmall)
                    Text(text = "ID: ${door.id}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Lieu: ${door.location}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Access Times:", style = MaterialTheme.typography.labelLarge)
                if (rules.isEmpty()) {
                     Text("No access rules found.", style = MaterialTheme.typography.bodySmall)
                } else {
                    rules.forEach { rule ->
                        rule.timeSlots.forEach { slot ->
                             Text(
                                text = "${slot.dayOfWeek}: ${slot.startTime} - ${slot.endTime}",
                                style = MaterialTheme.typography.bodySmall
                             )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ConnectedLockTheme {
        HomeScreen(user = sampleUser, onLogout = {})
    }
}
