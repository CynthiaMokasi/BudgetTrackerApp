package com.example.budgettrackerapp.ui.view

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.content.FileProvider
import com.example.budgettrackerapp.R
import com.example.budgettrackerapp.data.AppDatabase
import com.example.budgettrackerapp.ui.add.ReceiptViewActivity
import java.io.File
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

                        val dateText = java.text.SimpleDateFormat("d/M/yyyy", java.util.Locale.getDefault()).format(java.util.Date(expense.timestamp))

                        val expenseText = TextView(requireContext()).apply {
                            text =
                                "${expense.description ?: ""}\nR${expense.amount}\n${expense.category}\n${dateText}"
                            textSize = 16f
                            setTextColor(Color.BLACK)
                        }

                        // Add receipt image if available
                        if (!expense.receiptUri.isNullOrBlank()) {
                            try {
                                val uri = if (expense.receiptUri.startsWith("file://")) {
                                    // Local file - use FileProvider
                                    val file = File(expense.receiptUri.removePrefix("file://"))
                                    if (file.exists()) {
                                        FileProvider.getUriForFile(
                                            requireContext(),
                                            "${requireContext().packageName}.fileprovider",
                                            file
                                        )
                                    } else {
                                        Uri.parse(expense.receiptUri)
                                    }
                                } else {
                                    Uri.parse(expense.receiptUri)
                                }
                                
                                val receiptImage = ImageView(requireContext()).apply {
                                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200)
                                    scaleType = ImageView.ScaleType.CENTER_CROP
                                    setImageURI(uri)
                                    contentDescription = "Receipt thumbnail"
                                    setBackgroundColor(Color.BLACK)
                                }
                                receiptImage.setOnClickListener {
                                    val intent = Intent(requireContext(), ReceiptViewActivity::class.java)
                                    intent.putExtra("receipt_uri", uri.toString())
                                    startActivity(intent)
                                }
                                itemLayout.addView(receiptImage)
                            } catch (ex: Exception) {
                                // Log receipt error but continue
                                android.util.Log.e("ViewExpenses", "Error loading receipt: ${ex.message}")
                            }
                        }

                        itemLayout.addView(expenseText)

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