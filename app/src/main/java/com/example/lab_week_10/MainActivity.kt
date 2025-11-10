package com.example.lab_week_10

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.lab_week_10.database.Total
import com.example.lab_week_10.database.TotalDatabase
import com.example.lab_week_10.database.TotalObject
import com.example.lab_week_10.viewmodels.TotalViewModel
import java.util.Date

class MainActivity : AppCompatActivity() {

    private val db by lazy { prepareDatabase() }
    private val viewModel by lazy {
        ViewModelProvider(this)[TotalViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeValueFromDatabase()
        prepareViewModel()
    }

    private fun updateText(total: Int) {
        findViewById<TextView>(R.id.text_total).text =
            getString(R.string.text_total, total)
    }

    private fun prepareViewModel() {
        viewModel.total.observe(this) { updateText(it) }

        findViewById<Button>(R.id.button_increment).setOnClickListener {
            viewModel.incrementTotal()
        }
    }

    private fun prepareDatabase(): TotalDatabase {
        return Room.databaseBuilder(
            applicationContext,
            TotalDatabase::class.java,
            "total-database"
        )
            .fallbackToDestructiveMigration() // <--- Tambahkan ini
            .allowMainThreadQueries()
            .build()
    }

    // Ambil atau buat data baru
    private fun initializeValueFromDatabase() {
        val totalList = db.totalDao().getTotal(ID)
        if (totalList.isEmpty()) {
            val initTotal = Total(
                id = 1,
                total = TotalObject(value = 0, date = Date().toString())
            )
            db.totalDao().insert(initTotal)
        } else {
            viewModel.setTotal(totalList.first().total.value)
        }
    }

    // Simpan ke database setiap kali Activity berhenti
    override fun onPause() {
        super.onPause()
        val newTotal = Total(
            id = ID,
            total = TotalObject(
                value = viewModel.total.value ?: 0,
                date = Date().toString()
            )
        )
        db.totalDao().update(newTotal)
    }

    // Tampilkan tanggal terakhir update saat Activity dimulai
    override fun onStart() {
        super.onStart()
        val totalList = db.totalDao().getTotal(ID)
        if (totalList.isNotEmpty()) {
            val lastDate = totalList.first().total.date
            Toast.makeText(this, "Last updated: $lastDate", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val ID: Long = 1
    }
}
