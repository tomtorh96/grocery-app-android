package com.Tomtor.groceryapp.data.api

import android.util.Log
import com.google.gson.Gson
import com.Tomtor.groceryapp.data.model.WsMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketClient {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val gson = Gson()

    private val _messages = MutableSharedFlow<WsMessage>(extraBufferCapacity = 64)
    val messages: SharedFlow<WsMessage> = _messages

    fun connect(listId: String, token: String) {
        val request = Request.Builder()
            .url("${BuildConfig.WS_URL}/lists/$listId/ws")
            .addHeader("Authorization", "Bearer $token")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "connected to list $listId")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val message = gson.fromJson(text, WsMessage::class.java)
                    _messages.tryEmit(message)
                } catch (e: Exception) {
                    Log.e("WebSocket", "failed to parse message: $text")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "connection failed: ${t.message}")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "disconnected: $reason")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "leaving")
        webSocket = null
    }
}