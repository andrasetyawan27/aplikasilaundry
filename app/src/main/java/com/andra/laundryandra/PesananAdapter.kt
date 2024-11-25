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
import com.google.firebase.firestore.FirebaseFirestore

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
        val buttonhapus: Button = itemView.findViewById(R.id.buttonHapus)
        val buttonedit: Button = itemView.findViewById(R.id.buttonUbah)
        val buttonselesai: Button = itemView.findViewById(R.id.buttonSelesai)
    }

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

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

        // Logging untuk debugging data
        Log.d("BindViewHolder", "Binding Pesanan - ID: ${currentPesanan.id}, Nama: ${currentPesanan.namaPelanggan}")

        // Handle button actions
        holder.buttonhapus.setOnClickListener {
            onHapusClick(holder.itemView.context, currentPesanan.id, position)
        }

        holder.buttonedit.setOnClickListener {
            onEditClick(holder.itemView.context, currentPesanan)
        }

        holder.buttonselesai.setOnClickListener {
            onCompleteClick(holder.itemView.context, currentPesanan)
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
                deleteOrderFromFirestore(context, id, position)
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun deleteOrderFromFirestore(context: Context, id: String, position: Int) {
        Log.d("FirestoreDelete", "Menghapus dokumen dengan ID: $id")
        db.collection("pesanan")
            .document(id)
            .delete()
            .addOnSuccessListener {
                pesananList.removeAt(position)
                notifyItemRemoved(position)
                Toast.makeText(context, "Pesanan berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreDelete", "Gagal menghapus dokumen: ", e)
                Toast.makeText(context, "Gagal menghapus pesanan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onEditClick(context: Context, pesanan: PesananModel) {
        Log.d("PesananAdapter", "Mengedit pesanan dengan ID: ${pesanan.id}")
        Toast.makeText(context, "Edit pesanan: ${pesanan.namaPelanggan}", Toast.LENGTH_SHORT).show()
        // Tambahkan logika untuk edit pesanan
    }

    private fun onCompleteClick(context: Context, pesanan: PesananModel) {
        Log.d("PesananAdapter", "Menyelesaikan pesanan dengan ID: ${pesanan.id}")
        db.collection("pesanan")
            .document(pesanan.id)
            .update("status", "selesai")
            .addOnSuccessListener {
                Toast.makeText(context, "Pesanan selesai", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("PesananAdapter", "Gagal menyelesaikan pesanan: ", e)
                Toast.makeText(context, "Gagal menyelesaikan pesanan", Toast.LENGTH_SHORT).show()
            }
    }
}
