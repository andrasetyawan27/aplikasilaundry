package com.andra.laundryandra

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class ProsesAdapter(
    private val pesananList: ArrayList<PesananModel>,
    private val onItemClick: (PesananModel) -> Unit
) : RecyclerView.Adapter<ProsesAdapter.ProsesViewHolder>() {

    // Inner class untuk ViewHolder
    class ProsesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textNama: TextView = itemView.findViewById(R.id.textNama)
        val textLayanan: TextView = itemView.findViewById(R.id.textLayanan)
        val textJumlah: TextView = itemView.findViewById(R.id.textJumlah)
        val textTotal: TextView = itemView.findViewById(R.id.textTotal)
        val textTanggal: TextView = itemView.findViewById(R.id.textTanggal)
        val buttonHapus: Button = itemView.findViewById(R.id.buttonHapus)
    }

    private val database = FirebaseDatabase.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProsesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_proses, parent, false)
        return ProsesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProsesViewHolder, position: Int) {
        val currentPesanan = pesananList[position]

        // Set data ke ViewHolder
        holder.textNama.text = currentPesanan.namaPelanggan
        holder.textLayanan.text = "Layanan: ${currentPesanan.layanan}"
        holder.textJumlah.text = "Jumlah: ${currentPesanan.jumlah}"
        holder.textTotal.text = "Total: Rp ${currentPesanan.total}"
        holder.textTanggal.text = "Tanggal: ${currentPesanan.Tanggal}"

        // Event untuk tombol hapus
        holder.buttonHapus.setOnClickListener {
            onHapusClick(holder.itemView.context, currentPesanan.id, position)
        }

        // Event klik item
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
        Log.d("RealtimeDBDelete", "Menghapus data dengan ID: $id")
        database.getReference("Order") // Referensi ke tabel "Order"
            .child(id)
            .removeValue()
            .addOnSuccessListener {
                pesananList.removeAt(position)
                notifyItemRemoved(position)
                Toast.makeText(context, "Pesanan berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("RealtimeDBDelete", "Gagal menghapus data: ", e)
                Toast.makeText(context, "Gagal menghapus pesanan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
