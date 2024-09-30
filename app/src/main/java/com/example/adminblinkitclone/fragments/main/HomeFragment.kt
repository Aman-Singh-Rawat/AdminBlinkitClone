package com.example.adminblinkitclone.fragments.main

import android.os.Bundle
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
    private val adapterProduct = ProductAdapter(::onEditButtonClicked)
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
                product.apply {
                    productTitle = editProduct.etProductTitle.text.toString()
                    productQuantity = editProduct.etQuantity.text.toString().toInt()
                    productUnit = editProduct.etProductUnit.text.toString()
                    productPrice = editProduct.etPrice.text.toString().toInt()
                    productStock = editProduct.etNumberOfStock.text.toString().toInt()
                    productCategory = editProduct.etProductCategory.text.toString()
                    productType = editProduct.etProductType.text.toString()
                }
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