package com.example.newsly.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.newsly.R
import com.example.newsly.model.NewsItem

class NewsAdapter(
    private val context: Context,
    private var newsList: MutableList<NewsItem>,
    private val onViewClicked: (String) -> Unit,
    private val onShareClicked: (String) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.newsTitle)
        val viewButton: Button = itemView.findViewById(R.id.btnView)
        val shareButton: Button = itemView.findViewById(R.id.btnShare)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.news_item, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val newsItem = newsList[position]
        holder.titleTextView.text = newsItem.title

        // Set click listeners for buttons
        holder.viewButton.setOnClickListener { onViewClicked(newsItem.link) }
        holder.shareButton.setOnClickListener { onShareClicked(newsItem.link) }
    }

    override fun getItemCount(): Int = newsList.size

    fun updateNews(newNewsList: List<NewsItem>) {
        newsList.clear()
        newsList.addAll(newNewsList)
        notifyDataSetChanged()
    }
}
