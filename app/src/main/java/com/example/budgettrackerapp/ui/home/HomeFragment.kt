package com.example.budgettrackerapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.budgettrackerapp.R
import com.example.budgettrackerapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddExpense.setOnClickListener {
            navigateSafely(R.id.navigation_add)
        }

        binding.btnViewExpenses.setOnClickListener {
            navigateSafely(R.id.navigation_view)
        }

        binding.btnTotals.setOnClickListener {
            navigateSafely(R.id.navigation_totals)
        }

        binding.btnGoals.setOnClickListener {
            navigateSafely(R.id.navigation_goals)
        }
    }

    private fun navigateSafely(destinationId: Int) {
        try {
            findNavController().navigate(destinationId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}