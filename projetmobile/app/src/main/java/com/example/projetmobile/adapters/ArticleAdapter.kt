package com.example.projetmobile.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projetmobile.R
import com.example.projetmobile.models.Article
import com.google.firebase.firestore.FirebaseFirestore

class ArticleAdapter(private val articles: List<Article>) :
    RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    private var onItemClickListener: ((Article) -> Unit)? = null

    class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageArticle: ImageView = itemView.findViewById(R.id.imageArticle)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articles[position]

        holder.tvTitle.text = article.title
        holder.tvPrice.text = if (article.price > 0) "${article.price} €" else "Échange"

        // Chargement de l'image : si une URL existe on l'affiche, sinon placeholder
        if (article.images.isNotEmpty()) {
            Glide.with(holder.imageArticle.context)
                .load(article.images[0])
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(holder.imageArticle)
        } else {
            holder.imageArticle.setImageResource(R.drawable.ic_image_placeholder)
        }

        // Récupérer le nom d'utilisateur
        FirebaseFirestore.getInstance().collection("users")
            .document(article.userId)
            .get()
            .addOnSuccessListener { document ->
                val username = document.getString("username") ?: "Utilisateur"
                holder.tvUsername.text = "Par: $username"
            }
            .addOnFailureListener {
                holder.tvUsername.text = "Par: Utilisateur"
            }

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(article)
        }
    }

    override fun getItemCount() = articles.size

    fun setOnItemClickListener(listener: (Article) -> Unit) {
        onItemClickListener = listener
    }
}
