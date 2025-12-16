package com.d4viddf.silksongwaiting

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.d4viddf.silksongwaiting.ui.theme.AppTypography
import com.d4viddf.silksongwaiting.ui.theme.SilkSongScheme
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)

        setContent {
            MaterialTheme(
                colorScheme = SilkSongScheme,
                typography = AppTypography
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
                    CounterScreen()
                }
            }
        }
    }
}

// --- Background Colors ---
val BgBorder = Color(0xFF010005)
val BgMiddle = Color(0xFF05141B)
val BgInnerMiddle = Color(0xFF12323D)
val BgCenter = Color(0xFF435559)

// --- Text Colors ---
val SeaOfSorrowColor = Color(0xFF87DAE4)

// --- Custom Fonts ---
val TrajanFont = FontFamily(
    Font(R.font.trajan, FontWeight.Normal),
    Font(R.font.trajan, FontWeight.Bold)
)

@Composable
fun CounterScreen() {
    val context = LocalContext.current
    var timeElapsed by remember { mutableStateOf("Loading...") }

    // --- Permissions ---
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasPermission = isGranted
            if (isGranted) {
                NotificationHelper.showCountUpNotification(context)
                Toast.makeText(context, "Notification Launched", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Permission needed", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // --- Timer Logic ---
    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            val diff = now - NotificationHelper.ANNOUNCEMENT_TIME_MILLIS
            if (diff >= 0) {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60

                val timeString = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
                timeElapsed = "$days DAYS\n$timeString"
            } else {
                timeElapsed = "SOON"
            }
            delay(1000)
        }
    }

    // --- UI Layout ---
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f  to BgBorder,
                        0.15f to BgMiddle,
                        0.35f to BgInnerMiddle,
                        0.5f  to BgCenter,
                        0.65f to BgInnerMiddle,
                        0.85f to BgMiddle,
                        1.0f  to BgBorder
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Detect Orientation
        val isLandscape = maxWidth > maxHeight

        if (isLandscape) {
            // --- LANDSCAPE LAYOUT (Row: Left [Logo+Buttons] | Right [Counter]) ---
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Side: Header + Buttons
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AppHeader()

                    Spacer(Modifier.height(32.dp)) // Separation between Logo and Buttons

                    ActionButtons(
                        context = context,
                        hasPermission = hasPermission,
                        onPermissionRequest = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )
                }

                Spacer(Modifier.width(32.dp))

                // Right Side: Just the Counter (Big and centered)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AppCounter(timeElapsed)
                }
            }
        } else {
            // --- PORTRAIT LAYOUT (Vertical Stack) ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppHeader()
                AppCounter(timeElapsed)
                ActionButtons(
                    context = context,
                    hasPermission = hasPermission,
                    onPermissionRequest = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
            }
        }
    }
}

// --- REUSABLE COMPONENTS ---

@Composable
fun AppHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "HOLLOW KNIGHT",
            style = MaterialTheme.typography.titleMedium,
            fontFamily = TrajanFont,
            color = Color.White,
            letterSpacing = 2.sp
        )
        Text(
            text = "SILKSONG",
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = TrajanFont,
            fontSize = 42.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sea of SORROW",
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = TrajanFont,
            color = SeaOfSorrowColor,
            fontSize = 28.sp
        )
    }
}

@Composable
fun AppCounter(timeElapsed: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = timeElapsed,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 56.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "SINCE ANNOUNCEMENT",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.6f),
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun ActionButtons(
    context: android.content.Context,
    hasPermission: Boolean,
    onPermissionRequest: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // ROW: Notification & Widget Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Notification Button
            Button(
                onClick = {
                    if (hasPermission) {
                        NotificationHelper.showCountUpNotification(context)
                        Toast.makeText(context, "Notification Launched", Toast.LENGTH_SHORT).show()
                    } else {
                        onPermissionRequest()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BgInnerMiddle,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Notification")
            }

            // 2. Add Widget Button
            Button(
                onClick = {
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val myProvider = ComponentName(context, CounterWidgetReceiver::class.java)

                    if (appWidgetManager.isRequestPinAppWidgetSupported) {
                        val successIntent = Intent("com.d4viddf.silksongwaiting.WIDGET_PINNED")
                        successIntent.setPackage(context.packageName)

                        val successPendingIntent = android.app.PendingIntent.getBroadcast(
                            context,
                            0,
                            successIntent,
                            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                        )

                        appWidgetManager.requestPinAppWidget(myProvider, null, successPendingIntent)
                    } else {
                        Toast.makeText(context, "Widget pinning not supported", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BgInnerMiddle,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Widget")
            }
        }

        // Watch Trailer Button
        OutlinedButton(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, "https://youtu.be/qDFEeeLy6ws".toUri())
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Watch Teaser")
        }
    }
}