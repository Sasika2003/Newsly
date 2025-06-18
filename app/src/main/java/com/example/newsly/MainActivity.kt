package com.example.newsly

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsly.adapter.NewsAdapter
import com.example.newsly.model.NewsItem
import com.example.newsly.repository.NewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var progressBar: ProgressBar

    private val TAG = "MainActivity"
    private var currentCategory: String = "Sports" // Default category

    private val rssUrls = mapOf(
        "Sports" to "https://rss.cnn.com/rss/edition_sport.rss",
        "Health" to "https://health.economictimes.indiatimes.com/rss/topstories",
        "Technology" to "https://www.theverge.com/rss/index.xml",
        "Business" to "https://feeds.bbci.co.uk/news/business/rss.xml",
        "Entertainment" to "https://www.billboard.com/feed/",
        "World" to "https://feeds.bbci.co.uk/news/world/rss.xml"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)

        // Initialize buttons
        val btnAll = findViewById<Button>(R.id.btnAll)
        val btnSports = findViewById<Button>(R.id.btnSports)
        val btnHealth = findViewById<Button>(R.id.btnHealth)
        val btnTechnology = findViewById<Button>(R.id.btnTechnology)
        val btnBusiness = findViewById<Button>(R.id.btnBusiness)
        val btnEntertainment = findViewById<Button>(R.id.btnEntertainment)
        val btnWorld = findViewById<Button>(R.id.btnWorld)
        val refreshButton = findViewById<ImageButton>(R.id.refreshButton) // Added refresh button

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        newsAdapter = NewsAdapter(this, mutableListOf(), ::openNews, ::shareNews)
        recyclerView.adapter = newsAdapter

        // Set button click listeners
        btnAll.setOnClickListener {
            currentCategory = "All"
            loadAllNews()
        }
        btnSports.setOnClickListener {
            currentCategory = "Sports"
            loadNews("Sports")
        }
        btnHealth.setOnClickListener {
            currentCategory = "Health"
            loadNews("Health")
        }
        btnTechnology.setOnClickListener {
            currentCategory = "Technology"
            loadNews("Technology")
        }
        btnBusiness.setOnClickListener {
            currentCategory = "Business"
            loadNews("Business")
        }
        btnEntertainment.setOnClickListener {
            currentCategory = "Entertainment"
            loadNews("Entertainment")
        }
        btnWorld.setOnClickListener {
            currentCategory = "World"
            loadNews("World")
        }

        // Set refresh button click listener
        refreshButton.setOnClickListener {
            refreshNews()
        }

        // Load default news category (Sports)
        loadNews("Sports")
    }

    private fun loadNews(category: String) {
        val url = rssUrls[category] ?: return
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val newsList = withContext(Dispatchers.IO) { NewsRepository.fetchNews(url) }
                newsAdapter.updateNews(newsList)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading $category news: ${e.message}", e)
                Toast.makeText(this@MainActivity, "Error loading $category news", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun loadAllNews() {
        progressBar.visibility = View.VISIBLE
        val allNews = mutableListOf<NewsItem>()

        lifecycleScope.launch {
            try {
                for ((_, url) in rssUrls) {
                    val newsList = withContext(Dispatchers.IO) { NewsRepository.fetchNews(url) }
                    allNews.addAll(newsList)
                }
                newsAdapter.updateNews(allNews)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading all news: ${e.message}", e)
                Toast.makeText(this@MainActivity, "Error loading all news", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun refreshNews() {
        Toast.makeText(this, "Refreshing news...", Toast.LENGTH_SHORT).show()
        if (currentCategory == "All") {
            loadAllNews()
        } else {
            loadNews(currentCategory)
        }
    }

    private fun openNews(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun shareNews(url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out this news article: $url")
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }
}
