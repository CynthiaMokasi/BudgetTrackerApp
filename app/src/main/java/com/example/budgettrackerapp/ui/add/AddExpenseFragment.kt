package com.example.budgettrackerapp.ui.add

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.budgettrackerapp.R
import com.example.budgettrackerapp.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_add_expense, container, false)

        val title = view.findViewById<EditText>(R.id.etTitle)
        val amount = view.findViewById<EditText>(R.id.etAmount)

        val categorySpinner = view.findViewById<Spinner>(R.id.spinnerCategory)
        val dateField = view.findViewById<EditText>(R.id.etDate)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        val db = AppDatabase.getDatabase(requireContext())
        val badgeRepository = BadgeRepository.getInstance(requireContext())

        val categories = arrayOf("Food", "Transport", "Entertainment", "Bills", "Other")
        categorySpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        dateField.isFocusable = false
        dateField.isClickable = true

        dateField.setOnClickListener {
            val cal = Calendar.getInstance()

            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    dateField.setText("$d/${m + 1}/$y")
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnSave.setOnClickListener {
            val desc = title.text.toString().takeIf { it.isNotBlank() }
            val amt = amount.text.toString().toDoubleOrNull() ?: 0.0
            val category = categorySpinner.selectedItem.toString()

            val timestamp = try {
                val txt = dateField.text.toString()
                if (txt.isBlank()) System.currentTimeMillis()
                else SimpleDateFormat("d/M/yyyy", Locale.getDefault()).parse(txt)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }

            val expense = Expense(
                amount = amt,
                category = category,
                timestamp = timestamp,
                description = desc
            )

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    db.expenseDao().insert(expense)

                    // Award "First Step" badge if this is the first expense
                    val expenseCount = db.expenseDao().getAll().size
                    if (expenseCount == 1) {
                        badgeRepository.awardBadge(
                            Badge(name = "First Step", description = "Added your first expense")
                        )
                    }

                    // Award "Consistent Logger" badge on every expense added
                    badgeRepository.awardBadge(
                        Badge(name = "Consistent Logger", description = "Logged spending activity")
                    )
                }

                activity?.runOnUiThread {
                    Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
                    title.setText("")
                    amount.setText("")
                    dateField.setText("")
                    categorySpinner.setSelection(0)
                }
            }
        }

        return view
    }
}