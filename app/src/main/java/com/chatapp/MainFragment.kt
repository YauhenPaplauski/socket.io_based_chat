package com.chatapp

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.fragment_main.*
import org.json.JSONException
import org.json.JSONObject

class MainFragment : Fragment() {
    private lateinit var mAdapter: MessageAdapter
    private var mUsername: String = ""
    private var mSocket: Socket? = null
    private var isConnected = false
    private val events = arrayListOf<Pair<String, Emitter.Listener>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_main, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mAdapter = MessageAdapter(requireContext())
    }

    override fun onResume() {
        super.onResume()
        mSocket = ChatApplication.instance.socket?.let {
            for (item in events)
                it.on(item.first, item.second)
            it.connect()
        }
    }

    override fun onPause() {
        super.onPause()
        mSocket?.let {
            it.disconnect()
            for (item in events)
                it.off(item.first, item.second)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        messagesList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }

        messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptSend()
                return@setOnEditorActionListener true
            }
            false
        }
        nicknameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mUsername = s.toString()
            }

            override fun afterTextChanged(s: Editable) {}
        })

        btnSend.setOnClickListener { attemptSend() }
        initEvents()
    }

    private fun initEvents() {
        events.apply {
            add(Pair(Socket.EVENT_CONNECT, onConnect))
            add(Pair(Socket.EVENT_DISCONNECT, onDisconnect))
            add(Pair(Socket.EVENT_CONNECT_ERROR, onConnectError))
            add(Pair(Socket.EVENT_CONNECT_TIMEOUT, onConnectError))
            add(Pair(MESSAGE, onNewMessageListener))
        }
    }

    private val onConnect = Emitter.Listener {
        activity?.let {
            it.runOnUiThread {
                if (!isConnected) {
                    toast(R.string.connect)
                    isConnected = true
                }
            }
        }
    }

    private val onDisconnect = Emitter.Listener {
        activity?.runOnUiThread {
            Log.i(TAG, getString(R.string.disconnected))
            isConnected = false
            toast(R.string.disconnect)
        }
    }

    private val onConnectError = Emitter.Listener {
        activity?.runOnUiThread {
            Log.e(TAG, getString(R.string.error_connecting))
            toast(R.string.error_connect)
        }
    }

    private val onNewMessageListener = Emitter.Listener { args ->
        activity?.runOnUiThread(Runnable {
            val data = args[0] as JSONObject
            val username: String
            val message: String
            try {
                username = data.getString(USER)
                message = data.getString(MESSAGE)
            } catch (e: JSONException) {
                Log.e(TAG, e.message)
                return@Runnable
            }

            addMessage(username, message)
        })
    }

    private fun addMessage(username: String, message: String) {
        mAdapter.addMessage(
            Message.Builder(getMsgType(username)).username(username).message(message).build()
        )
        scrollToBottom()
    }

    private fun getMsgType(msgNick: String) =
        if (msgNick == mUsername) Message.MY_MESSAGE else Message.INCOME_MESSAGE

    private fun attemptSend() {
        if (mUsername.isEmpty()) {
            toast(R.string.enter_name)
            return
        }
        if (mSocket == null) return
        mSocket?.let { if (!it.connected()) return }

        val message = messageInput.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(message)) {
            messageInput.requestFocus()
            return
        }

        messageInput.setText(EMPTY_STRING)
        addMessage(mUsername, message)

        mSocket?.emit(MESSAGE, prepareSendMessageJson(mUsername, message))
    }

    private fun prepareSendMessageJson(nickName: String, message: String) =
        JSONObject().apply {
            put(MESSAGE, message)
            put(USER, nickName)
        }

    private fun scrollToBottom() =
        messagesList.scrollToPosition(mAdapter.itemCount - 1)

    companion object {
        private val TAG = MainFragment::class.java.simpleName
    }
}

