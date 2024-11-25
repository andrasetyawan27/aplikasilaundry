package com.andra.laundryandra

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ubahActivity : AppCompatActivity() {

    private lateinit var etNamaPelanggan: EditText
    private lateinit var etJumlah: EditText
    private lateinit var btnSimpanTransaksi: Button
    private lateinit var layananText: TextView
    private lateinit var hargaText: TextView
    private lateinit var tanggalText: TextView
    private lateinit var database: DatabaseReference
    private lateinit var transaksiId: String

    private val hargaPerKilo = 8000 // Harga per kilogram dalam rupiah (sesuaikan dengan kebutuhan)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ubah_activity) // Gunakan layout XML yang sudah ada

        // Inisialisasi elemen tampilan dari XML
        tanggalText = findViewById(R.id.tanggal)
        layananText = findViewById(R.id.spinnerLayanan)
        etNamaPelanggan = findViewById(R.id.etNamaPelanggan)
        etJumlah = findViewById(R.id.etJumlah)
        hargaText = findViewById(R.id.harga)
        btnSimpanTransaksi = findViewById(R.id.btnSimpanTransaksi)

        // Inisialisasi Firebase
        database = FirebaseDatabase.getInstance("https://laundryandrasetyawan-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("Order")

        // Mendapatkan ID transaksi yang akan diedit dari Intent (harus diberikan saat membuka activity ini)
        transaksiId = intent.getStringExtra("transaksiId") ?: return // Pastikan transaksiId ada
        Log.d("ubahActivity", "Received Transaksi ID: $transaksiId") // Debug log to check transaksiId

        // Load data transaksi yang ada untuk diubah
        loadDataTransaksi()

        // Set tanggal saat ini
        val currentDate = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        tanggalText.text = "Tanggal: $currentDate"
        layananText.text = "Layanan: Reguler"

        // TextWatcher untuk menghitung harga otomatis
        etJumlah.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculateTotalHarga()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Tombol Simpan
        btnSimpanTransaksi.setOnClickListener {
            simpanDataTransaksi(currentDate)
        }
    }

    private fun loadDataTransaksi() {
        database.child(transaksiId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                Log.d("ubahActivity", "Data exists: ${snapshot.exists()}")
                if (snapshot.exists()) {
                    val transaksi = snapshot.getValue(Transaksi::class.java)
                    transaksi?.let {
                        // Tampilkan data transaksi yang ada
                        etNamaPelanggan.setText(it.namaPelanggan)
                        etJumlah.setText(it.jumlah.toString())
                        hargaText.text = "Harga: ${it.total}"
                    }
                } else {
                    Toast.makeText(this@ubahActivity, "Transaksi tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Toast.makeText(this@ubahActivity, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun calculateTotalHarga() {
        val jumlah = etJumlah.text.toString().trim()
        if (jumlah.isNotEmpty()) {
            val jumlahInt = jumlah.toIntOrNull() ?: 0
            val totalHarga = jumlahInt * hargaPerKilo
            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            val jumlahFormat = formatRupiah.format(totalHarga) // Format dalam bentuk "Rp65.000,00"
            hargaText.text = "Harga: $jumlahFormat"
        } else {
            hargaText.text = "Harga: -"
        }
    }

    private fun simpanDataTransaksi(tanggal: String) {
        val namaPelanggan = etNamaPelanggan.text.toString().trim()
        val jumlah = etJumlah.text.toString().trim()

        if (namaPelanggan.isEmpty() || jumlah.isEmpty()) {
            Toast.makeText(this, "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        val jumlahInt = try {
            jumlah.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Jumlah harus berupa angka", Toast.LENGTH_SHORT).show()
            return
        }

        val totalHarga = jumlahInt * hargaPerKilo
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(totalHarga)

        // Buat data transaksi
        val transaksi = mapOf(
            "id" to transaksiId, // Gunakan transaksiId yang ada
            "namaPelanggan" to namaPelanggan,
            "jumlah" to jumlahInt,
            "layanan" to "Reguler", // Gunakan "Reguler"
            "total" to formatRupiah,
            "Tanggal" to tanggal
        )

        // Update data transaksi ke Firebase
        database.child(transaksiId).setValue(transaksi).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                etNamaPelanggan.setText("")
                etJumlah.setText("")
                hargaText.text = "Total Harga: -"
            } else {
                Toast.makeText(this, "Gagal memperbarui data", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

data class Transaksi(
    val id: String = "",
    val namaPelanggan: String = "",
    val jumlah: Int = 0,
    val layanan: String = "",
    val total: String = "",
    val Tanggal: String = ""
)
