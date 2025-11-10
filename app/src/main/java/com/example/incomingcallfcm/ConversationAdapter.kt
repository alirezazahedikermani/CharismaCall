package com.example.incomingcallfcm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ConversationAdapter(
    private var conversations: List<ConversationSummary>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.nameText)
        val subjectText: TextView = view.findViewById(R.id.subjectText)
        val timeText: TextView = view.findViewById(R.id.timeText)
        val unreadCountText: TextView = view.findViewById(R.id.unreadCountText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conversation = conversations[position]
        holder.nameText.text = conversation.name
        holder.subjectText.text = conversation.lastSubject
        holder.timeText.text = formatTime(conversation.lastMessageTime)

        // Show unread count badge
        if (conversation.unreadCount > 0) {
            holder.unreadCountText.visibility = View.VISIBLE
            holder.unreadCountText.text = conversation.unreadCount.toString()
        } else {
            holder.unreadCountText.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onItemClick(conversation.name)
        }
    }

    override fun getItemCount() = conversations.size

    fun updateConversations(newConversations: List<ConversationSummary>) {
        conversations = newConversations
        notifyDataSetChanged()
    }

    private fun formatTime(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        calendar.timeInMillis = timestamp

        val diff = now - timestamp
        val oneDayInMillis = 24 * 60 * 60 * 1000

        return if (diff < oneDayInMillis && isSameDay(now, timestamp)) {
            // Today - show time only
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        } else {
            // Other days - show date only
            SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
        }
    }

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
