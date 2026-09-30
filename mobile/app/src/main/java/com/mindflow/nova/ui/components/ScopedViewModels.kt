package com.mindflow.nova.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

/**
 * Los ViewModel que se pidan dentro de [content] viven solo mientras este
 * bloque esté en pantalla y se destruyen al salir. Sin esto quedarían vivos
 * toda la vida de la Activity (la app no usa una librería de navegación que
 * los limpie), y cada lección abierta dejaría el suyo ocupando memoria.
 */
@Composable
fun ScopedViewModels(content: @Composable () -> Unit) {
    val owner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }

    DisposableEffect(owner) {
        onDispose { owner.viewModelStore.clear() }
    }

    CompositionLocalProvider(LocalViewModelStoreOwner provides owner, content = content)
}
