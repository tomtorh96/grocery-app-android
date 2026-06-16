package com.Tomtor.groceryapp.data.repository

import com.Tomtor.groceryapp.data.api.ApiClient
import com.Tomtor.groceryapp.data.model.*

class ListRepository {

    suspend fun getLists(): Result<List<GroceryList>> {
        return try {
            val response = ApiClient.service.getLists()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to get lists"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getList(id: String): Result<ListDetail> {
        return try {
            val response = ApiClient.service.getList(id)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get list"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createList(name: String): Result<GroceryList> {
        return try {
            val response = ApiClient.service.createList(CreateListRequest(name))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create list"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinList(inviteCode: String): Result<GroceryList> {
        return try {
            val response = ApiClient.service.joinList(JoinListRequest(inviteCode))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Invalid invite code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMembers(listId: String): Result<List<Member>> {
        return try {
            val response = ApiClient.service.getMembers(listId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to get members"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeMember(listId: String, userId: String): Result<Unit> {
        return try {
            val response = ApiClient.service.removeMember(listId, userId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Failed to remove member"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMemberRole(listId: String, userId: String, role: String): Result<Unit> {
        return try {
            val response = ApiClient.service.updateMemberRole(listId, userId, UpdateRoleRequest(role))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Failed to update role"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renameList(listId: String, name: String): Result<Unit> {
        return try {
            val response = ApiClient.service.renameList(listId, RenameListRequest(name))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Failed to rename list"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leaveList(listId: String): Result<Unit> {
        return try {
            val response = ApiClient.service.leaveList(listId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Failed to leave list"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}