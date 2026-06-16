package com.Tomtor.groceryapp.data.model

data class User(
    val id: String,
    val username: String
)

data class AuthResponse(
    val token: String,
    val user_id: String,
    val username: String
)

data class GroceryList(
    val id: String,
    val name: String,
    val invite_code: String,
    val created_by: String,
    val created_at: String
)

data class Item(
    val id: String,
    val list_id: String,
    val name: String,
    val added_by: String,
    val is_got: Boolean,
    val label: String = "Other",
    val created_at: String
)

data class AddItemRequest(
    val name: String,
    val label: String = "Other"
)

data class EditItemRequest(
    val name: String,
    val label: String
)
data class History(
    val id: String,
    val list_id: String,
    val user_id: String,
    val username: String,
    val action: String,
    val item_name: String,
    val timestamp: String
)

data class ListDetail(
    val list: GroceryList,
    val items: List<Item>
)

// request bodies
data class RegisterRequest(
    val username: String,
    val password: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class CreateListRequest(
    val name: String
)

data class JoinListRequest(
    val invite_code: String
)

data class MarkItemRequest(
    val is_got: Boolean
)

// websocket messages
data class WsMessage(
    val type: String,
    val payload: Any?
)

data class Member(
    val user_id: String,
    val username: String,
    val role: String,
    val joined_at: String
)

data class UpdateRoleRequest(
    val role: String
)

data class RenameListRequest(
    val name: String
)