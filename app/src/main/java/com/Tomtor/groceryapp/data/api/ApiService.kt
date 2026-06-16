package com.Tomtor.groceryapp.data.api

import com.Tomtor.groceryapp.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // lists
    @POST("lists")
    suspend fun createList(@Body request: CreateListRequest): Response<GroceryList>

    @GET("lists")
    suspend fun getLists(): Response<List<GroceryList>>

    @GET("lists/{id}")
    suspend fun getList(@Path("id") id: String): Response<ListDetail>

    @POST("lists/join")
    suspend fun joinList(@Body request: JoinListRequest): Response<GroceryList>

    // items
    @POST("lists/{id}/items")
    suspend fun addItem(
        @Path("id") listId: String,
        @Body request: AddItemRequest
    ): Response<Item>

    @DELETE("lists/{id}/items/{itemId}")
    suspend fun deleteItem(
        @Path("id") listId: String,
        @Path("itemId") itemId: String
    ): Response<Unit>

    @PATCH("lists/{id}/items/{itemId}")
    suspend fun markItem(
        @Path("id") listId: String,
        @Path("itemId") itemId: String,
        @Body request: MarkItemRequest
    ): Response<Item>

    @POST("lists/{id}/reset")
    suspend fun resetItems(@Path("id") listId: String): Response<Unit>

    // history
    @GET("lists/{id}/history")
    suspend fun getHistory(@Path("id") listId: String): Response<List<History>>

    // members
    @GET("lists/{id}/members")
    suspend fun getMembers(@Path("id") listId: String): Response<List<Member>>

    @DELETE("lists/{id}/members/{userId}")
    suspend fun removeMember(
        @Path("id") listId: String,
        @Path("userId") userId: String
    ): Response<Unit>

    @PATCH("lists/{id}/members/{userId}")
    suspend fun updateMemberRole(
        @Path("id") listId: String,
        @Path("userId") userId: String,
        @Body request: UpdateRoleRequest
    ): Response<Unit>

    @PATCH("lists/{id}")
    suspend fun renameList(
        @Path("id") listId: String,
        @Body request: RenameListRequest
    ): Response<Unit>

    @DELETE("lists/{id}/leave")
    suspend fun leaveList(@Path("id") listId: String): Response<Unit>

    @PUT("lists/{id}/items/{itemId}")
    suspend fun editItem(
        @Path("id") listId: String,
        @Path("itemId") itemId: String,
        @Body request: EditItemRequest
    ): Response<Item>
}