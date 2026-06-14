package com.example.budgettrackerapp.ui.add

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.budgettrackerapp.R
import com.example.budgettrackerapp.data.*
import android.content.Intent
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
 

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
        val btnAttach = view.findViewById<Button>(R.id.btnAttachReceipt)
        val btnCapture = view.findViewById<Button>(R.id.btnCaptureReceipt)
        val receiptImage = view.findViewById<ImageView>(R.id.ivReceipt)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        var selectedReceiptUri: Uri? = null

        val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                try {
                    // Copy picked image to local storage to avoid permission issues
                    val localFile = copyUriToLocalFile(it)
                    selectedReceiptUri = Uri.fromFile(localFile)
                    receiptImage.setImageURI(selectedReceiptUri)
                    receiptImage.visibility = View.VISIBLE
                } catch (ex: Exception) {
                    Toast.makeText(context, "Failed to load image: ${ex.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Prepare TakePicture launcher
        var currentPhotoFile: File? = null
        lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                currentPhotoFile?.let { f ->
                    val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", f)
                    // Show confirmation dialog: accept or retake
                    showCaptureConfirmation(uri,
                        onAccept = {
                            selectedReceiptUri = uri
                            receiptImage.setImageURI(uri)
                            receiptImage.visibility = View.VISIBLE
                        },
                        onRetake = {
                            // delete previous file and relaunch camera
                            try {
                                if (f.exists()) f.delete()
                            } catch (ex: Exception) { }
                            val newFile = try { createImageFile() } catch (ex: Exception) { null }
                            newFile?.also {
                                currentPhotoFile = it
                                val newUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", it)
                                takePictureLauncher.launch(newUri)
                            }
                        }
                    )
                }
            }
        }

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

        btnAttach.setOnClickListener {
            getContent.launch("image/*")
        }

        btnCapture.setOnClickListener {
            // create file and launch camera
            val photoFile = try {
                createImageFile()
            } catch (ex: Exception) {
                null
            }
            photoFile?.also {
                currentPhotoFile = it
                val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", it)
                takePictureLauncher.launch(uri)
            }
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
                description = desc,
                receiptUri = selectedReceiptUri?.toString()
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
                    receiptImage.visibility = View.GONE
                    receiptImage.setImageDrawable(null)
                    selectedReceiptUri = null
                }
            }
        }

        receiptImage.setOnClickListener {
            selectedReceiptUri?.let { uri ->
                val intent = Intent(requireContext(), com.example.budgettrackerapp.ui.add.ReceiptViewActivity::class.java)
                intent.putExtra("receipt_uri", uri.toString())
                startActivity(intent)
            }
        }

        return view
    }

    @Throws(Exception::class)
    private fun createImageFile(): File {
        val storageDir = File(requireContext().filesDir, "receipts")
        if (!storageDir.exists()) storageDir.mkdirs()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val image = File(storageDir, "JPEG_${timeStamp}_.jpg")
        image.createNewFile()
        return image
    }

    private fun showCaptureConfirmation(uri: Uri, onAccept: () -> Unit, onRetake: () -> Unit) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        val dialog = builder.create()

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24,24,24,24)
        }
        val iv = ImageView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 800)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageURI(uri)
        }
        val buttons = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            val btnRetake = Button(requireContext()).apply {
                text = "Retake"
                layoutParams = lp
                setOnClickListener { dialog.dismiss(); onRetake() }
            }
            val btnAccept = Button(requireContext()).apply {
                text = "Accept"
                layoutParams = lp
                setOnClickListener { dialog.dismiss(); onAccept() }
            }
            addView(btnRetake)
            addView(btnAccept)
        }
        layout.addView(iv)
        layout.addView(buttons)

        dialog.setView(layout)
        dialog.show()
    }

    private fun copyUriToLocalFile(uri: Uri): File {
        val storageDir = File(requireContext().filesDir, "receipts")
        if (!storageDir.exists()) storageDir.mkdirs()
        
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val outputFile = File(storageDir, "receipt_${timeStamp}.jpg")
        
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        inputStream?.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        return outputFile
    }
}