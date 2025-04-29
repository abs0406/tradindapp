package com.example.projetmobile.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projetmobile.R

class CategoryCardAdapter(
    private val categories: List<String>,
    private val onCategoryClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryCardAdapter.CategoryViewHolder>() {

    // Couleurs pour chaque catégorie
    private val categoryColors = mapOf(
        "Shoes" to "#FF5722",  // Orange profond
        "Cars" to "#2196F3",   // Bleu
        "Watch" to "#9C27B0",  // Violet
        "Clothes" to "#4CAF50", // Vert
        "Tech" to "#607D8B"   , // Bleu-gris
        "Bag" to "#FFC107"     // Jaune/Ambre

    )

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val categoryBackground: RelativeLayout = itemView.findViewById(R.id.categoryBackground)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_card, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.tvCategoryName.text = category

        // Appliquer la couleur de fond
        val backgroundColor = categoryColors[category] ?: "#CCCCCC"
        holder.categoryBackground.setBackgroundColor(Color.parseColor(backgroundColor))

        // Gérer le clic sur la catégorie
        holder.itemView.setOnClickListener {
            onCategoryClick(category)
        }
    }

    override fun getItemCount() = categories.size
}