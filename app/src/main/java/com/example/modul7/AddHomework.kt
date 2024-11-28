package com.example.modul7

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.modul7.database.DatabaseContract
import com.example.modul7.databinding.ActivityAddHomeworkBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AddHomeworkActivity : AppCompatActivity() {

    private var isEdit = false
    private var homework: Homework? = null
    private var position: Int = 0
    private lateinit var homeworkHelper: HomeworkHelper
    private lateinit var binding: ActivityAddHomeworkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHomeworkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        homeworkHelper = HomeworkHelper.getInstance(applicationContext)
        homeworkHelper.open()

        homework = intent.getParcelableExtra(EXTRA_HOMEWORK)
        if (homework != null) {
            position = intent.getIntExtra(EXTRA_POSITION, 0)
            isEdit = true
        } else {
            homework = Homework()
        }

        val actionBarTitle: String
        val btnTitle: String

        if (isEdit) {
            actionBarTitle = "Ubah"
            btnTitle = "Update"
            homework?.let {
                binding.edtTitle.setText(it.title)
                binding.edtDescription.setText(it.description)
            }
            binding.btnDelete.visibility = View.VISIBLE
        } else {
            actionBarTitle = "Tambah"
            btnTitle = "Simpan"
            binding.btnDelete.visibility = View.GONE
        }

        supportActionBar?.apply {
            title = actionBarTitle
            setDisplayHomeAsUpEnabled(true)
        }

        binding.btnSubmit.text = btnTitle
        binding.btnSubmit.setOnClickListener { addNewHomework() }
        binding.btnDelete.setOnClickListener { showAlertDialog(ALERT_DIALOG_DELETE) }
    }

    private fun addNewHomework() {
        val title = binding.edtTitle.text.toString().trim()
        val description = binding.edtDescription.text.toString().trim()

        if (title.isEmpty()) {
            binding.edtTitle.error = "Title tidak boleh kosong"
            return
        }

        homework?.title = title
        homework?.description = description

        val intent = Intent()
        intent.putExtra(EXTRA_HOMEWORK, homework)
        intent.putExtra(EXTRA_POSITION, position)

        val values = ContentValues()
        values.put(DatabaseContract.HomeworkColumns.TITLE, title)
        values.put(DatabaseContract.HomeworkColumns.DESCRIPTION, description)

        if (isEdit) {
            homework?.id?.let {
                val result = homeworkHelper.update(it.toString(), values)
                if (result > 0) {
                    setResult(RESULT_UPDATE, intent)
                    finish()
                } else {
                    Toast.makeText(this, "Gagal memperbaharui data", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            homework?.date = getCurrentDate()
            values.put(DatabaseContract.HomeworkColumns.DATE, homework?.date)
            val result = homeworkHelper.insert(values)
            if (result > 0) {
                homework?.id = result.toInt()
                setResult(RESULT_ADD, intent)
                finish()
            } else {
                Toast.makeText(this, "Gagal menambah data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val date = java.util.Date()
        return dateFormat.format(date)
    }

    override fun onBackPressed() {
        showAlertDialog(ALERT_DIALOG_CLOSE)
    }

    private fun showAlertDialog(type: Int) {
        val isDialogClose = type == ALERT_DIALOG_CLOSE
        val dialogTitle = if (isDialogClose) "Batal" else "Hapus Homework"
        val dialogMessage = if (isDialogClose) {
            "Apakah Anda yakin ingin membatalkan perubahan?"
        } else {
            "Apakah Anda yakin ingin menghapus data ini?"
        }

        AlertDialog.Builder(this).apply {
            setTitle(dialogTitle)
            setMessage(dialogMessage)
            setCancelable(false)
            setPositiveButton("Ya") { _, _ ->
                if (isDialogClose) {
                    finish()
                } else {
                    homework?.id?.let {
                        val result = homeworkHelper.deleteById(it.toString()).toLong()
                        if (result > 0) {
                            val intent = Intent()
                            intent.putExtra(EXTRA_POSITION, position)
                            setResult(RESULT_DELETE, intent)
                            finish()
                        } else {
                            Toast.makeText(this@AddHomeworkActivity, "Gagal menghapus data", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            setNegativeButton("Tidak") { dialog, _ -> dialog.cancel() }
        }.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        homeworkHelper.close()
    }

    companion object {
        const val EXTRA_HOMEWORK = "extra_homework"
        const val EXTRA_POSITION = "extra_position"
        const val RESULT_ADD = 101
        const val RESULT_UPDATE = 201
        const val RESULT_DELETE = 301
        const val ALERT_DIALOG_CLOSE = 10
        const val ALERT_DIALOG_DELETE = 20
    }
}
