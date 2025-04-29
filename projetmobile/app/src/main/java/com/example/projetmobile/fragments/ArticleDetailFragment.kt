package com.example.projetmobile.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.projetmobile.R
import com.example.projetmobile.models.Article
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ArticleDetailFragment : Fragment(R.layout.fragment_article_detail) {

    private lateinit var ivImage: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvOwner: TextView
    private lateinit var btnChat: Button

    private val db = FirebaseFirestore.getInstance()
    private lateinit var article: Article

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Récupère l'objet Article passé via Bundle
        article = requireArguments().getParcelable("article")!!

        // Lie les vues
        ivImage       = view.findViewById(R.id.imageDetail)
        tvTitle       = view.findViewById(R.id.tvTitleDetail)
        tvDescription = view.findViewById(R.id.tvDescriptionDetail)
        tvPrice       = view.findViewById(R.id.tvPriceDetail)
        tvCategory    = view.findViewById(R.id.tvCategoryDetail)
        tvOwner       = view.findViewById(R.id.tvOwnerDetail)
        btnChat       = view.findViewById(R.id.btnChat)

        // Affiche les données de l'article
        tvTitle.text       = article.title
        tvDescription.text = article.description
        tvPrice.text       = if (article.price > 0) "${article.price} €" else article.exchangeFor
        tvCategory.text    = article.category

        // Charge l'image si dispo
        article.images.firstOrNull()?.let {
            Glide.with(this).load(it).into(ivImage)
        } ?: ivImage.setImageResource(R.drawable.ic_image_placeholder)

        // Récupère le nom du propriétaire depuis Firestore
        db.collection("users").document(article.userId)
            .get()
            .addOnSuccessListener { doc ->
                val username = doc.getString("username") ?: "Utilisateur"
                tvOwner.text   = "Par : $username"
                btnChat.text   = "Parler avec $username"
                btnChat.setOnClickListener {
                    // Ajouter des logs pour déboguer
                    Log.d("ChatDebug", "UserId de l'article: ${article.userId}")
                    Log.d("ChatDebug", "Mon userId: ${FirebaseAuth.getInstance().currentUser?.uid}")
                    Log.d("ChatDebug", "Nom: $username")

                    // Ouvre ton écran de chat avec peerId + peerName
                    val chatFrag = ChatListFragment().apply {
                        arguments = Bundle().apply {
                            putString("peerId", article.userId)
                            putString("peerName", username)
                            putString("articleId", article.id)
                            putString("articleTitle", article.title)
                        }
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, chatFrag)
                        .addToBackStack(null)
                        .commit()
                }
            }
    }
}