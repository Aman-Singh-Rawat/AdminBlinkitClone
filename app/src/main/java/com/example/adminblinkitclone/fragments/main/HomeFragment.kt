package com.example.adminblinkitclone.fragments.main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.util.Util
import com.example.adminblinkitclone.R
import com.example.adminblinkitclone.adapters.CategoryAdapter
import com.example.adminblinkitclone.adapters.ProductAdapter
import com.example.adminblinkitclone.databinding.EditProductLayoutBinding
import com.example.adminblinkitclone.databinding.FragmentHomeBinding
import com.example.adminblinkitclone.models.Product
import com.example.adminblinkitclone.util.Category
import com.example.adminblinkitclone.util.Constant
import com.example.adminblinkitclone.util.Utils
import com.example.adminblinkitclone.viewmodels.AdminViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private val viewModel: AdminViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding get () = _binding!!
    private val adapterProduct = ProductAdapter(::onEditButtonClicked)

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

        binding.etSearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                adapterProduct.filter.filter(query)
            }

            override fun afterTextChanged(s: Editable?) {}

        })
    }

    private fun getAllProducts(category: String) {
        // Check if binding is initialized
        if (_binding == null) return

        binding.shimmerViewContainer.visibility = View.VISIBLE

        // Use lifecycleScope with viewLifecycleOwner to respect fragment lifecycle
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.fetchAllProducts(category).collect { products ->
                // Check if binding is still initialized before using it
                if (_binding == null) return@collect

                if (products.isEmpty()) {
                    binding.rvProducts.visibility = View.GONE
                    binding.tvNoProductAdded.visibility = View.VISIBLE
                } else {
                    binding.rvProducts.visibility = View.VISIBLE
                    binding.tvNoProductAdded.visibility = View.GONE
                }
                binding.rvProducts.adapter = adapterProduct
                adapterProduct.differ.submitList(products)
                adapterProduct.originalList = products as ArrayList<Product>
                binding.shimmerViewContainer.visibility = View.GONE
            }
        }
    }


    private fun onCategoryClicked(categories: Category) {
        getAllProducts(categories.category)
    }

    private fun onEditButtonClicked(product: Product) {
        val editProduct = EditProductLayoutBinding.inflate(LayoutInflater.from(requireContext()))
        editProduct.apply {
            etProductTitle.setText(product.productTitle)
            etQuantity.setText(product.productQuantity.toString())
            etProductUnit.setText(product.productUnit)
            etPrice.setText(product.productPrice.toString())
            etNumberOfStock.setText(product.productStock.toString())
            etQuantity.setText(product.productQuantity.toString())
            etProductType.setText(product.productType)
        }
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(editProduct.root)
            .create()

        alertDialog.show()
        editProduct.btnEditProduct.setOnClickListener {
            editProduct.apply {
                etProductTitle.isEnabled = true
                etQuantity.isEnabled = true
                etProductUnit.isEnabled = true
                etPrice.isEnabled = true
                etNumberOfStock.isEnabled = true
                etProductCategory.isEnabled = true
                etProductType.isEnabled = true
            }
        }
        editProduct.btnSaveProduct.setOnClickListener {
            lifecycleScope.launch {
                product.productTitle = editProduct.etProductTitle.text.toString()
                product.productQuantity = editProduct.etQuantity.text.toString().toInt()
                product.productUnit = editProduct.etProductUnit.text.toString()
                product.productPrice = editProduct.etPrice.text.toString().toInt()
                product.productStock = editProduct.etNumberOfStock.text.toString().toInt()
                product.productCategory = editProduct.etProductCategory.text.toString()
                product.productType = editProduct.etProductType.text.toString()
                viewModel.savingUpdateProducts(product)
            }
            Utils.showToast(requireContext(), "Saved Changes..!")
            alertDialog.dismiss()
        }
        setAutoCompleteTextViews(editProduct)
    }

    private fun setAutoCompleteTextViews(editProduct: EditProductLayoutBinding) {
        val units = ArrayAdapter(requireContext(), R.layout.show_list, Constant.allUnitsOfProducts)
        val category = ArrayAdapter(requireContext(), R.layout.show_list, Constant.allProductsCategory)
        val productType = ArrayAdapter(requireContext(), R.layout.show_list, Constant.allProductType)

        binding.apply {
            editProduct.etProductUnit.setAdapter(units)
            editProduct.etProductCategory.setAdapter(category)
            editProduct.etProductType.setAdapter(productType)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}