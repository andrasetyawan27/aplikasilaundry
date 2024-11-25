package com.andra.laundryandra

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransaksiExpressActivity : AppCompatActivity() {

    private lateinit var etNamaPelanggan: EditText
    private lateinit var etJumlah: EditText
    private lateinit var btnSimpanTransaksi: Button
    private lateinit var layananText: TextView
    private lateinit var hargaText: TextView
    private lateinit var tanggalText: TextView
    private lateinit var database: DatabaseReference

    private val hargaPerKilo = 13000 // Harga per kilogram dalam rupiah

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transaksi_express) // Gunakan layout XML yang sudah ada

        // Inisialisasi elemen tampilan dari XML
        tanggalText = findViewById(R.id.tanggal)
        layananText = findViewById(R.id.layanantext)
        etNamaPelanggan = findViewById(R.id.etNamaPelanggan)
        etJumlah = findViewById(R.id.etJumlah)
        hargaText = findViewById(R.id.harga)
        btnSimpanTransaksi = findViewById(R.id.btnSimpanTransaksi)

        // Set tanggal saat ini
        val currentDate = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        tanggalText.text = "Tanggal: $currentDate"
        layananText.text = "Layanan: Express"

        // Inisialisasi Firebase
        database = FirebaseDatabase.getInstance("https://laundryandrasetyawan-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("Order")

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

    private fun calculateTotalHarga() {
        val jumlah = etJumlah.text.toString().trim()
        if (jumlah.isNotEmpty()) {
            val jumlahInt = jumlah.toIntOrNull() ?: 0
            val totalHarga = jumlahInt * hargaPerKilo
            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            val jumlahFormat = formatRupiah.format(totalHarga).replace("Rp", "").trim() // Hanya ambil angka tanpa "Rp"
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

        // Generate ID transaksi
        val transaksiId = database.push().key ?: return

        // Data transaksi dengan ID
        val transaksi = mapOf(
            "id" to transaksiId, // Tambahkan ID transaksi
            "namaPelanggan" to namaPelanggan,
            "jumlah" to jumlahInt,
            "layanan" to "Express",
            "total" to formatRupiah,
            "Tanggal" to tanggal
        )

        // Simpan data ke Firebase
        database.child(transaksiId).setValue(transaksi).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                etNamaPelanggan.setText("")
                etJumlah.setText("")
                hargaText.text = "Total Harga: -"
            } else {
                Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
