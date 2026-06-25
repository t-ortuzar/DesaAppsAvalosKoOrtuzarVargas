package com.example.desaappsavaloskoortuzarvargas.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import java.net.HttpURLConnection
import java.net.URL

/**
 * Represents a single news item from the Steam News API.
 * These are real announcements published by game developers on the Steam store page.
 */
data class SteamNewsItem(
    val gid: String,
    val title: String,
    val url: String,
    val author: String,
    val contents: String,
    val date: Long,        // Unix timestamp (seconds)
    val feedname: String,
    val appId: Int
) {
    /** ISO date string (YYYY-MM-DD) derived from the Unix timestamp. */
    val dateFormatted: String
        get() {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            return sdf.format(java.util.Date(date * 1000L))
        }

    /**
     * Guess the news category based on keywords in the title.
     */
    val category: String
        get() = when {
            title.contains(Regex("(?i)sale|discount|deal|off|free|gratis|oferta|descuento")) -> "discount"
            title.contains(Regex("(?i)dlc|expansion|content|pack|pass|season|update|patch|fix|hotfix|actualiz")) -> "update"
            title.contains(Regex("(?i)event|tournament|league|season|challenge|contest|community")) -> "event"
            title.contains(Regex("(?i)launch|release|now available|out now|lanzamiento|disponible")) -> "release"
            else -> "update"
        }
}

/**
 * Service to fetch real developer news from the Steam News API.
 *
 * Endpoint: https://api.steampowered.com/ISteamNews/GetNewsForApp/v2/
 * No API key required. Returns news in the `appnews.newsitems` array.
 *
 * Content is filtered to `steam_community_announcements` feed so only
 * developer-posted announcements are returned (not reviews or third-party articles).
 */
class SteamNewsService {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /** In-memory cache: appId → (items, fetchTimestampMs) */
    private val cache = mutableMapOf<Int, Pair<List<SteamNewsItem>, Long>>()
    private val CACHE_TTL_MS = 20 * 60 * 1000L   // 20 minutes

    /**
     * Fetch the latest developer announcements for a Steam app.
     * Results are cached for [CACHE_TTL_MS] milliseconds.
     *
     * @param appId   Steam application ID
     * @param count   Maximum number of items to return (default 5)
     * @return List of [SteamNewsItem], or null if the request failed
     */
    suspend fun getNewsForApp(appId: Int, count: Int = 5): List<SteamNewsItem>? =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val cached = cache[appId]
            if (cached != null && now - cached.second < CACHE_TTL_MS) {
                return@withContext cached.first
            }

            try {
                val urlStr = "https://api.steampowered.com/ISteamNews/GetNewsForApp/v2/" +
                    "?appid=$appId&count=$count&maxlength=1200&format=json" +
                    "&feeds=steam_community_announcements"
                val url = URL(urlStr)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                conn.setRequestProperty("Accept", "application/json")

                if (conn.responseCode != 200) return@withContext null

                val body = conn.inputStream.bufferedReader().readText()
                val root = json.parseToJsonElement(body).jsonObject
                val appNews = root["appnews"]?.jsonObject ?: return@withContext null
                val items = appNews["newsitems"]?.jsonArray ?: return@withContext null

                val newsItems = items.mapNotNull { element ->
                    try {
                        val obj = element.jsonObject
                        val gid = obj["gid"]?.jsonPrimitive?.content ?: ""
                        val title = obj["title"]?.jsonPrimitive?.content ?: return@mapNotNull null
                        val itemUrl = obj["url"]?.jsonPrimitive?.content ?: ""
                        val author = obj["author"]?.jsonPrimitive?.content ?: ""
                        val rawContents = obj["contents"]?.jsonPrimitive?.content ?: ""
                        val date = obj["date"]?.jsonPrimitive?.longOrNull ?: 0L
                        val feedname = obj["feedname"]?.jsonPrimitive?.content ?: ""
                        val itemAppId = obj["appid"]?.jsonPrimitive?.longOrNull?.toInt() ?: appId

                        SteamNewsItem(
                            gid = gid,
                            title = title,
                            url = itemUrl,
                            author = author,
                            contents = stripMarkup(rawContents),
                            date = date,
                            feedname = feedname,
                            appId = itemAppId
                        )
                    } catch (_: Exception) { null }
                }

                cache[appId] = Pair(newsItems, now)
                newsItems
            } catch (_: Exception) { null }
        }

    /**
     * Strip Steam-flavored BBCode and HTML markup from news content
     * so it displays as clean plain text.
     */
    private fun stripMarkup(raw: String): String {
        return raw
            .replace(Regex("\\[img\\][^\\[]*\\[/img\\]", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\[url=[^]]*\\]([^\\[]*)\\[/url\\]", RegexOption.IGNORE_CASE), "$1")
            .replace(Regex("\\[/?(b|i|u|h[1-6]|list|\\*|quote|code|strike|spoiler|previewyoutube)[^]]*\\]", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<[^>]+>"), "")
            .replace(Regex("&amp;"), "&")
            .replace(Regex("&lt;"), "<")
            .replace(Regex("&gt;"), ">")
            .replace(Regex("&quot;"), "\"")
            .replace(Regex("\\{STEAM_CLAN_IMAGE}[^ \\n]*"), "")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }
}

