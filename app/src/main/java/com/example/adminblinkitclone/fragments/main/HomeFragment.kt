package com.example.adminblinkitclone.fragments.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.adminblinkitclone.adapters.CategoryAdapter
import com.example.adminblinkitclone.adapters.ProductAdapter
import com.example.adminblinkitclone.databinding.FragmentHomeBinding
import com.example.adminblinkitclone.util.Category
import com.example.adminblinkitclone.util.Constant
import com.example.adminblinkitclone.viewmodels.AdminViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private val viewModel: AdminViewModel by viewModels()
    private val adapterProduct = ProductAdapter()
    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding get () = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() {
        binding.rvCategories.adapter = CategoryAdapter(Constant.getCategoryList, ::onCategoryClicked)
        getAllProducts("All")
    }

    private fun getAllProducts(category: String) {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        lifecycleScope.launch {
            viewModel.fetchAllProducts(category).collect {
                if (it.isEmpty()) {
                    binding.rvProducts.visibility = View.GONE
                    binding.tvNoProductAdded.visibility = View.VISIBLE
                } else {
                    binding.rvProducts.visibility = View.VISIBLE
                    binding.tvNoProductAdded.visibility = View.GONE
                }

                binding.rvProducts.adapter = adapterProduct
                adapterProduct.differ.submitList(it)
                binding.shimmerViewContainer.visibility = View.GONE
            }
        }
    }

    private fun onCategoryClicked(categories: Category) {
        getAllProducts(categories.category)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}