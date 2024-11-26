package com.andra.laundryandra

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class PesananAdapter(
    private val pesananList: ArrayList<PesananModel>,
    private val onItemClick: (PesananModel) -> Unit
) : RecyclerView.Adapter<PesananAdapter.PesananViewHolder>() {

    class PesananViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textNama: TextView = itemView.findViewById(R.id.textNama)
        val textLayanan: TextView = itemView.findViewById(R.id.textLayanan)
        val textJumlah: TextView = itemView.findViewById(R.id.textJumlah)
        val textTotal: TextView = itemView.findViewById(R.id.textTotal)
        val textTanggal: TextView = itemView.findViewById(R.id.textTanggal)
        val buttonHapus: Button = itemView.findViewById(R.id.buttonHapus)
        val buttonProses: Button = itemView.findViewById(R.id.buttonProses)
        val buttonUbah: Button = itemView.findViewById(R.id.buttonUbah)
    }

    // Firebase Database references
    private val databaseReference =
        FirebaseDatabase.getInstance("https://laundryandrasetyawan-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Order")

    private val selesaiReference =
        FirebaseDatabase.getInstance("https://laundryandrasetyawan-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Selesai")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PesananViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_pesanan, parent, false)
        return PesananViewHolder(view)
    }

    override fun onBindViewHolder(holder: PesananViewHolder, position: Int) {
        val currentPesanan = pesananList[position]
        holder.textNama.text = currentPesanan.namaPelanggan
        holder.textLayanan.text = "Layanan: ${currentPesanan.layanan}"
        holder.textJumlah.text = "Jumlah: ${currentPesanan.jumlah}"
        holder.textTotal.text = "Total: ${currentPesanan.total}"
        holder.textTanggal.text = "Tanggal: ${currentPesanan.Tanggal}"

        // Tombol hapus


        // Tombol Ubah
        holder.buttonUbah.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ubahActivity::class.java)

            val bundle = Bundle().apply {
                putString("EXTRA_ID", currentPesanan.id ?: "")
            }

            intent.putExtras(bundle)
            context.startActivity(intent)
        }

        // Event Klik
        holder.itemView.setOnClickListener {
            onItemClick(currentPesanan)
        }
    }

    override fun getItemCount(): Int = pesananList.size

    private fun onHapusClick(context: Context, id: String, position: Int) {
        if (id.isEmpty()) {
            Toast.makeText(context, "ID pesanan tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(context)
        builder.setMessage("Apakah Anda yakin ingin menghapus pesanan ini?")
            .setCancelable(false)
            .setPositiveButton("Ya") { _, _ ->
                deleteOrderFromDatabase(context, id, position)
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun deleteOrderFromDatabase(context: Context, id: String, position: Int) {
        databaseReference.child(id)
            .removeValue()
            .addOnSuccessListener {
                pesananList.removeAt(position)
                notifyItemRemoved(position)
                Toast.makeText(context, "Pesanan berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("PesananAdapter", "Gagal menghapus data: ", e)
                Toast.makeText(context, "Gagal menghapus pesanan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onProsesClick(context: Context, id: String, position: Int) {
        if (id.isEmpty()) {
            Toast.makeText(context, "ID pesanan tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val orderReference = FirebaseDatabase.getInstance("https://laundryandrasetyawan-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Order")
        val selesaiReference = FirebaseDatabase.getInstance("https://laundryandrasetyawan-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Selesai")

        // Ambil data dari tabel Order
        orderReference.child(id).get().addOnSuccessListener { snapshot ->
            val pesanan = snapshot.getValue(PesananModel::class.java)
            if (pesanan != null) {
                selesaiReference.child(id).setValue(pesanan) // Tambahkan ke tabel Selesai
                    .addOnSuccessListener {
                        // Hapus dari tabel Order
                        orderReference.child(id).removeValue().addOnSuccessListener {
                            pesananList.removeAt(position)
                            notifyItemRemoved(position)
                            Toast.makeText(context, "Pesanan berhasil diproses", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener { e ->
                            Log.e("PesananAdapter", "Gagal menghapus data dari Order: ", e)
                            Toast.makeText(context, "Gagal menghapus data dari Order: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { e ->
                        Log.e("PesananAdapter", "Gagal menyalin data ke Selesai: ", e)
                        Toast.makeText(context, "Gagal memproses pesanan: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Data pesanan tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Log.e("PesananAdapter", "Gagal membaca data dari Order: ", e)
            Toast.makeText(context, "Gagal membaca data pesanan: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}