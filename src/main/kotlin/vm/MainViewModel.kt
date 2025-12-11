package vm

import data.Database
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import api.RedditApi
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.fetch.Fetcher
import com.github.panpf.sketch.fetch.OkHttpHttpUriFetcher
import data.Auth
import data.RedditResponse
import data.allImageUrls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainViewModel(
    private val database: Database,
    private val api: RedditApi
): ViewModel() {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _fetchedSubreddit = MutableStateFlow(FetchedSubreddit())
    val fetchedSubreddit = _fetchedSubreddit.asStateFlow()

    private val userHome = System.getProperty("user.home")
    private val appDir = File(userHome, ".myred")

    init {
        viewModelScope.launch {
            val settings = database.getSettings()
            _uiState.value = _uiState.value.copy(
                darkMode = settings.darkMode,
            )
        }
    }

    private fun getAccessToken() : String {
        val tokenFile = File(appDir, "token.json")
        val token = json.decodeFromString<Auth>(tokenFile.readText()).accessToken
        return token
    }

    fun getHotPosts() {
        viewModelScope.launch(Dispatchers.IO) {
            val token = getAccessToken()
            val redditResponse = api.fetchHotPosts(
                accessToken = token,
                subreddit = "boxingcirclejerk"
            )
            val decodedRedditResponse = json.decodeFromString<RedditResponse>(redditResponse)
            println("Fetched ${decodedRedditResponse.data.children.size} posts")

            val encodeDecoded = json.encodeToString<RedditResponse>(decodedRedditResponse)
            val subreddit = decodedRedditResponse.data.children.first().data.subreddit

            saveFetchedPosts(
                subreddit = subreddit,
                text = encodeDecoded
            )
        }
    }

    private fun saveFetchedPosts(subreddit: String, text: String){
        val saveTime = System.currentTimeMillis()
        val formattedSaveTime = formatMillis(saveTime)
        val saveDir = File("$appDir/$subreddit")
        if(!saveDir.exists()) saveDir.mkdirs()

        val file = File("$saveDir/$formattedSaveTime.json")
        file.writeText(text)

        val decoded = json.decodeFromString<RedditResponse>(text)

        decoded.data.children.forEach { child ->
            val urls = child.data.allImageUrls()

            if (urls.isNotEmpty()) {
                val postId = child.data.id
                downloadImagesForPost(subreddit, postId, urls)
            }
        }
    }

    private fun downloadImagesForPost(subreddit: String, postId: String, urls: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val saveDir = File("$appDir/$subreddit/images/$postId")
            if (!saveDir.exists()) saveDir.mkdirs()

            urls.forEachIndexed { index, url ->
                try {
                    var sanitizedUrl = url.replace("&amp;", "&")

                    if (sanitizedUrl.contains("preview.redd.it") && sanitizedUrl.contains("format=")) {
                        sanitizedUrl = sanitizedUrl.replace(Regex("[?&]format=[^&]*"), "")
                        sanitizedUrl = sanitizedUrl.replace(Regex("[?&]$"), "")
                    }

                    val extension = when {
                        sanitizedUrl.contains(".gif", ignoreCase = true) -> "gif"
                        sanitizedUrl.contains(".webp", ignoreCase = true) -> "webp"
                        sanitizedUrl.contains(".png", ignoreCase = true) -> "png"
                        sanitizedUrl.contains(".jpg", ignoreCase = true) -> "jpg"
                        sanitizedUrl.contains(".jpeg", ignoreCase = true) -> "jpeg"
                        else -> "jpg"
                    }

                    val file = File(saveDir, "image_${index + 1}.$extension")

                    if (file.exists()) {
                        println("File already exists: ${file.path}")
                        return@forEachIndexed
                    }

                    println("Downloading: $sanitizedUrl")

                    val request = Request.Builder()
                        .url(sanitizedUrl)
                        .header("User-Agent", "script:MyRed:1.0 (by u/username)")
                        .build()

                    httpClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            file.outputStream().use { out ->
                                response.body?.byteStream()?.copyTo(out)
                            }
                            println("Saved image to ${file.path}")
                        } else {
                            println("Failed to download $sanitizedUrl (${response.code})")
                        }
                    }
                } catch (e: java.net.SocketTimeoutException) {
                    println("Timeout downloading image from $url - skipping")
                } catch (e: Exception) {
                    println("Error downloading image from $url: ${e.message}")
                }
            }
        }
    }

    private fun listBatches(){
        val batches = _uiState.value.selectedSubreddit?.subredditFolder?.listFiles()?.toList()
        println("Found ${batches?.size} batches")
        val sorted = batches?.sortedByDescending { file ->
            Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
                .creationTime().toMillis()
        }

        sorted?.let { list ->
            _uiState.update {
                it.copy(
                    fetchedPostBatches = list
                )
            }
        }
    }

    fun loadSelectedBatch(batch: File){
        val fileContent = batch.readText()
        val decodedContent = json.decodeFromString<RedditResponse>(fileContent)
        _uiState.update {
            it.copy(
                selectedSubredditBatch = mapOf(batch to decodedContent),
                availableSubredditsDialogShown = false
            )
        }
    }

    fun loadTestBatch(){
        val batch = File("test.json")
        val fileContent = batch.readText()
        val decodedContent = json.decodeFromString<RedditResponse>(fileContent)
        _uiState.update {
            it.copy(
                selectedSubredditBatch = mapOf(batch to decodedContent)
            )
        }
    }

    private fun updateFetchedSubreddits(){
        val files = appDir.listFiles()
        val list = mutableListOf<FetchedSubreddit>()
        files.forEach { file->
            if(file.isDirectory){
                val sub = _fetchedSubreddit.value.copy(
                    subredditFolder = file,
                    isExtended = false
                )

                list.add(sub)
                _uiState.update {
                    it.copy(fetchedSubreddits = list)
                }
            }
        }
    }

    fun toggleSubredditExtended(sub: FetchedSubreddit) {
        _uiState.update {
            it.copy(
                selectedSubreddit = sub
            )
        }

        _uiState.update { currentState ->
            val updatedList = currentState.fetchedSubreddits.map { fetchedSub ->
                if (fetchedSub.subredditFolder?.absolutePath == sub.subredditFolder?.absolutePath) {
                    fetchedSub.copy(isExtended = !fetchedSub.isExtended)
                } else {
                    fetchedSub.copy(isExtended = false)
                }
            }
            currentState.copy(fetchedSubreddits = updatedList)
        }

        listBatches()
    }

    fun showAvailableSubredditsDialog(){
        viewModelScope.launch {
            _uiState.update { it.copy(
                availableSubredditsDialogShown = true
            ) }

            updateFetchedSubreddits()
        }
    }

    fun closeAvailableSubredditsDialog(){
        _uiState.update { it.copy(
            availableSubredditsDialogShown = false,
            showSubredditPostBatches = false
        ) }
    }

    private fun formatMillis(ms: Long): String {
        val formatter = DateTimeFormatter.ofPattern("MMMMdd-HH_mm_ss")
            .withZone(ZoneId.of("Europe/Berlin"))

        return formatter.format(Instant.ofEpochMilli(ms))
    }


    fun toggleDarkMode() {
        val newDarkMode = !_uiState.value.darkMode
        _uiState.value = _uiState.value.copy(darkMode = newDarkMode)

        viewModelScope.launch {
            val settings = database.getSettings()
            database.saveSettings(settings.copy(darkMode = newDarkMode))
        }
    }

    data class UiState(
        val darkMode: Boolean = true,
        val loadedSubreddit: String = "",
        val selectedSubreddit: FetchedSubreddit? = null,
        val selectedSubredditBatch: Map<File, RedditResponse>? = null,
        val fetchedSubreddits: List<FetchedSubreddit> = mutableListOf(),
        val fetchedPostBatches: List<File> = mutableListOf(),
        val availableSubredditsDialogShown: Boolean = false,
        val showSubredditPostBatches: Boolean = false
    )

    data class FetchedSubreddit(
        val subredditFolder: File? = null,
        val isExtended: Boolean = false
    )
}