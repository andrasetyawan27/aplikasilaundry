package com.andra.laundryandra

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.andra.laundryandra.databinding.UbahActivityBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class ubahActivity : AppCompatActivity() {
    private lateinit var binding: UbahActivityBinding
    private lateinit var database: DatabaseReference
    private var pesananId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menggunakan ViewBinding
        binding = UbahActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil id yang dikirim dari PesananAdapter
        pesananId = intent.extras?.getString("EXTRA_ID")

        // Inisialisasi Firebase RTDB
        database = FirebaseDatabase.getInstance("https://laundryandrasetyawan-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Order")

        if (pesananId != null) {
            // Ambil data pesanan dari Firebase berdasarkan id
            getPesananData(pesananId!!)
        }

        // Tombol simpan transaksi
        binding.btnSimpanTransaksi.setOnClickListener {
            saveTransaction()
        }
    }

    private fun getPesananData(id: String) {
        // Mengambil data dari Firebase berdasarkan ID yang diteruskan
        database.child(id).get().addOnSuccessListener { snapshot ->
            // Memeriksa apakah data ada
            if (snapshot.exists()) {
                // Ambil data dari snapshot dan konversi ke model PesananModel
                val pesanan = snapshot.getValue(PesananModel::class.java)

                // Jika data ditemukan, tampilkan di UI
                pesanan?.let {
                    binding.etNamaPelanggan.setText(it.namaPelanggan)
                    binding.etJumlah.setText(it.jumlah.toString())
                    binding.spinnerLayanan.setSelection(getLayananIndex(it.layanan))
                    binding.harga.text = "Harga: ${it.total}"
                    binding.tanggal.text = "Tanggal: ${it.Tanggal}"
                }
            } else {
                // Jika data tidak ditemukan
                Toast.makeText(this, "Data pesanan tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            // Menangani kesalahan jika ada
            Toast.makeText(this, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getLayananIndex(layanan: String): Int {
        val layananOptions = resources.getStringArray(R.array.layanan_options)
        return layananOptions.indexOf(layanan)
    }

    private fun saveTransaction() {
        val namaPelanggan = binding.etNamaPelanggan.text.toString()
        val jumlah = binding.etJumlah.text.toString().toIntOrNull() ?: 0
        val layanan = binding.spinnerLayanan.selectedItem.toString()
        val total = binding.harga.text.toString().replace("Harga: ", "")
        val tanggal = binding.tanggal.text.toString().replace("Tanggal: ", "")

        // Validasi input
        if (namaPelanggan.isNotEmpty() && jumlah > 0) {
            val pesanan = PesananModel(pesananId ?: "", namaPelanggan, jumlah, layanan, tanggal, total)
            // Update data pesanan di Firebase
            database.child(pesananId!!).setValue(pesanan).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Transaksi berhasil disimpan", Toast.LENGTH_SHORT).show()
                    finish()  // Kembali ke activity sebelumnya
                } else {
                    Toast.makeText(this, "Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Nama dan jumlah harus diisi", Toast.LENGTH_SHORT).show()
        }
    }
}
