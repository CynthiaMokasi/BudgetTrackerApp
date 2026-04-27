package com.example.budgettrackerapp.ui.view

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.budgettrackerapp.R
import com.example.budgettrackerapp.data.AppDatabase
import java.util.concurrent.Executors

class ViewExpensesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_view_expenses, container, false)

        val containerLayout = view.findViewById<LinearLayout>(R.id.container)
        val emptyText = view.findViewById<TextView>(R.id.tvEmpty)

        val db = AppDatabase.getDatabase(requireContext())

        fun loadExpenses() {

            Executors.newSingleThreadExecutor().execute {

                val expenses = db.expenseDao().getAll()

                activity?.runOnUiThread {

                    containerLayout.removeAllViews()

                    if (expenses.isEmpty()) {
                        emptyText.visibility = View.VISIBLE
                        return@runOnUiThread
                    }

                    emptyText.visibility = View.GONE

                    for (expense in expenses) {

                        val itemLayout = LinearLayout(requireContext()).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(24, 24, 24, 24)
                            setBackgroundColor(Color.LTGRAY)
                        }

                        val expenseText = TextView(requireContext()).apply {
                            text =
                                "${expense.title}\nR${expense.amount}\n${expense.category}\n${expense.date}"
                            textSize = 16f
                            setTextColor(Color.BLACK)
                        }

                        val deleteButton = Button(requireContext()).apply {
                            text = "DELETE"
                            setBackgroundColor(Color.RED)
                            setTextColor(Color.WHITE)

                            setOnClickListener {

                                Executors.newSingleThreadExecutor().execute {
                                    db.expenseDao().delete(expense)

                                    activity?.runOnUiThread {
                                        Toast.makeText(
                                            context,
                                            "Expense deleted",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        loadExpenses()
                                    }
                                }
                            }
                        }

                        itemLayout.addView(expenseText)
                        itemLayout.addView(deleteButton)

                        containerLayout.addView(itemLayout)
                    }
                }
            }
        }

        loadExpenses()

        return view
    }
}