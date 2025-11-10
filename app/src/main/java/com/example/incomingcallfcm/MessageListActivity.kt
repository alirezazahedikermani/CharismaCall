package com.example.incomingcallfcm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MessageListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessageAdapter
    private lateinit var name: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)

        // Get name from intent
        name = intent.getStringExtra("name") ?: ""

        // Setup toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = MessageAdapter(emptyList())
        recyclerView.adapter = adapter

        // Load messages from database
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            db.messageDao().getMessagesByName(name).collectLatest { messages ->
                adapter.updateMessages(messages)

                // Scroll to first unread message
                val firstUnreadPosition = adapter.getFirstUnreadPosition()
                if (firstUnreadPosition >= 0) {
                    recyclerView.scrollToPosition(firstUnreadPosition)
                }

                // Mark all messages as read
                db.messageDao().markAllAsRead(name)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
