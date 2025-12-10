package api

import data.Auth
import kotlinx.serialization.json.Json
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class RedditApi {
    private val client = OkHttpClient()

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val userHome = System.getProperty("user.home")
    private val appDir = File(userHome, ".myred")

    init {
        val file = File(appDir, "token.json")
        if(!file.exists() || file.readText().isEmpty()){
            getAccessToken()
        }
    }

    fun getAccessToken() {
        val clientId = System.getenv("CLIENT_ID")
        val clientSecret = System.getenv("CLIENT_SECRET")
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

        val responseString = response.body.string()
        writeTokenToFile(responseString)
    }

    private fun writeTokenToFile(text: String){
        val file = File(appDir, "token.json")
        file.writeText(text)
        println("wrote access token to a file")
    }

    fun fetchNewPosts(accessToken: String, subreddit: String, limit: String = "10") : String {
        val userAgent = "script:MyRed:1.0 (by u/zikzikkh)"

        val request = Request.Builder()
            .url("https://oauth.reddit.com/r/$subreddit/new?limit=$limit")
            .header("Authorization", "Bearer $accessToken")
            .header("User-Agent", userAgent)
            .get()
            .build()

        val response = client.newCall(request).execute()
        val body = response.body.string()
        println("Hot kotlin posts response:\n$body")

        return body
    }

    fun fetchHotPosts(accessToken: String, subreddit: String, limit: String = "10") : String {
        val userAgent = "script:MyRed:1.0 (by u/zikzikkh)"

        val request = Request.Builder()
            .url("https://oauth.reddit.com/r/$subreddit/hot?limit=$limit")
            .header("Authorization", "Bearer $accessToken")
            .header("User-Agent", userAgent)
            .get()
            .build()

        val response = client.newCall(request).execute()
        val body = response.body.string()
        println("Hot kotlin posts response:\n$body")

        return body
    }
}