package com.example.projetmobile.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projetmobile.R
import com.example.projetmobile.models.Conversation
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ConversationAdapter(private val conversations: List<Conversation>) :
    RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

    private var onItemClickListener: ((Conversation) -> Unit)? = null
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    inner class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvConversationTitle)
        val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val conversation = conversations[position]

        // Afficher le titre de l'article
        holder.tvTitle.text = conversation.articleTitle

        // Afficher le dernier message
        holder.tvLastMessage.text = conversation.lastMessage

        // Formater et afficher l'horodatage
        conversation.lastTimestamp?.let {
            holder.tvTimestamp.text = dateFormat.format(it.toDate())
        }

        // Gestionnaire de clic
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(conversation)
        }
    }

    override fun getItemCount() = conversations.size

    fun setOnItemClickListener(listener: (Conversation) -> Unit) {
        onItemClickListener = listener
    }
}