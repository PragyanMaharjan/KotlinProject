package com.example.thriftclothing.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.thriftclothing.databinding.FragmentHomeBinding
import com.example.thriftclothing.ui.activity.AvailableDogAccessoryActivity
import com.example.thriftclothing.ui.activity.AvailableDogFoodActivity
import com.example.thriftclothing.ui.activity.AvailableMedicineActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnDogFood.setOnClickListener {
            startActivity(Intent(requireContext(), AvailableDogFoodActivity::class.java))
        }

        binding.btnMedicine.setOnClickListener {
            startActivity(Intent(requireContext(), AvailableMedicineActivity::class.java))
        }

        binding.btnJacket.setOnClickListener {
            startActivity(Intent(requireContext(), AvailableDogAccessoryActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
