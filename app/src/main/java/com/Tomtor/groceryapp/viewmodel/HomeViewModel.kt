package com.Tomtor.groceryapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Tomtor.groceryapp.data.model.GroceryList
import com.Tomtor.groceryapp.data.repository.ListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeState(
    val isLoading: Boolean = false,
    val lists: List<GroceryList> = emptyList(),
    val error: String? = null
)

class HomeViewModel : ViewModel() {

    private val repository = ListRepository()

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    init {
        loadLists()
    }

    fun loadLists() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = repository.getLists()
            _state.value = if (result.isSuccess) {
                _state.value.copy(isLoading = false, lists = result.getOrDefault(emptyList()))
            } else {
                _state.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun createList(name: String) {
        viewModelScope.launch {
            val result = repository.createList(name)
            if (result.isSuccess) {
                loadLists()
            } else {
                _state.value = _state.value.copy(error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun joinList(inviteCode: String) {
        viewModelScope.launch {
            val result = repository.joinList(inviteCode)
            if (result.isSuccess) {
                loadLists()
            } else {
                _state.value = _state.value.copy(error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun leaveList(listId: String) {
        viewModelScope.launch {
            val result = repository.leaveList(listId)
            if (result.isSuccess) {
                loadLists()
            } else {
                _state.value = _state.value.copy(error = result.exceptionOrNull()?.message)
            }
        }
    }
}