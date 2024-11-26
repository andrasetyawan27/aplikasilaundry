package com.andra.laundryandra

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.andra.laundryandra.databinding.ActivityDashboardBinding
import com.google.firebase.database.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var database: DatabaseReference
    private lateinit var adapter: PesananAdapter
    private var pesananList = ArrayList<PesananModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Database reference
        database = FirebaseDatabase.getInstance("https://laundryandrasetyawan-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("Order")

        // Initialize buttons
        val Badmin = binding.tambahadmin
        val Bleguler = binding.leguler
        val Bexpress = binding.express

        // Set up button click listeners
        Badmin.setOnClickListener {
            val intent = Intent(this@DashboardActivity, ProsesAdapter::class.java)
            startActivity(intent)
        }

        Bleguler.setOnClickListener {
            val intent = Intent(this@DashboardActivity,TransaksiRegulerActivity::class.java)
            startActivity(intent)
        }

        Bexpress.setOnClickListener {
            val intent = Intent(this@DashboardActivity, TransaksiExpressActivity::class.java)
            startActivity(intent)
        }

        // Set up RecyclerView
        setupRecyclerView()

        // Load data from Firebase
        loadPesananData()
    }

    private fun setupRecyclerView() {
        adapter = PesananAdapter(pesananList) { pesanan ->
            onPesananClick(pesanan)
        }

        binding.listpesanan.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = this@DashboardActivity.adapter
        }
    }

    private fun loadPesananData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pesananList.clear() // Clear list to avoid duplication
                for (data in snapshot.children) {
                    val pesanan = data.getValue(PesananModel::class.java)
                    if (pesanan != null) {
                        pesananList.add(pesanan)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DashboardActivity, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("DashboardActivity", "DatabaseError: ${error.message}")
            }
        })
    }

    private fun onPesananClick(pesanan: PesananModel) {
        Toast.makeText(this, "Detail pesanan: ${pesanan.namaPelanggan}", Toast.LENGTH_SHORT).show()
    }
}
