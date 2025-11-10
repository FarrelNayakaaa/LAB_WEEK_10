package com.example.lab_week_10

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.lab_week_10.database.Total
import com.example.lab_week_10.database.TotalDatabase
import com.example.lab_week_10.viewmodels.TotalViewModel

class MainActivity : AppCompatActivity() {

    // Membuat instance Database dan ViewModel
    private val db by lazy { prepareDatabase() }
    private val viewModel by lazy {
        ViewModelProvider(this)[TotalViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ambil nilai awal dari database
        initializeValueFromDatabase()

        // Siapkan ViewModel dan observer
        prepareViewModel()
    }

    private fun updateText(total: Int) {
        findViewById<TextView>(R.id.text_total).text =
            getString(R.string.text_total, total)
    }

    private fun prepareViewModel() {
        // Observe LiveData agar UI selalu update
        viewModel.total.observe(this) { updateText(it) }

        // Tombol untuk menambah nilai
        findViewById<Button>(R.id.button_increment).setOnClickListener {
            viewModel.incrementTotal()
        }
    }

    // Buat database "total-database"
    private fun prepareDatabase(): TotalDatabase {
        return Room.databaseBuilder(
            applicationContext,
            TotalDatabase::class.java,
            "total-database"
        ).allowMainThreadQueries() // untuk latihan (tidak disarankan di production)
            .build()
    }

    // Ambil atau buat data awal dari database
    private fun initializeValueFromDatabase() {
        val totalList = db.totalDao().getTotal(ID)
        if (totalList.isEmpty()) {
            db.totalDao().insert(Total(id = 1, total = 0))
        } else {
            viewModel.setTotal(totalList.first().total)
        }
    }

    // Simpan data ke database setiap kali Activity di-pause
    override fun onPause() {
        super.onPause()
        db.totalDao().update(Total(ID, viewModel.total.value ?: 0))
    }

    companion object {
        const val ID: Long = 1
    }
}

