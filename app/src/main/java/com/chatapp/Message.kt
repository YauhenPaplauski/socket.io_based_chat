package com.chatapp

class Message private constructor() {
    var type: Int = 0
    var message: String? = null
    var username: String? = null

    class Builder(private val mType: Int) {
        private var mUsername: String? = null
        private var mMessage: String? = null

        fun username(username: String): Builder {
            mUsername = username
            return this
        }

        fun message(message: String): Builder {
            mMessage = message
            return this
        }

        fun build(): Message {
            val message = Message()
            message.type = mType
            message.username = mUsername
            message.message = mMessage
            return message
        }
    }

    companion object {
        const val MY_MESSAGE = 11
        const val INCOME_MESSAGE = 22
    }
}
