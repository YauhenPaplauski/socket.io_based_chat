package com.chatapp

import android.app.Application
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

class ChatApplication : Application() {
    companion object {
        lateinit var instance: ChatApplication
    }

    var socket: Socket

    init {
        instance = this
        try {
            socket = IO.socket(CHAT_SERVER_URL)
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }

    }
}
