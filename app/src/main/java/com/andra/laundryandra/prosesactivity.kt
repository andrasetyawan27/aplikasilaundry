package com.andra.laundryandra

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class prosesactivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: View
    private lateinit var adapter: ProsesAdapter
    private val pesananList = ArrayList<PesananModel>()
    private val databaseReference = FirebaseDatabase.getInstance("https://laundryandrasetyawan-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Selesai")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.proses_activity)

        // Inisialisasi RecyclerView dan ProgressBar
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProsesAdapter(pesananList) { pesanan ->
            // Event saat item diklik
            Toast.makeText(this, "Item ${pesanan.namaPelanggan} diklik!", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter

        // Muat data dari Firebase
        fetchPesananData()
    }

    private fun fetchPesananData() {
        progressBar.visibility = View.VISIBLE
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pesananList.clear() // Hapus data lama
                for (data in snapshot.children) {
                    val pesanan = data.getValue(PesananModel::class.java)
                    if (pesanan != null) {
                        pesanan.id = data.key ?: "" // Simpan ID dari Firebase
                        pesananList.add(pesanan)
                    }
                }
                adapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProsesActivity", "Gagal memuat data: ${error.message}")
                Toast.makeText(this@prosesactivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
        })
    }
}
