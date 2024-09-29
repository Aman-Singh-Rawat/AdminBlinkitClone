package com.example.adminblinkitclone.fragments.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.adminblinkitclone.R
import com.example.adminblinkitclone.adapters.CategoryAdapter
import com.example.adminblinkitclone.databinding.FragmentHomeBinding
import com.example.adminblinkitclone.util.Constant

class HomeFragment : Fragment() {
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
        binding.rvCategories.adapter = CategoryAdapter(Constant.getCategoryList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}