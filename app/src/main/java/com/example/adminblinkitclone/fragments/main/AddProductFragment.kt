package com.example.adminblinkitclone.fragments.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.adminblinkitclone.R
import com.example.adminblinkitclone.activities.AdminMainActivity
import com.example.adminblinkitclone.adapters.AdapterSelectedImage
import com.example.adminblinkitclone.databinding.FragmentAddProductBinding
import com.example.adminblinkitclone.models.Product
import com.example.adminblinkitclone.util.Constant
import com.example.adminblinkitclone.util.Utils
import com.example.adminblinkitclone.viewmodels.AdminViewModel
import kotlinx.coroutines.launch

class AddProductFragment : Fragment() {
    private var _binding: FragmentAddProductBinding? = null
    private val binding: FragmentAddProductBinding get () = _binding!!

    private val viewModel: AdminViewModel by viewModels()

    private val imageUris: ArrayList<Uri> = arrayListOf()
    private val galleryPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Register for activity result to get the selected image from gallery
    private val getImageFromGallery = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { results ->
        val fiveImages = results.take(5)
        imageUris.clear()
        imageUris.addAll(fiveImages)

        binding.rvProductImage.adapter = AdapterSelectedImage(imageUris)
    }

    private fun openGallery() {
        getImageFromGallery.launch("image/*")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() {
        setAutoCompleteTextViews()
        binding.ibAddImages.setOnClickListener {
            requestStoragePermission()
        }

        binding.btnAddProduct.setOnClickListener {
            Utils.showDialog(requireContext(), "Uploading images...")
            val productTitle = binding.etProductTitle.text.toString()
            val productQuantity = binding.etQuantity.text.toString()
            val productUnit = binding.etProductUnit.text.toString()
            val productPrice = binding.etPrice.text.toString()
            val productStock = binding.etNumberOfStock.text.toString()
            val productCategory = binding.etProductCategory.text.toString()
            val productType = binding.etProductType.text.toString()

            if (productTitle.isEmpty() || productCategory.isEmpty() || productQuantity.isEmpty()
                || productPrice.isEmpty() || productStock.isEmpty() || productType.isEmpty()
                || productUnit.isEmpty()) {

                Utils.hideDialog()
                Utils.showToast(requireContext(), "Empty fields are not allowed")
            } else if (imageUris.isEmpty()) {
                Utils.hideDialog()
                Utils.showToast(requireContext(), "Please upload some images")
            } else {
                val product = Product(
                    productTitle = productTitle,
                    productQuantity = productQuantity.toInt(),
                    productUnit = productUnit,
                    productPrice = productPrice.toInt(),
                    productStock = productStock.toInt(),
                    productCategory = productCategory,
                    productType = productType,
                    itemCount = 0,
                    adminUid = Utils.getUserCurrentId()
                )
                saveImage(product)
            }

        }
    }

    private fun saveImage(product: Product) {
        viewModel.saveImageInDb(imageUris)
        lifecycleScope.launch {
            viewModel.isImageUploaded.collect {
                if (it) {
                    Utils.hideDialog()
                    Utils.showToast(requireContext(), "Images saved")
                    getUrls(product)

                }
            }
        }
    }

    private fun getUrls(product: Product) {
        Utils.showDialog(requireContext(), "Publishing product...")
        lifecycleScope.launch {
            viewModel.downloadUrls.collect {
                val urls = it
                product.productImageUris = urls
                saveProduct(product )
            }
        }
    }

    private fun saveProduct(product: Product){
        viewModel.saveProduct(product, {
            Utils.hideDialog()
            startActivity(Intent(requireActivity(), AdminMainActivity::class.java))
            Utils.showToast(requireContext(), it)
        },) {
            Utils.hideDialog()
            Utils.showToast(requireContext(), it)
        }
    }

    private fun setAutoCompleteTextViews() {
        val units = ArrayAdapter(requireContext(), R.layout.show_list, Constant.allUnitsOfProducts)
        val category = ArrayAdapter(requireContext(), R.layout.show_list, Constant.allProductsCategory)
        val productType = ArrayAdapter(requireContext(), R.layout.show_list, Constant.allProductType)

        binding.apply {
            etProductUnit.setAdapter(units)
            etProductCategory.setAdapter(category)
            etProductType.setAdapter(productType)
        }
    }

    private fun requestStoragePermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                galleryPermission.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
            else -> {
                galleryPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}