package com.Tomtor.groceryapp.data.repository

import com.Tomtor.groceryapp.data.api.ApiClient
import com.Tomtor.groceryapp.data.model.*

class ItemRepository {

    suspend fun addItem(listId: String, name: String, label: String = "Other"): Result<Item> {
        return try {
            val response = ApiClient.service.addItem(listId, AddItemRequest(name, label))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to add item"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteItem(listId: String, itemId: String): Result<Unit> {
        return try {
            val response = ApiClient.service.deleteItem(listId, itemId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete item"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markItem(listId: String, itemId: String, isGot: Boolean): Result<Item> {
        return try {
            val response = ApiClient.service.markItem(listId, itemId, MarkItemRequest(isGot))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to mark item"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetItems(listId: String): Result<Unit> {
        return try {
            val response = ApiClient.service.resetItems(listId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to reset items"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHistory(listId: String): Result<List<History>> {
        return try {
            val response = ApiClient.service.getHistory(listId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to get history"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editItem(listId: String, itemId: String, name: String, label: String): Result<Item> {
        return try {
            val response = ApiClient.service.editItem(listId, itemId, EditItemRequest(name, label))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to edit item"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}