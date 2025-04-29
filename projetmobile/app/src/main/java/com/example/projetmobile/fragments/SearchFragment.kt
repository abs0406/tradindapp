package com.example.projetmobile.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
import java.util.Locale

class SearchFragment : Fragment() {

    private lateinit var etSearch: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoResults: TextView
    private lateinit var articleAdapter: ArticleAdapter
    private val articlesList = mutableListOf<Article>()
    private lateinit var db: FirebaseFirestore
    private var lastQuery: String = ""
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etSearch    = view.findViewById(R.id.etSearch)
        recyclerView= view.findViewById(R.id.recyclerViewSearchResults)
        progressBar = view.findViewById(R.id.progressBar)
        tvNoResults = view.findViewById(R.id.tvNoResults)

        db = FirebaseFirestore.getInstance()
        setupRecyclerView()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString().trim()
                lastQuery = text
                if (text.isBlank()) {
                    articlesList.clear()
                    articleAdapter.notifyDataSetChanged()
                    tvNoResults.visibility = View.GONE
                } else {
                    searchArticles(text)
                }
            }
        })
    }

    private fun setupRecyclerView() {
        articleAdapter = ArticleAdapter(articlesList)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = articleAdapter
        }
        articleAdapter.setOnItemClickListener { article ->
            // Navigation vers le détail de l'article
            val details = ArticleDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("article", article)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, details)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun searchArticles(query: String) {
        progressBar.visibility = View.VISIBLE
        tvNoResults.visibility = View.GONE
        articlesList.clear()

        val filter = query.lowercase(Locale.getDefault())
        val thisQuery = filter

        var base: Query = db.collection("articles")
        currentUserId?.let { uid -> base = base.whereNotEqualTo("userId", uid) }

        base.get()
            .addOnSuccessListener { snap ->
                if (thisQuery != lastQuery.lowercase(Locale.getDefault())) return@addOnSuccessListener
                progressBar.visibility = View.GONE
                val results = snap.documents.map { doc ->
                    doc.toObject(Article::class.java)!!.apply { id = doc.id }
                }.filter { it.title.lowercase(Locale.getDefault()).contains(filter) }

                if (results.isEmpty()) {
                    tvNoResults.visibility = View.VISIBLE
                } else {
                    articlesList.addAll(results)
                    articleAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
