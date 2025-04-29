package com.example.projetmobile.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projetmobile.LoginActivity
import com.example.projetmobile.R
import com.example.projetmobile.adapters.ArticleAdapter
import com.example.projetmobile.models.Article
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnLogout: Button
    private lateinit var articleAdapter: ArticleAdapter
    private val articlesList = mutableListOf<Article>()
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvUsername = view.findViewById(R.id.tvUsername)
        tvEmail = view.findViewById(R.id.tvEmail)
        recyclerView = view.findViewById(R.id.recyclerViewMyArticles)
        btnLogout = view.findViewById(R.id.btnLogout)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Vérifier si l'utilisateur est connecté
        val currentUser = auth.currentUser
        if (currentUser == null) {
            navigateToLogin()
            return
        }

        // Afficher l'email de l'utilisateur
        tvEmail.text = currentUser.email

        // Récupérer le nom d'utilisateur
        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username")
                    tvUsername.text = username ?: "Utilisateur"
                }
            }

        setupRecyclerView()
        loadUserArticles(currentUser.uid)

        // Bouton de déconnexion
        btnLogout.setOnClickListener {
            auth.signOut()
            navigateToLogin()
        }
    }

    private fun setupRecyclerView() {
        articleAdapter = ArticleAdapter(articlesList)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = articleAdapter
        }
    }

    private fun loadUserArticles(userId: String) {
        db.collection("articles")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                articlesList.clear()

                for (document in documents) {
                    val article = document.toObject(Article::class.java)
                    articlesList.add(article)
                }

                articleAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToLogin() {
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}