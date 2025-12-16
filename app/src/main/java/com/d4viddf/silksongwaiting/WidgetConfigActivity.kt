package com.d4viddf.silksongwaiting

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.* // Standard Compose M3 for the Activity
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.launch

class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Get Widget ID
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            // Use Dynamic Colors for the Config Screen itself (Standard Compose)
            val context = LocalContext.current
            val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            val colorScheme = if (dynamicColor) dynamicDarkColorScheme(context) else darkColorScheme()

            MaterialTheme(colorScheme = colorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ConfigScreen()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ConfigScreen() {
        // Default State
        var showHeader by remember { mutableStateOf(true) }
        var showTimer by remember { mutableStateOf(true) }
        var showFooter by remember { mutableStateOf(true) }
        var useSystemColor by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(true) }

        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        // --- LOAD SAVED OPTIONS (Fix for "Re-edit") ---
        LaunchedEffect(Unit) {
            try {
                val manager = GlanceAppWidgetManager(context)
                // Important: Get the specific GlanceId for this Widget ID
                val glanceId = manager.getGlanceIdBy(appWidgetId)

                // Fetch the existing state
                val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)

                // Update UI toggles
                showHeader = prefs[booleanPreferencesKey("show_header")] ?: true
                showTimer = prefs[booleanPreferencesKey("show_timer")] ?: true
                showFooter = prefs[booleanPreferencesKey("show_footer")] ?: true
                useSystemColor = prefs[booleanPreferencesKey("use_system_color")] ?: false
            } catch (e: Exception) {
                // If new widget, defaults stay true
            } finally {
                isLoading = false
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Widget Settings") })
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Preview", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(16.dp))

                // Live Preview Component
                WidgetPreviewCard(showHeader, showTimer, showFooter, useSystemColor)

                Spacer(modifier = Modifier.height(32.dp))

                Text("Display", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))

                SwitchRow("Show Logo / Header", showHeader) { showHeader = it }
                SwitchRow("Show Timer", showTimer) { showTimer = it }
                SwitchRow("Show 'Since' Label", showFooter) { showFooter = it }
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                SwitchRow("Use System Colors", useSystemColor) { useSystemColor = it }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        scope.launch {
                            saveWidgetState(context, showHeader, showTimer, showFooter, useSystemColor)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Apply Changes")
                }
            }
        }
    }

    @Composable
    fun WidgetPreviewCard(header: Boolean, timer: Boolean, footer: Boolean, systemColor: Boolean) {
        // Simulate Widget Colors using Activity Theme
        val bgColor = if (systemColor) MaterialTheme.colorScheme.background else Color(0xFF010005)
        val textPrimary = if (systemColor) MaterialTheme.colorScheme.onSurface else Color.White
        val textSecondary = if (systemColor) MaterialTheme.colorScheme.secondary else Color(0xFFB0BEC5)
        val textAccent = if (systemColor) MaterialTheme.colorScheme.primary else Color(0xFF87DAE4)

        val bgMod = if (systemColor) Modifier.background(bgColor) else Modifier.background(Brush.verticalGradient(listOf(Color(0xFF010005), Color(0xFF12323D), Color(0xFF010005))))

        Box(
            modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(24.dp))
                .then(bgMod).border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(0.3f), RoundedCornerShape(24.dp)).padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (header) {
                    Text("HOLLOW KNIGHT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textPrimary, fontFamily = FontFamily.Serif)
                    Text("SILKSONG", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary, fontFamily = FontFamily.Serif)
                    Text("SEA of SORROW", fontSize = 14.sp, color = textAccent, fontFamily = FontFamily.Serif)
                    if (timer || footer) Spacer(Modifier.height(12.dp))
                }
                if (timer) {
                    Text("1 DAYS", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    Text("00 H 00 M", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                    if (footer) Spacer(Modifier.height(8.dp))
                }
                if (footer) Text("SINCE ANNOUNCEMENT", fontSize = 8.sp, color = textPrimary.copy(0.7f))
            }
        }
    }

    @Composable
    fun SwitchRow(text: String, checked: Boolean, onChange: (Boolean) -> Unit) {
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text, style = MaterialTheme.typography.bodyLarge)
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }

    private suspend fun saveWidgetState(context: android.content.Context, header: Boolean, timer: Boolean, footer: Boolean, systemColor: Boolean) {
        val manager = GlanceAppWidgetManager(context)
        val glanceId = manager.getGlanceIdBy(appWidgetId)

        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[booleanPreferencesKey("show_header")] = header
            prefs[booleanPreferencesKey("show_timer")] = timer
            prefs[booleanPreferencesKey("show_footer")] = footer
            prefs[booleanPreferencesKey("use_system_color")] = systemColor
        }
        CounterWidget().update(context, glanceId)

        val result = Intent().apply { putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId) }
        setResult(Activity.RESULT_OK, result)
        finish()
    }
}