package com.example.adminblinkitclone.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.adminblinkitclone.databinding.ItemViewProductCategoriesBinding
import com.example.adminblinkitclone.util.Category

class CategoryAdapter(
    private val categoryList: ArrayList<Category>,
    val onCategory: (Category) -> Unit)
    : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    inner class CategoryViewHolder(val binding: ItemViewProductCategoriesBinding)
        : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CategoryViewHolder(ItemViewProductCategoriesBinding
            .inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = categoryList.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.binding.ivCategoryImage.setImageResource(categoryList[position].image)
        holder.binding.tvCategoryTitle.text = categoryList[position].category

        holder.itemView.setOnClickListener { onCategory.invoke(categoryList[position]) }
    }
}