package com.example.budgettrackerapp.ui.add

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.budgettrackerapp.R
import java.io.File

class ReceiptViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt_view)

        val iv = findViewById<ImageView>(R.id.ivFullReceipt)
        val uriString = intent.getStringExtra("receipt_uri")
        uriString?.let { uriStr ->
            try {
                val uri = if (uriStr.startsWith("file://")) {
                    // Local file - use FileProvider
                    val file = File(uriStr.removePrefix("file://"))
                    if (file.exists()) {
                        FileProvider.getUriForFile(
                            this,
                            "${packageName}.fileprovider",
                            file
                        )
                    } else {
                        Uri.parse(uriStr)
                    }
                } else {
                    Uri.parse(uriStr)
                }
                iv.setImageURI(uri)
            } catch (ex: Exception) {
                android.util.Log.e("ReceiptView", "Error loading receipt: ${ex.message}")
            }
        }

        iv.setOnClickListener { finish() }
    }
}
