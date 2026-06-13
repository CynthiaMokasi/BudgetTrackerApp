package com.example.budgettrackerapp.ui.totals

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.budgettrackerapp.R
import com.example.budgettrackerapp.data.CategorySpend
import com.example.budgettrackerapp.data.BudgetLimit
import com.example.budgettrackerapp.ui.viewmodel.ExpenseViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class CategoryTotalsFragment : Fragment() {

    private val viewModel: ExpenseViewModel by viewModels()
    private val categories = listOf("Food", "Transport", "Entertainment", "Bills", "Other")
    private var latestLimits: List<BudgetLimit> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_category_totals, container, false)

        val tv = view.findViewById<TextView>(R.id.tvTotals)
        val spinner = view.findViewById<Spinner>(R.id.spinnerRange)
        val barChart = view.findViewById<BarChart>(R.id.barChart)

        // Spinner setup
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf("Last Week", "Last Month", "Last Year"))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        fun loadForRange(range: String) {
            val now = System.currentTimeMillis()
            val start = when (range) {
                "Last Week" -> now - 7L * 24 * 60 * 60 * 1000
                "Last Month" -> now - 30L * 24 * 60 * 60 * 1000
                "Last Year" -> now - 365L * 24 * 60 * 60 * 1000
                else -> now - 30L * 24 * 60 * 60 * 1000
            }

            var latestSpending: List<CategorySpend> = emptyList()

            viewModel.getSpendingByCategory(start, now).observe(viewLifecycleOwner) { list ->
                latestSpending = list
                renderChart(latestSpending, barChart, tv, latestLimits)
                renderLimits(latestLimits, barChart)
            }

            viewModel.getBudgetLimits().observe(viewLifecycleOwner) { limits ->
                latestLimits = limits
                renderChart(latestSpending, barChart, tv, latestLimits)
                renderLimits(limits, barChart)
            }
        }

        spinner.setSelection(1)
        spinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, viewSel: View?, position: Int, id: Long) {
                val range = parent?.getItemAtPosition(position) as String
                loadForRange(range)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        // initial load
        loadForRange("Last Month")

        // set marker tooltip
        barChart.marker = com.example.budgettrackerapp.ui.totals.CategoryMarkerView(requireContext())

        return view
    }

    private fun renderChart(list: List<CategorySpend>, barChart: BarChart, tv: TextView, limits: List<BudgetLimit>) {
        val totalsMap = categories.associateWith { c -> list.firstOrNull { it.category == c }?.total ?: 0.0 }

        tv.text = getString(R.string.category_totals,
            totalsMap["Food"] ?: 0.0,
            totalsMap["Transport"] ?: 0.0,
            totalsMap["Entertainment"] ?: 0.0,
            totalsMap["Bills"] ?: 0.0,
            totalsMap["Other"] ?: 0.0)

        val limitMap = limits.associateBy { it.category }
        val rangeEntries = ArrayList<BarEntry>()
        val spendEntries = ArrayList<BarEntry>()
        for ((i, c) in categories.withIndex()) {
            val total = totalsMap[c] ?: 0.0
            val limit = limitMap[c]
            val minAmount = limit?.minAmount ?: 0.0
            val maxAmount = limit?.maxAmount ?: 0.0
            val yVals = when {
                maxAmount > minAmount -> floatArrayOf(minAmount.toFloat(), (maxAmount - minAmount).toFloat())
                maxAmount > 0.0 -> floatArrayOf(maxAmount.toFloat())
                else -> floatArrayOf(0f)
            }

            rangeEntries.add(BarEntry(i.toFloat(), yVals))
            spendEntries.add(BarEntry(i.toFloat(), total.toFloat()))
        }

        val rangeSet = BarDataSet(rangeEntries, "Budget Range")
        rangeSet.setDrawValues(false)
        rangeSet.colors = listOf(
            Color.parseColor("#33C8E6FF"),
            Color.parseColor("#3390CAF9")
        )
        rangeSet.stackLabels = arrayOf("Min Target", "Range")

        val spendSet = BarDataSet(spendEntries, "Expenses")
        spendSet.color = Color.parseColor("#4CAF50")
        spendSet.valueTextColor = Color.BLACK
        spendSet.valueTextSize = 12f

        val data = BarData(rangeSet, spendSet)
        data.barWidth = 0.35f

        barChart.data = data
        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.axisRight.isEnabled = false
        barChart.setDrawValueAboveBar(true)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = IndexAxisValueFormatter(categories)
        xAxis.granularity = 1f
        xAxis.axisMinimum = -0.5f
        xAxis.axisMaximum = categories.size - 0.5f

        barChart.legend.isEnabled = true
        renderLimits(limits, barChart)
        barChart.animateY(600)
        barChart.invalidate()
    }

    private fun renderLimits(limits: List<BudgetLimit>, barChart: BarChart) {
        barChart.axisLeft.removeAllLimitLines()

        if (limits.isEmpty()) return
        val avgMin = limits.map { it.minAmount }.average().toFloat()
        val avgMax = limits.map { it.maxAmount }.average().toFloat()

        if (avgMin > 0f) {
            val llMin = LimitLine(avgMin, "Avg Min Goal")
            llMin.lineColor = Color.YELLOW
            llMin.lineWidth = 2f
            barChart.axisLeft.addLimitLine(llMin)
        }
        if (avgMax > 0f) {
            val llMax = LimitLine(avgMax, "Avg Max Goal")
            llMax.lineColor = Color.RED
            llMax.lineWidth = 2f
            barChart.axisLeft.addLimitLine(llMax)
        }

        val limitMap = limits.associateBy { it.category }
        val spendSet = barChart.data?.getDataSetByIndex(1) as? BarDataSet
        if (spendSet != null) {
            val newColors = ArrayList<Int>()
            for (i in 0 until spendSet.entryCount) {
                val entry = spendSet.getEntryForIndex(i)
                val cat = categories.getOrNull(entry.x.toInt())
                val total = entry.y.toDouble()
                val limit = limitMap[cat]
                if (limit != null && limit.maxAmount > 0) {
                    newColors.add(when {
                        total > limit.maxAmount -> Color.RED
                        total >= limit.maxAmount * 0.9 -> Color.YELLOW
                        else -> Color.parseColor("#4CAF50")
                    })
                } else {
                    newColors.add(Color.parseColor("#4CAF50"))
                }
            }
            spendSet.colors = newColors
        }

        barChart.invalidate()
    }
}