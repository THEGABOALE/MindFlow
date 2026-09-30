package com.mindflow.nova.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindflow.nova.data.group.GroupRepository
import com.mindflow.nova.data.group.JoinGroupResult
import com.mindflow.nova.data.group.RemoteGroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JoinGroupState(
    /** True mientras el backend valida el código. */
    val isLoading: Boolean = false,
    /** Por qué se rechazó el último código, o null. */
    val errorMessage: String? = null,
    /** True cuando el estudiante ya quedó matriculado en la sala. */
    val joined: Boolean = false
)

/** Coordina la pantalla del código de sala: manda el código y guarda si entró o por qué no. */
class JoinGroupViewModel(
    private val repository: GroupRepository = RemoteGroupRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(JoinGroupState())
    val state: StateFlow<JoinGroupState> = _state.asStateFlow()

    fun join(code: String) {
        if (code.isBlank() || _state.value.isLoading) return

        _state.value = JoinGroupState(isLoading = true)

        viewModelScope.launch {
            _state.value = when (val result = repository.joinByCode(code)) {
                JoinGroupResult.Joined -> JoinGroupState(joined = true)
                is JoinGroupResult.Failed -> JoinGroupState(errorMessage = result.message)
            }
        }
    }

    /** Al volver a escribir se quita el error del intento anterior. */
    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
