package com.example.projetmobile.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projetmobile.R
import com.example.projetmobile.adapters.ConversationAdapter
import com.example.projetmobile.models.Conversation
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoConversations: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var conversationAdapter: ConversationAdapter
    private val conversationsList = mutableListOf<Conversation>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid

    // Variables pour une nouvelle conversation
    private var peerId: String? = null
    private var peerName: String? = null
    private var articleId: String? = null
    private var articleTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            peerId = it.getString("peerId")
            peerName = it.getString("peerName")
            articleId = it.getString("articleId")
            articleTitle = it.getString("articleTitle")

            // Ajouter des logs pour déboguer
            Log.d("ChatDebug", "Args - peerId: $peerId, peerName: $peerName")
            Log.d("ChatDebug", "Args - articleId: $articleId, articleTitle: $articleTitle")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewConversations)
        tvNoConversations = view.findViewById(R.id.tvNoConversations)
        progressBar = view.findViewById(R.id.progressBar)

        setupRecyclerView()

        // Si on a les infos pour une nouvelle conversation
        if (peerId != null && articleId != null && articleTitle != null) {
            Log.d("ChatDebug", "Creating or opening conversation")
            createOrOpenConversation(peerId!!, articleId!!, articleTitle!!)
        } else {
            Log.d("ChatDebug", "Loading all conversations")
            loadConversations()
        }
    }

    private fun setupRecyclerView() {
        conversationAdapter = ConversationAdapter(conversationsList)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = conversationAdapter
        }

        conversationAdapter.setOnItemClickListener { conversation ->
            // Trouver l'ID de l'autre participant
            val otherId = conversation.participants.find { it != currentUserId }

            // Récupérer le nom du destinataire
            otherId?.let { id ->
                db.collection("users").document(id)
                    .get()
                    .addOnSuccessListener { doc ->
                        val peerName = doc.getString("username") ?: "Utilisateur"

                        // Ouvrir la page de chat
                        val chatFragment = ChatFragment().apply {
                            arguments = Bundle().apply {
                                putString("conversationId", conversation.id)
                                putString("articleTitle", conversation.articleTitle)
                                putString("recipientId", otherId)
                                putString("peerName", peerName)  // Ajouter le nom de la personne
                            }
                        }

                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, chatFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                    .addOnFailureListener {
                        // En cas d'erreur, ouvrir quand même le chat mais sans le nom personnalisé
                        val chatFragment = ChatFragment().apply {
                            arguments = Bundle().apply {
                                putString("conversationId", conversation.id)
                                putString("articleTitle", conversation.articleTitle)
                                putString("recipientId", otherId)
                            }
                        }

                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, chatFragment)
                            .addToBackStack(null)
                            .commit()
                    }
            }
        }
    }

    private fun loadConversations() {
        progressBar.visibility = View.VISIBLE
        tvNoConversations.visibility = View.GONE

        if (currentUserId == null) {
            tvNoConversations.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            return
        }

        // Version simplifiée de la requête sans tri complexe pour tester
        db.collection("conversations")
            .whereArrayContains("participants", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE
                Log.d("ChatDebug", "Conversations found: ${documents.size()}")

                if (documents.isEmpty) {
                    tvNoConversations.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                conversationsList.clear()
                for (document in documents) {
                    val conversation = document.toObject(Conversation::class.java)
                    conversationsList.add(conversation)
                    Log.d("ChatDebug", "Conversation loaded: ${conversation.id}, article: ${conversation.articleTitle}")
                }

                conversationAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Log.e("ChatDebug", "Error loading conversations", e)
                Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createOrOpenConversation(recipientId: String, articleId: String, articleTitle: String) {
        progressBar.visibility = View.VISIBLE
        Log.d("ChatDebug", "Current UserID: $currentUserId")
        Log.d("ChatDebug", "Recipient ID: $recipientId")
        Log.d("ChatDebug", "Article ID: $articleId")
        Log.d("ChatDebug", "Article Title: $articleTitle")

        if (currentUserId == null) {
            Toast.makeText(context, "Vous devez être connecté", Toast.LENGTH_SHORT).show()
            return
        }

        // Créer directement une nouvelle conversation sans vérifier si elle existe
        createNewConversation(recipientId, articleId, articleTitle)
    }

    private fun createNewConversation(recipientId: String, articleId: String, articleTitle: String) {
        val participants = listOf(currentUserId!!, recipientId)
        val now = Timestamp.now()

        Log.d("ChatDebug", "Creating new conversation with participants: $participants")

        val conversation = Conversation(
            participants = participants,
            lastMessage = "Nouvelle conversation",
            lastTimestamp = now,
            articleId = articleId,
            articleTitle = articleTitle
        )

        db.collection("conversations")
            .add(conversation)
            .addOnSuccessListener { documentReference ->
                progressBar.visibility = View.GONE
                val conversationId = documentReference.id
                Log.d("ChatDebug", "Conversation created with ID: $conversationId")
                openChatFragment(conversationId, articleTitle, recipientId)
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Log.e("ChatDebug", "Error creating conversation", e)
                Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openChatFragment(conversationId: String, articleTitle: String, recipientId: String) {
        Log.d("ChatDebug", "Opening chat with conversationId: $conversationId")

        // Récupérer le nom de la personne de la base de données
        db.collection("users").document(recipientId)
            .get()
            .addOnSuccessListener { doc ->
                val peerName = doc.getString("username") ?: "Utilisateur"

                val chatFragment = ChatFragment().apply {
                    arguments = Bundle().apply {
                        putString("conversationId", conversationId)
                        putString("articleTitle", articleTitle)
                        putString("recipientId", recipientId)
                        putString("peerName", peerName)  // Ajouter le nom de la personne
                    }
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, chatFragment)
                    .addToBackStack(null)
                    .commit()
            }
            .addOnFailureListener { e ->
                // En cas d'erreur, ouvrir quand même le chat mais sans le nom personnalisé
                val chatFragment = ChatFragment().apply {
                    arguments = Bundle().apply {
                        putString("conversationId", conversationId)
                        putString("articleTitle", articleTitle)
                        putString("recipientId", recipientId)
                    }
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, chatFragment)
                    .addToBackStack(null)
                    .commit()
            }
    }
}