package com.example.projetmobile.fragments

import android.os.Bundle
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
import com.example.projetmobile.adapters.ArticleAdapter
import com.example.projetmobile.models.Article
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CategoryArticlesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvCategoryTitle: TextView
    private lateinit var articleAdapter: ArticleAdapter
    private val articlesList = mutableListOf<Article>()

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var categoryName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            categoryName = it.getString(ARG_CATEGORY_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category_articles, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewCategoryArticles)
        progressBar = view.findViewById(R.id.progressBar)
        tvCategoryTitle = view.findViewById(R.id.tvCategoryTitle)

        categoryName?.let {
            tvCategoryTitle.text = it
        } ?: run {
            tvCategoryTitle.text = "Articles"
        }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        loadArticlesByCategory()
    }

    private fun setupRecyclerView() {
        articleAdapter = ArticleAdapter(articlesList)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = articleAdapter
        }

        articleAdapter.setOnItemClickListener { article ->
            // Naviguer vers le détail de l'article
            val bundle = Bundle().apply {
                putParcelable("article", article)
            }

            val detailFragment = ArticleDetailFragment().apply {
                arguments = bundle
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadArticlesByCategory() {
        progressBar.visibility = View.VISIBLE
        articlesList.clear()

        // Si aucune catégorie n'est spécifiée, ne rien faire
        if (categoryName == null) {
            progressBar.visibility = View.GONE
            return
        }

        // Récupérer l'ID de l'utilisateur actuel pour exclure ses articles
        val currentUserId = auth.currentUser?.uid

        // Charger les articles de la catégorie spécifiée avec une requête simplifiée
        db.collection("articles")
            .whereEqualTo("category", categoryName)
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE

                if (documents.isEmpty) {
                    Toast.makeText(context, "Aucun article trouvé dans cette catégorie", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Filtrer et trier manuellement
                val filteredArticles = documents
                    .map { it.toObject(Article::class.java) }
                    .filter { it.userId != currentUserId }
                    .sortedByDescending { it.createdAt }

                articlesList.addAll(filteredArticles)
                articleAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val ARG_CATEGORY_NAME = "category_name"

        fun newInstance(categoryName: String): CategoryArticlesFragment {
            return CategoryArticlesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY_NAME, categoryName)
                }
            }
        }
    }
}