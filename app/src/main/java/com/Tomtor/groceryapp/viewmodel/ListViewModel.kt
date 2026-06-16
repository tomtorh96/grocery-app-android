package com.Tomtor.groceryapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Tomtor.groceryapp.data.api.WebSocketClient
import com.Tomtor.groceryapp.data.model.GroceryList
import com.Tomtor.groceryapp.data.model.History
import com.Tomtor.groceryapp.data.model.Item
import com.Tomtor.groceryapp.data.model.Member
import com.Tomtor.groceryapp.data.repository.ItemRepository
import com.Tomtor.groceryapp.data.repository.ListRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class SortOrder { NAME, NAME_LABEL, NEED_TO_GET, NEED_TO_GET_LABEL }
data class ListState(
    val isLoading: Boolean = false,
    val list: GroceryList? = null,
    val items: List<Item> = emptyList(),
    val members: List<Member> = emptyList(),
    val currentUserRole: String = "guest",
    val currentUserId: String = "",
    val history: List<History> = emptyList(),
    val sortOrder: SortOrder = SortOrder.NEED_TO_GET,
    val error: String? = null
)

class ListViewModel : ViewModel() {

    private val listRepository = ListRepository()
    private val itemRepository = ItemRepository()
    private val wsClient = WebSocketClient()
    private val gson = Gson()

    private val _state = MutableStateFlow(ListState())
    val state: StateFlow<ListState> = _state

    fun loadList(listId: String, token: String, userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, currentUserId = userId)

            val result = listRepository.getList(listId)
            if (result.isSuccess) {
                val detail = result.getOrNull()!!
                _state.value = _state.value.copy(
                    isLoading = false,
                    list = detail.list,
                    items = sortItems(detail.items, _state.value.sortOrder)
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
                )
            }

            // load members and current user role
            val membersResult = listRepository.getMembers(listId)
            if (membersResult.isSuccess) {
                val members = membersResult.getOrDefault(emptyList())
                val role = members.find { it.user_id == userId }?.role ?: "guest"
                _state.value = _state.value.copy(
                    members = members,
                    currentUserRole = role
                )
            }

            // connect websocket
            wsClient.connect(listId, token)
            wsClient.messages.collect { message ->
                handleWsMessage(message.type, message.payload)
            }
        }
    }

    private fun handleWsMessage(type: String, payload: Any?) {
        val current = _state.value.items.toMutableList()
        when (type) {
            "item_added" -> {
                val map = payload as? Map<*, *>
                val itemJson = gson.toJson(map?.get("item"))
                val item = gson.fromJson(itemJson, Item::class.java)
                if (item != null && current.none { it.id == item.id }) {
                    current.add(item)
                    _state.value = _state.value.copy(items = sortItems(current, _state.value.sortOrder))
                }
            }
            "item_removed" -> {
                val map = payload as? Map<*, *>
                val itemId = map?.get("item_id") as? String
                if (itemId != null) {
                    current.removeAll { it.id == itemId }
                    _state.value = _state.value.copy(items = current)
                }
            }
            "item_marked", "item_unmarked" -> {
                val map = payload as? Map<*, *>
                val itemJson = gson.toJson(map?.get("item"))
                val item = gson.fromJson(itemJson, Item::class.java)
                if (item != null) {
                    val index = current.indexOfFirst { it.id == item.id }
                    if (index >= 0) current[index] = item
                    _state.value = _state.value.copy(items = sortItems(current, _state.value.sortOrder))
                }
            }
            "reset_all" -> {
                val reset = current.map { it.copy(is_got = false) }
                _state.value = _state.value.copy(items = reset)
            }
            "item_edited" -> {
                val map = payload as? Map<*, *>
                val itemJson = gson.toJson(map?.get("item"))
                val item = gson.fromJson(itemJson, Item::class.java)
                if (item != null) {
                    val updated = _state.value.items.map {
                        if (it.id == item.id) item else it
                    }
                    _state.value = _state.value.copy(items = sortItems(updated, _state.value.sortOrder))
                }
            }
            "member_removed" -> {
                val map = payload as? Map<*, *>
                val removedUserId = map?.get("user_id") as? String
                if (removedUserId != null) {
                    val updated = _state.value.members.filter { it.user_id != removedUserId }
                    _state.value = _state.value.copy(members = updated)
                }
            }
            "member_role_changed" -> {
                val map = payload as? Map<*, *>
                val changedUserId = map?.get("user_id") as? String
                val newRole = map?.get("role") as? String
                if (changedUserId != null && newRole != null) {
                    val updated = _state.value.members.map {
                        if (it.user_id == changedUserId) it.copy(role = newRole) else it
                    }
                    val currentRole = if (changedUserId == _state.value.currentUserId) newRole
                    else _state.value.currentUserRole
                    _state.value = _state.value.copy(members = updated, currentUserRole = currentRole)
                }
            }
            "list_renamed" -> {
                val map = payload as? Map<*, *>
                val newName = map?.get("name") as? String
                if (newName != null) {
                    _state.value = _state.value.copy(list = _state.value.list?.copy(name = newName))
                }
            }

        }
    }

    fun addItem(listId: String, name: String, label: String = "Other") {
        viewModelScope.launch {
            itemRepository.addItem(listId, name, label)
        }
    }

    fun deleteItem(listId: String, itemId: String) {
        viewModelScope.launch {
            itemRepository.deleteItem(listId, itemId)
            val updated = _state.value.items.filter { it.id != itemId }
            _state.value = _state.value.copy(items = updated)
        }
    }

    fun markItem(listId: String, itemId: String, isGot: Boolean) {
        viewModelScope.launch {
            val result = itemRepository.markItem(listId, itemId, isGot)
            if (result.isSuccess) {
                val updated = _state.value.items.map {
                    if (it.id == itemId) it.copy(is_got = isGot) else it
                }
                _state.value = _state.value.copy(items = sortItems(updated, _state.value.sortOrder))
            }
        }
    }

    fun resetItems(listId: String) {
        viewModelScope.launch {
            itemRepository.resetItems(listId)
            val reset = _state.value.items.map { it.copy(is_got = false) }
            _state.value = _state.value.copy(items = reset)
        }
    }

    fun setSortOrder(order: SortOrder) {
        _state.value = _state.value.copy(
            sortOrder = order,
            items = sortItems(_state.value.items, order)
        )
    }

    fun loadHistory(listId: String) {
        viewModelScope.launch {
            val result = itemRepository.getHistory(listId)
            if (result.isSuccess) {
                _state.value = _state.value.copy(history = result.getOrDefault(emptyList()))
            }
        }
    }

    fun loadMembers(listId: String) {
        viewModelScope.launch {
            val result = listRepository.getMembers(listId)
            if (result.isSuccess) {
                val members = result.getOrDefault(emptyList())
                val currentId = _state.value.currentUserId
                val role = members.find { it.user_id == currentId }?.role ?: "guest"
                _state.value = _state.value.copy(
                    members = members,
                    currentUserRole = role
                )
            }
        }
    }

    fun setCurrentUserId(userId: String) {
        _state.value = _state.value.copy(currentUserId = userId)
    }

    fun removeMember(listId: String, userId: String) {
        viewModelScope.launch {
            val result = listRepository.removeMember(listId, userId)
            if (result.isSuccess) {
                val updated = _state.value.members.filter { it.user_id != userId }
                _state.value = _state.value.copy(members = updated)
            }
        }
    }

    fun updateMemberRole(listId: String, userId: String, role: String) {
        viewModelScope.launch {
            val result = listRepository.updateMemberRole(listId, userId, role)
            if (result.isSuccess) {
                val updated = _state.value.members.map {
                    if (it.user_id == userId) it.copy(role = role) else it
                }
                _state.value = _state.value.copy(members = updated)
            }
        }
    }

    fun renameList(listId: String, name: String) {
        viewModelScope.launch {
            val result = listRepository.renameList(listId, name)
            if (result.isSuccess) {
                _state.value = _state.value.copy(
                    list = _state.value.list?.copy(name = name)
                )
            }
        }
    }

    private fun sortItems(items: List<Item>, order: SortOrder): List<Item> {
        return when (order) {
            SortOrder.NAME -> items.sortedBy { it.name.lowercase() }
            SortOrder.NAME_LABEL -> items.sortedWith(
                compareBy({ it.label.lowercase() }, { it.name.lowercase() })
            )
            SortOrder.NEED_TO_GET -> items.sortedBy { it.is_got }
            SortOrder.NEED_TO_GET_LABEL -> items.sortedWith(
                compareBy({ it.is_got }, { it.label.lowercase() }, { it.name.lowercase() })
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        wsClient.disconnect()
    }

    fun editItem(listId: String, itemId: String, name: String, label: String) {
        viewModelScope.launch {
            val result = itemRepository.editItem(listId, itemId, name, label)
            if (result.isSuccess) {
                val updated = _state.value.items.map {
                    if (it.id == itemId) result.getOrNull()!! else it
                }
                _state.value = _state.value.copy(items = sortItems(updated, _state.value.sortOrder))
            }
        }
    }
}