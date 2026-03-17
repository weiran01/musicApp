package com.example.myapp.Data

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R

class RecordAdapter(private val data: MutableList<RecordModel>) :
    RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {
    var onItemDeleteListener: ((Int) -> Unit)? = null
    var onItemPlayListener: ((String) -> Unit)? = null


    class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvListTime: TextView = itemView.findViewById(R.id.tv_list_time)
        val tvListTitle: TextView = itemView.findViewById(R.id.tv_list_title)
        val btnDelete: ImageButton = itemView.findViewById(R.id.delete)
        val btnPlay: ImageButton = itemView.findViewById(R.id.play)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_record, parent, false)
        return RecordViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val currentModel = data[position]

        holder.tvListTime.text = currentModel.time
        holder.tvListTitle.text = currentModel.title

        holder.btnPlay.setOnClickListener {
            if (currentModel.audioPath.isNullOrEmpty()) {
                android.widget.Toast.makeText(
                    holder.itemView.context,
                    "无录音文件可播放",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            // 只分发事件，不处理跳转
            onItemPlayListener?.invoke(currentModel.audioPath!!)
        }

        holder.btnDelete.setOnClickListener {
            showDeleteDialog(holder.itemView.context, position)
        }
    }

    override fun getItemCount(): Int = data.size

    private fun showDeleteDialog(context: Context, position: Int) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.delete))
            .setMessage(context.getString(R.string.redelete))
            .setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                onItemDeleteListener?.invoke(position)
            }
            .setNegativeButton(context.getString(R.string.no), null)
            .show()
    }

    fun removeItem(position: Int) {
        if (position in 0 until data.size) {
            data.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, data.size)
        }
    }
}