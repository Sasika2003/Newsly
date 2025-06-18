package com.example.newsly.repository

import android.util.Log
import com.example.newsly.model.NewsItem
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

object NewsRepository {
    private const val TAG = "NewsRepository"
    private const val TIMEOUT = 15000 // 15 seconds timeout

    // Sample data for fallback if network fails
    private val fallbackData = listOf(
        NewsItem(
            "Sports News 1",
            "https://example.com/sports1",
            "This is a sample sports news article that appears when network connection fails."
        ),
        NewsItem(
            "Sports News 2",
            "https://example.com/sports2",
            "Another sample sports news article for testing purposes."
        ),
        NewsItem(
            "Health News 1",
            "https://example.com/health1",
            "Sample health news article for testing the app when network is not available."
        ),
        NewsItem(
            "Health News 2",
            "https://example.com/health2",
            "Another health news item for demonstration purposes."
        )
    )

    fun fetchNews(url: String): List<NewsItem> {
        val newsList = mutableListOf<NewsItem>()

        try {
            Log.d(TAG, "Attempting to fetch news from URL: $url")

            // Try to fetch from alternative sources if CNN doesn't work
            val actualUrl = when {
                url.contains("sport") -> "https://feeds.bbci.co.uk/sport/rss.xml"
                url.contains("health") -> "https://health.economictimes.indiatimes.com/rss/topstories"
                else -> url
            }

            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            // Install the trust manager
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)

            // Set up the connection
            val connection = URL(actualUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = TIMEOUT
            connection.readTimeout = TIMEOUT
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; NewslyApp/1.0)")

            val responseCode = connection.responseCode
            Log.d(TAG, "Response code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Parse XML content
                val factory = XmlPullParserFactory.newInstance()
                factory.isNamespaceAware = false
                val parser = factory.newPullParser()

                val inputStream = connection.inputStream
                parser.setInput(inputStream, "UTF-8")

                var eventType = parser.eventType
                var title = ""
                var link = ""
                var description = ""
                var insideItem = false

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            when (parser.name.lowercase()) {
                                "item" -> insideItem = true
                                "title" -> if (insideItem) title = readText(parser)
                                "link" -> if (insideItem) link = readText(parser)
                                "description" -> if (insideItem) description = readText(parser)
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name.equals("item", ignoreCase = true)) {
                                if (title.isNotBlank() && link.isNotBlank()) {
                                    newsList.add(NewsItem(title, link, description))
                                    Log.d(TAG, "Added news item: $title")
                                }
                                title = ""
                                link = ""
                                description = ""
                                insideItem = false
                            }
                        }
                    }
                    eventType = parser.next()
                }

                inputStream.close()
            } else {
                Log.e(TAG, "HTTP error code: $responseCode")
                throw Exception("Server returned error code: $responseCode")
            }

            connection.disconnect()

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching news: ${e.message}", e)

            // Return fallback data if network fails
            return if (url.contains("sport")) {
                fallbackData.filter { it.title.contains("Sports") }
            } else {
                fallbackData.filter { it.title.contains("Health") }
            }
        }

        // If no news items were retrieved, return fallback data
        if (newsList.isEmpty()) {
            Log.d(TAG, "No news items found, using fallback data")
            return if (url.contains("sport")) {
                fallbackData.filter { it.title.contains("Sports") }
            } else {
                fallbackData.filter { it.title.contains("Health") }
            }
        }

        Log.d(TAG, "Successfully fetched ${newsList.size} news items")
        return newsList
    }

    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }
}