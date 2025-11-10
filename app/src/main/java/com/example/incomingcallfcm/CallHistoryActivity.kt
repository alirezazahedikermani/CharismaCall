package com.example.incomingcallfcm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CallHistoryActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ConversationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_history)

        // Setup toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Messages"

        // Setup navigation drawer
        drawerLayout = findViewById(R.id.drawerLayout)
        val navigationView: NavigationView = findViewById(R.id.navigationView)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ConversationAdapter(emptyList()) { name ->
            // Navigate to MessageListActivity when a conversation is clicked
            val intent = Intent(this, MessageListActivity::class.java)
            intent.putExtra("name", name)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Load conversations from database
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            db.messageDao().getConversationSummaries().collectLatest { conversations ->
                adapter.updateConversations(conversations)
            }
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
