package com.example.adminblinkitclone.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminblinkitclone.databinding.ItemViewImageSelectionBinding

class AdapterSelectedImage(val imageUris: ArrayList<Uri>) :
    RecyclerView.Adapter<AdapterSelectedImage.SelectedImageViewHolder>() {
    inner class SelectedImageViewHolder(val binding: ItemViewImageSelectionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedImageViewHolder {
        return SelectedImageViewHolder(
            ItemViewImageSelectionBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount() = imageUris.size

    override fun onBindViewHolder(holder: SelectedImageViewHolder, position: Int) {
        val image = imageUris[position]
        holder.binding.apply {
            Glide.with(holder.itemView.context)
                .load(image)
                .override(800, 800)
                .into(ivImage)
        }
        holder.binding.ibClose.setOnClickListener {
            if (position < imageUris.size) {
                imageUris.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }
}