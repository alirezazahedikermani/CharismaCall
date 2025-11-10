package com.example.incomingcallfcm

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private var messages: List<Message>
) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val subjectText: TextView = view.findViewById(R.id.subjectText)
        val bodyText: TextView = view.findViewById(R.id.bodyText)
        val timestampText: TextView = view.findViewById(R.id.timestampText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.subjectText.text = message.subject
        holder.bodyText.text = message.body
        holder.timestampText.text = formatTimestamp(message.timestamp)

        // Highlight unread messages
        if (!message.isRead) {
            holder.subjectText.setTypeface(null, Typeface.BOLD)
            holder.bodyText.setTypeface(null, Typeface.BOLD)
        } else {
            holder.subjectText.setTypeface(null, Typeface.NORMAL)
            holder.bodyText.setTypeface(null, Typeface.NORMAL)
        }
    }

    override fun getItemCount() = messages.size

    fun updateMessages(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    fun getFirstUnreadPosition(): Int {
        return messages.indexOfFirst { !it.isRead }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
