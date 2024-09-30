package com.example.adminblinkitclone.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.adminblinkitclone.models.Product
import com.example.adminblinkitclone.util.Utils
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow

class AdminViewModel: ViewModel() {
    private val firebaseStorageInstance = FirebaseStorage.getInstance()
    private val firebaseDatabaseInstance = FirebaseDatabase.getInstance(
        "https://blinkit-clone-b8338-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )

    private val _isImagesUploaded = MutableStateFlow(false)
    var isImageUploaded: StateFlow<Boolean> = _isImagesUploaded

    private val _downloadUrls = MutableStateFlow<ArrayList<String?>>(arrayListOf())
    var downloadUrls: StateFlow<ArrayList<String?>> = _downloadUrls

    private val _isProductSaved = MutableStateFlow(false)
    var isProductSaved: StateFlow<Boolean> = _isProductSaved

    fun saveImageInDb(imageUri: ArrayList<Uri>) {
        val downloadUrls = ArrayList<String?>()

        imageUri.forEach { uri ->
            Utils.getUserCurrentId()?.let { it1 ->
                val imageRef = firebaseStorageInstance.reference.child(it1)
                    .child("images").child(System.currentTimeMillis().toString())
                imageRef.putFile(uri).continueWithTask { imageRef.downloadUrl }
                    .addOnCompleteListener { task ->
                        downloadUrls.add(task.result.toString())

                        if (downloadUrls.size == imageUri.size) {
                            _isImagesUploaded.value = true
                            _downloadUrls.value = downloadUrls
                        }
                    }
            }
        }
    }

    fun saveProduct(product: Product, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        firebaseDatabaseInstance.getReference("Admins")
            .child("AllProducts/${product.productRandomId}").setValue(product)
            .addOnSuccessListener {
                Log.d("debugging", "51")
                firebaseDatabaseInstance.getReference("Admins")
                    .child("ProductCategory/${product.productRandomId}")
                    .setValue(product)
                    .addOnSuccessListener {
                        Log.d("debugging", "success")
                        firebaseDatabaseInstance.getReference("Admins")
                            .child("productType/${product.productRandomId}")
                            .setValue(product).addOnSuccessListener {
                                Log.d("debugging", "success")
                                onSuccess.invoke("Your product is Live")
                            }
                            .addOnFailureListener {
                                error ->
                                Log.d("debugging", "error")
                                onFailure.invoke(error.message.toString())
                            }
                    }
            }
    }

    fun fetchAllProducts(): Flow<List<Product>> = callbackFlow {
        val db = firebaseDatabaseInstance.getReference("Admins").child("AllProducts")

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                for (product in snapshot.children) {
                    val value = product.getValue(Product::class.java)
                    if (value != null) products.add(value)
                }
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        db.addValueEventListener(eventListener)
        awaitClose { db.removeEventListener(eventListener) }
    }
}