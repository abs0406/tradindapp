package com.example.projetmobile.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.projetmobile.R
import com.example.projetmobile.adapters.CategoryCardAdapter

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar

    // Liste des catégories (même que dans AddArticleFragment)
    private val categories = listOf("Shoes", "Cars", "Watch", "Clothes", "Tech","Bag")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewCategories)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        progressBar = view.findViewById(R.id.progressBar)

        setupRecyclerView()

        swipeRefresh.setOnRefreshListener {
            setupRecyclerView()
            swipeRefresh.isRefreshing = false
        }
    }

    private fun setupRecyclerView() {
        val categoryAdapter = CategoryCardAdapter(categories) { category ->
            // Navigation vers la liste des articles de cette catégorie
            val categoryArticlesFragment = CategoryArticlesFragment.newInstance(category)

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, categoryArticlesFragment)
                .addToBackStack(null)
                .commit()
        }

        recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2) // Affichage en grille de 2 colonnes
            adapter = categoryAdapter
        }
    }
}