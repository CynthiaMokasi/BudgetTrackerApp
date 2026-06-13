package com.example.budgettrackerapp.ui.totals

import android.content.Context
import android.widget.TextView
import com.example.budgettrackerapp.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class CategoryMarkerView(context: Context) : MarkerView(context, R.layout.view_marker) {
    private val tvContent: TextView = findViewById(R.id.tvMarker)

    override fun refreshContent(e: com.github.mikephil.charting.data.Entry?, highlight: Highlight?) {
        if (e is BarEntry) {
            tvContent.text = String.format("%s: %.2f", e.x.toInt(), e.y)
        } else if (e != null) {
            tvContent.text = String.format("%.2f", e.y)
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat())
    }
}
