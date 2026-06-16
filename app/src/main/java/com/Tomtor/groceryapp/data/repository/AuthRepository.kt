package com.Tomtor.groceryapp.data.repository

import com.Tomtor.groceryapp.data.api.ApiClient
import com.Tomtor.groceryapp.data.local.TokenStore
import com.Tomtor.groceryapp.data.model.LoginRequest
import com.Tomtor.groceryapp.data.model.RegisterRequest

class AuthRepository(private val tokenStore: TokenStore) {

    suspend fun register(username: String, password: String): Result<Unit> {
        return try {
            val response = ApiClient.service.register(RegisterRequest(username, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                ApiClient.setToken(body.token)
                tokenStore.save(body.token, body.user_id, body.username)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(username: String, password: String): Result<Unit> {
        return try {
            val response = ApiClient.service.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                ApiClient.setToken(body.token)
                tokenStore.save(body.token, body.user_id, body.username)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        ApiClient.setToken(null)
        tokenStore.clear()
    }
}