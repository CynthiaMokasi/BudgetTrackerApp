package com.example.budgettrackerapp.ui.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.budgettrackerapp.R
import com.example.budgettrackerapp.data.AppDatabase

class GoalsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_goals, container, false)

        val etMin = view.findViewById<EditText>(R.id.etMin)
        val etMax = view.findViewById<EditText>(R.id.etMax)
        val result = view.findViewById<TextView>(R.id.tvResult)
        val btn = view.findViewById<Button>(R.id.btnCheck)

        val db = AppDatabase.getDatabase(requireContext())

        btn.setOnClickListener {

            Thread {

                val total = db.expenseDao().getAll().sumOf { it.amount }

                val min = etMin.text.toString().toDoubleOrNull() ?: 0.0
                val max = etMax.text.toString().toDoubleOrNull() ?: 0.0

                requireActivity().runOnUiThread {

                    result.text = when {
                        total < min -> "Below minimum spending"
                        total > max -> "Exceeded maximum!"
                        else -> "Within budget"
                    }
                }

            }.start()
        }

        return view
    }
}