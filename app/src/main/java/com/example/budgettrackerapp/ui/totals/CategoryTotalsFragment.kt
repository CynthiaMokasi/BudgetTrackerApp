package com.example.budgettrackerapp.ui.totals

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.budgettrackerapp.R
import com.example.budgettrackerapp.data.AppDatabase
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import java.util.concurrent.Executors

class CategoryTotalsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_category_totals, container, false)

        val tv = view.findViewById<TextView>(R.id.tvTotals)
        val pieChart = view.findViewById<PieChart>(R.id.pieChart)

        val db = AppDatabase.getDatabase(requireContext())

        Executors.newSingleThreadExecutor().execute {

            val food = db.expenseDao().getTotalByCategory("Food") ?: 0.0
            val transport = db.expenseDao().getTotalByCategory("Transport") ?: 0.0
            val entertainment = db.expenseDao().getTotalByCategory("Entertainment") ?: 0.0
            val bills = db.expenseDao().getTotalByCategory("Bills") ?: 0.0
            val other = db.expenseDao().getTotalByCategory("Other") ?: 0.0

            activity?.runOnUiThread {

                tv.text = getString(
                    R.string.category_totals,
                    food,
                    transport,
                    entertainment,
                    bills,
                    other
                )

                val entries = ArrayList<PieEntry>()

                if (food > 0) entries.add(PieEntry(food.toFloat(), "Food"))
                if (transport > 0) entries.add(PieEntry(transport.toFloat(), "Transport"))
                if (entertainment > 0) entries.add(PieEntry(entertainment.toFloat(), "Entertainment"))
                if (bills > 0) entries.add(PieEntry(bills.toFloat(), "Bills"))
                if (other > 0) entries.add(PieEntry(other.toFloat(), "Other"))

                val dataSet = PieDataSet(entries, "Expenses")

                dataSet.colors = listOf(
                    Color.BLUE,
                    Color.GREEN,
                    Color.MAGENTA,
                    Color.RED,
                    Color.CYAN
                )

                val data = PieData(dataSet)
                data.setValueTextSize(14f)
                data.setValueTextColor(Color.WHITE)

                pieChart.data = data
                pieChart.description.isEnabled = false
                pieChart.setUsePercentValues(true)
                pieChart.centerText = "Spending"
                pieChart.setCenterTextSize(18f)
                pieChart.animateY(1000)

                pieChart.invalidate() // refresh
            }
        }

        return view
    }
}