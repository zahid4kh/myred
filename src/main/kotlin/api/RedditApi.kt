package api

import data.Auth
import data.Database
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class RedditApi(
    private val database: Database
) {
    private val client = OkHttpClient()

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val userHome = System.getProperty("user.home")
    private val appDir = File(userHome, ".myred")

    init {
        if (!appDir.exists()) {
            appDir.mkdirs()
        }

        if (needsTokenRefresh()) {
            println("Token expired or missing, refreshing...")
            getAccessToken()
        } else {
            println("Token is still valid")
        }
    }

    private fun needsTokenRefresh(): Boolean {
        val tokenFile = File(appDir, "token.json")

        if (!tokenFile.exists() || tokenFile.readText().isEmpty()) {
            println("Token file missing or empty")
            return true
        }

        return try {
            val settings = runBlocking { database.getSettings() }
            val lastRefresh = settings.lastTokenRefresh

            if (lastRefresh == 0L) {
                println("No refresh timestamp found")
                return true
            }

            val auth = json.decodeFromString<Auth>(tokenFile.readText())
            val expiresIn = auth.expiresIn // seconds

            val currentTime = System.currentTimeMillis() / 1000
            val tokenAge = currentTime - (lastRefresh / 1000)

            val bufferTime = 300
            val needsRefresh = tokenAge >= (expiresIn - bufferTime)

            if (needsRefresh) {
                println("Token expires in ${expiresIn - tokenAge} seconds, refreshing...")
            } else {
                println("Token is valid for ${expiresIn - tokenAge} more seconds")
            }

            needsRefresh
        } catch (e: Exception) {
            println("Error checking token expiration: ${e.message}")
            true
        }
    }

    fun getAccessToken() {
        try {
            val clientId = System.getenv("CLIENT_ID")
            val clientSecret = System.getenv("CLIENT_SECRET")

            if (clientId.isNullOrEmpty() || clientSecret.isNullOrEmpty()) {
                println("ERROR: CLIENT_ID and CLIENT_SECRET environment variables must be set!")
                return
            }

            val userAgent = "script:MyRed:1.0 (by u/zikzikkh)"
            val formBody = FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build()

            val request = Request.Builder()
                .url("https://www.reddit.com/api/v1/access_token")
                .header("Authorization", Credentials.basic(clientId, clientSecret))
                .header("User-Agent", userAgent)
                .post(formBody)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseString = response.body?.string() ?: ""
                writeTokenToFile(responseString)
                updateLastRefreshTime()
                println("Access token refreshed successfully")
            } else {
                println("Failed to get access token: ${response.code} ${response.message}")
            }
        } catch (e: Exception) {
            println("Error getting access token: ${e.message}")
        }
    }

    private fun writeTokenToFile(text: String){
        val file = File(appDir, "token.json")
        file.writeText(text)
        println("wrote access token to a file")
    }

    private fun updateLastRefreshTime() {
        runBlocking {
            try {
                val currentSettings = database.getSettings()
                val updatedSettings = currentSettings.copy(
                    lastTokenRefresh = System.currentTimeMillis()
                )
                database.saveSettings(updatedSettings)
                println("Updated last refresh timestamp")
            } catch (e: Exception) {
                println("Error updating last refresh time: ${e.message}")
            }
        }
    }

    private fun getValidAccessToken(): String {
        if (needsTokenRefresh()) {
            println("Token expired, refreshing before use...")
            getAccessToken()
        }

        val tokenFile = File(appDir, "token.json")
        return if (tokenFile.exists() && tokenFile.readText().isNotEmpty()) {
            val token = json.decodeFromString<Auth>(tokenFile.readText())
            token.accessToken
        } else {
            throw IllegalStateException("No valid access token available")
        }
    }

    fun fetchHotPosts(accessToken: String, subreddit: String, limit: String = "10", after: String? = null): String {
        val userAgent = "script:MyRed:1.0 (by u/zikzikkh)"

        var url = "https://oauth.reddit.com/r/$subreddit/hot?limit=$limit"
        if (!after.isNullOrEmpty()) {
            url += "&after=$after"
        }

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $accessToken")
            .header("User-Agent", userAgent)
            .get()
            .build()

        val response = client.newCall(request).execute()
        val body = response.body.string()
        println("Hot $subreddit posts response (after: $after):\n$body")

        if (response.code == 401) {
            println("Token appears to be expired (401), forcing refresh...")
            getAccessToken()
            throw IllegalStateException("Token expired, please retry request")
        }

        return body
    }

    fun fetchNewPosts(accessToken: String, subreddit: String, limit: String = "10", after: String? = null): String {
        val userAgent = "script:MyRed:1.0 (by u/zikzikkh)"

        var url = "https://oauth.reddit.com/r/$subreddit/new?limit=$limit"
        if (!after.isNullOrEmpty()) {
            url += "&after=$after"
        }

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $accessToken")
            .header("User-Agent", userAgent)
            .get()
            .build()

        val response = client.newCall(request).execute()
        val body = response.body.string()
        println("New $subreddit posts response (after: $after):\n$body")

        if (response.code == 401) {
            println("Token appears to be expired (401), forcing refresh...")
            getAccessToken()
            throw IllegalStateException("Token expired, please retry request")
        }

        return body
    }
}