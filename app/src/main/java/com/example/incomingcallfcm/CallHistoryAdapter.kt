package com.example.incomingcallfcm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class CallHistoryAdapter(private val callHistory: List<CallHistory>) :
    RecyclerView.Adapter<CallHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = callHistory[position]
        val date = Date(item.timestamp)
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
        holder.textView.text = format.format(date)
    }

    override fun getItemCount() = callHistory.size
}
