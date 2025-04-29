package com.example.projetmobile.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projetmobile.R
import com.example.projetmobile.models.Message
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(private val messages: List<Message>, private val currentUserId: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    inner class SentMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tvSentMessage)
        val tvTime: TextView = itemView.findViewById(R.id.tvSentTime)
    }

    inner class ReceivedMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tvReceivedMessage)
        val tvTime: TextView = itemView.findViewById(R.id.tvReceivedTime)
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_sent, parent, false)
            SentMessageHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_received, parent, false)
            ReceivedMessageHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        // Utilisez content au lieu de text
        if (holder.itemViewType == VIEW_TYPE_SENT) {
            val sentHolder = holder as SentMessageHolder
            sentHolder.tvMessage.text = message.content
            message.timestamp?.let {
                sentHolder.tvTime.text = dateFormat.format(it.toDate())
            }
        } else {
            val receivedHolder = holder as ReceivedMessageHolder
            receivedHolder.tvMessage.text = message.content
            message.timestamp?.let {
                receivedHolder.tvTime.text = dateFormat.format(it.toDate())
            }
        }
    }

    override fun getItemCount() = messages.size
}