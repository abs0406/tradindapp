package com.example.projetmobile.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projetmobile.R
import com.example.projetmobile.adapters.MessageAdapter
import com.example.projetmobile.models.Message
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var tvChatTitle: TextView

    private lateinit var messageAdapter: MessageAdapter
    private val messagesList = mutableListOf<Message>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid

    private var conversationId: String? = null
    private var recipientId: String? = null
    private var articleTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            conversationId = it.getString("conversationId")
            recipientId = it.getString("recipientId")
            articleTitle = it.getString("articleTitle")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewMessages)
        etMessage = view.findViewById(R.id.etMessage)
        btnSend = view.findViewById(R.id.btnSend)
        tvChatTitle = view.findViewById(R.id.tvChatTitle)

        // Modifier cette ligne pour utiliser peerName au lieu d'articleTitle
        val peerName = arguments?.getString("peerName")
        tvChatTitle.text = peerName ?: "Conversation"

        setupRecyclerView()
        setupSendButton()

        conversationId?.let {
            loadMessages(it)
        } ?: run {
            Toast.makeText(context, "Erreur: ID de conversation non trouvé", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messagesList, currentUserId ?: "")
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true // Pour afficher les messages du plus ancien au plus récent
            }
            adapter = messageAdapter
        }
    }

    private fun setupSendButton() {
        btnSend.setOnClickListener {
            val messageText = etMessage.text.toString().trim()

            if (messageText.isEmpty()) {
                return@setOnClickListener
            }

            if (conversationId == null || currentUserId == null || recipientId == null) {
                Toast.makeText(context, "Erreur: informations manquantes", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendMessage(conversationId!!, messageText)
        }
    }

    private fun loadMessages(conversationId: String) {
        db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots == null) return@addSnapshotListener

                messagesList.clear()
                for (doc in snapshots) {
                    val message = doc.toObject(Message::class.java)
                    messagesList.add(message)
                }

                messageAdapter.notifyDataSetChanged()

                // Défiler vers le bas pour voir le dernier message
                if (messagesList.isNotEmpty()) {
                    recyclerView.scrollToPosition(messagesList.size - 1)
                }

                // Marquer les messages comme lus
                markMessagesAsRead(conversationId)
            }
    }

    private fun sendMessage(conversationId: String, text: String) {
        val message = Message(
            senderId = currentUserId!!,
            content = text,
            timestamp = Timestamp.now(),
            read = false
        )

        // Ajouter le message à la collection "messages" de la conversation
        db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                // Effacer le champ de texte
                etMessage.setText("")

                // Mettre à jour les infos de la conversation
                updateConversationInfo(conversationId, text)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Erreur d'envoi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateConversationInfo(conversationId: String, lastMessage: String) {
        val updates = hashMapOf<String, Any>(
            "lastMessage" to lastMessage,
            "lastTimestamp" to Timestamp.now()
        )

        db.collection("conversations")
            .document(conversationId)
            .update(updates)
            .addOnFailureListener { e ->
                Toast.makeText(context, "Erreur de mise à jour: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun markMessagesAsRead(conversationId: String) {
        // Marquer comme lus tous les messages reçus (qui ne sont pas de l'utilisateur actuel)
        db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .whereEqualTo("senderId", recipientId)
            .whereEqualTo("read", false)
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()

                for (doc in documents) {
                    batch.update(doc.reference, "read", true)
                }

                if (!documents.isEmpty) {
                    batch.commit()
                }
            }
    }
}