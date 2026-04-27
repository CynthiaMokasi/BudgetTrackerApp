package com.example.budgettrackerapp.ui.add

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.budgettrackerapp.R
import com.example.budgettrackerapp.data.*
import java.util.*
import java.util.concurrent.Executors

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

            val expense = Expense(
                title = title.text.toString(),
                amount = amount.text.toString().toDouble(),
                category = categorySpinner.selectedItem.toString(),
                date = dateField.text.toString()
            )

            Executors.newSingleThreadExecutor().execute {
                db.expenseDao().insert(expense)

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