package com.chatapp

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.layout_msg.view.*
import kotlinx.android.synthetic.main.layout_my_msg.view.message
import java.util.*

class MessageAdapter(context: Context) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    private val mUsernameColors: IntArray = context.resources.getIntArray(R.array.username_colors)
    private val mMessages = ArrayList<Message>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == Message.MY_MESSAGE)
            MyMessageVH(infateView(parent, R.layout.layout_my_msg))
        else
            MessageVH(infateView(parent, R.layout.layout_msg))
    }

    private fun infateView(parent: ViewGroup, layoutRes: Int): View = LayoutInflater
        .from(parent.context)
        .inflate(layoutRes, parent, false)

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) =
        viewHolder.bind(mMessages[position])

    override fun getItemCount() = mMessages.size

    override fun getItemViewType(position: Int) = mMessages[position].type

    fun addMessage(msg: Message) = mMessages.add(msg)

    abstract inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(msg: Message) {
            setMessage(msg.message)
            setUsername(msg.username)
        }

        protected open fun setUsername(username: String?) {}

        private fun setMessage(message: String?) {
            itemView.message?.let { it.text = message }
        }
    }

    inner class MyMessageVH(itemView: View) : ViewHolder(itemView)

    inner class MessageVH(itemView: View) : ViewHolder(itemView) {
        override fun setUsername(username: String?) {
            itemView.nickName.let { tv ->
                tv.text = itemView.context.getString(R.string.nickname_placeholder, username)
                username?.let { tv.setTextColor(getUsernameColor(it)) }
            }
        }

        private fun getUsernameColor(username: String): Int {
            var hash = 7
            var i = 0
            val len = username.length
            while (i < len) {
                hash = username.codePointAt(i) + (hash shl 4) - hash
                i++
            }
            val index = Math.abs(hash % mUsernameColors.size)
            return mUsernameColors[index]
        }
    }
}
