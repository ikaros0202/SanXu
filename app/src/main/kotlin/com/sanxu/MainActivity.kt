package com.sanxu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanxu.domain.repository.ThemeChoice
import com.sanxu.ui.SanxuTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity: ComponentActivity() {
    private val model: WorkspaceViewModel by viewModels()
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycle.addObserver(LifecycleEventObserver { _,event -> if(event==Lifecycle.Event.ON_START) model.foreground() })
        setContent {
            val settings by model.settings.collectAsStateWithLifecycle()
            val dark=when(settings.theme) { ThemeChoice.SYSTEM->isSystemInDarkTheme(); ThemeChoice.LIGHT->false; ThemeChoice.DARK->true }
            SideEffect {
                WindowCompat.getInsetsController(window,window.decorView).apply {
                    isAppearanceLightStatusBars=!dark
                    isAppearanceLightNavigationBars=!dark
                }
            }
            SanxuTheme(dark) { SanxuApp(model) }
        }
    }
}
