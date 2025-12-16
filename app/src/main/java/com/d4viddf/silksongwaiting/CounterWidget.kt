package com.d4viddf.silksongwaiting

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.*
import androidx.glance.material3.ColorProviders
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.util.Locale
import java.util.concurrent.TimeUnit

class CounterWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CounterWidget()
}

class CounterWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val showHeader = prefs[booleanPreferencesKey("show_header")] ?: true
            val showTimer = prefs[booleanPreferencesKey("show_timer")] ?: true
            val showFooter = prefs[booleanPreferencesKey("show_footer")] ?: true
            val useSystemColor = prefs[booleanPreferencesKey("use_system_color")] ?: false

            val now = System.currentTimeMillis()
            val diff = now - NotificationHelper.ANNOUNCEMENT_TIME_MILLIS
            val days = if (diff >= 0) TimeUnit.MILLISECONDS.toDays(diff) else 0
            val hours = if (diff >= 0) TimeUnit.MILLISECONDS.toHours(diff) % 24 else 0
            val minutes = if (diff >= 0) TimeUnit.MILLISECONDS.toMinutes(diff) % 60 else 0
            val daysString = "$days DAYS"
            val timeString = String.format(Locale.US, "%02d H %02d M", hours, minutes)

            val myColors = if (useSystemColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ColorProviders(
                    light = dynamicLightColorScheme(context),
                    dark = dynamicDarkColorScheme(context)
                )
            } else {
                ColorProviders(
                    light = androidx.compose.material3.darkColorScheme(),
                    dark = androidx.compose.material3.darkColorScheme()
                )
            }

            GlanceTheme(colors = myColors) {
                WidgetContent(daysString, timeString, showHeader, showTimer, showFooter, useSystemColor)
            }
        }
    }

    @Composable
    private fun WidgetContent(
        days: String, time: String, showHeader: Boolean, showTimer: Boolean, showFooter: Boolean, useSystemColor: Boolean
    ) {
        val context = LocalContext.current
        val size = LocalSize.current
        val isWideMode = size.width >= 250.dp

        // --- SMOOTH TRANSITION INTENT ---
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK // Removed CLEAR_TASK to prevent cold boot
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val bgModifier = if (useSystemColor) {
            GlanceModifier.background(GlanceTheme.colors.surface).cornerRadius(24.dp)
        } else {
            GlanceModifier.background(ImageProvider(R.drawable.widget_background_gradient))
        }

        val textPrimary = if (useSystemColor) GlanceTheme.colors.onSurface else ColorProvider(Color.White)
        val textSecondary = if (useSystemColor) GlanceTheme.colors.secondary else ColorProvider(Color(0xFFB0BEC5))
        val textAccent = if (useSystemColor) GlanceTheme.colors.primary else ColorProvider(Color(0xFF87DAE4))

        Box(
            modifier = GlanceModifier.fillMaxSize().then(bgModifier).padding(16.dp).clickable(actionStartActivity(intent)),
            contentAlignment = Alignment.Center
        ) {
            if (isWideMode) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalAlignment = Alignment.CenterHorizontally, modifier = GlanceModifier.fillMaxSize()) {
                    if (showHeader) {
                        Column(horizontalAlignment = Alignment.Start) { Header(textPrimary, textAccent) }
                        if (showTimer || showFooter) Spacer(GlanceModifier.width(24.dp))
                    }
                    if (showTimer || showFooter) {
                        Column(horizontalAlignment = if (showHeader) Alignment.End else Alignment.CenterHorizontally) {
                            if (showTimer) Counter(days, time, textPrimary, textSecondary)
                            if (showTimer && showFooter) Spacer(GlanceModifier.height(4.dp))
                            if (showFooter) Footer(textPrimary)
                        }
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalAlignment = Alignment.CenterVertically) {
                    if (showHeader) {
                        Header(textPrimary, textAccent)
                        if (showTimer || showFooter) Spacer(GlanceModifier.height(12.dp))
                    }
                    if (showTimer) {
                        Counter(days, time, textPrimary, textSecondary)
                        if (showFooter) Spacer(GlanceModifier.height(8.dp))
                    }
                    if (showFooter) Footer(textPrimary)
                }
            }
        }
    }

    @Composable private fun Header(primary: ColorProvider, accent: ColorProvider) {
        Text("HOLLOW KNIGHT", style = TextStyle(color = primary, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif))
        Text("SILKSONG", style = TextStyle(color = primary, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif))
        Text("Sea of SORROW", style = TextStyle(color = accent, fontSize = 14.sp, fontWeight = FontWeight.Normal, fontFamily = FontFamily.Serif))
    }

    @Composable private fun Counter(days: String, time: String, primary: ColorProvider, secondary: ColorProvider) {
        Text(days, style = TextStyle(color = primary, fontSize = 32.sp, fontWeight = FontWeight.Bold))
        Text(time, style = TextStyle(color = secondary, fontSize = 14.sp, fontWeight = FontWeight.Bold))
    }

    @Composable private fun Footer(primary: ColorProvider) {
        Text("SINCE ANNOUNCEMENT", style = TextStyle(color = primary, fontSize = 8.sp, fontWeight = FontWeight.Normal))
    }
}