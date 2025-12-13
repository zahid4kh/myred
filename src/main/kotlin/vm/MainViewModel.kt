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
import data.allVideoUrls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.imageio.IIOImage
import javax.imageio.ImageIO

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

    private fun isDownloadableImageUrl(url: String): Boolean {
        if (url.contains("i.redd.it")) return true

        if (url.contains("preview.redd.it")) {
            val hasImageExtension = url.contains(Regex("\\.(jpg|jpeg|png|gif|webp)($|\\?)", RegexOption.IGNORE_CASE))
            val hasImageFormat = url.contains("format=pjpg") || url.contains("format=png") || url.contains("format=jpg")
            return hasImageExtension || hasImageFormat
        }

        if (url.contains("i.imgur.com")) return true

        if (url.contains(Regex("\\.(jpg|jpeg|png|gif|webp)($|\\?)", RegexOption.IGNORE_CASE))) {
            return true
        }

        return false
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
                subreddit = "VideoPorn"
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
            val imageUrls = child.data.allImageUrls()
            val videoUrls = child.data.allVideoUrls()
            val allUrls = imageUrls + videoUrls

            if (allUrls.isNotEmpty()) {
                val postId = child.data.id
                downloadImagesForPost(subreddit, postId, allUrls)
            }
        }
    }

    fun createResizedGif(originalFile: File, maxSizeMB: Int = 20): File? {
        try {
            val fileSizeMB = originalFile.length() / (1024 * 1024)
            if (fileSizeMB <= maxSizeMB) return originalFile

            val resizedFile = File(originalFile.parent, "${originalFile.nameWithoutExtension}_resized.gif")
            if (resizedFile.exists()) return resizedFile

            val readers = ImageIO.getImageReadersByFormatName("gif")
            if (!readers.hasNext()) return null

            val reader = readers.next()
            val input = ImageIO.createImageInputStream(originalFile)
            reader.input = input

            val writers = ImageIO.getImageWritersByFormatName("gif")
            if (!writers.hasNext()) return null

            val writer = writers.next()
            val output = ImageIO.createImageOutputStream(resizedFile)
            writer.output = output

            val numFrames = reader.getNumImages(true)
            val originalWidth = reader.getWidth(0)
            val originalHeight = reader.getHeight(0)

            val scaleFactor = if (fileSizeMB > 50) 0.3 else 0.5
            val newWidth = (originalWidth * scaleFactor).toInt()
            val newHeight = (originalHeight * scaleFactor).toInt()

            writer.prepareWriteSequence(null)

            for (i in 0 until numFrames) {
                val frame = reader.read(i)
                val metadata = reader.getImageMetadata(i)

                val resizedFrame = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB)
                val g = resizedFrame.createGraphics()
                g.drawImage(frame.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), 0, 0, null)
                g.dispose()

                writer.writeToSequence(IIOImage(resizedFrame, null, metadata), null)
            }

            writer.endWriteSequence()
            writer.dispose()
            reader.dispose()
            input.close()
            output.close()

            println("Resized GIF: ${originalFile.name} (${fileSizeMB}MB) -> ${resizedFile.name} (${resizedFile.length() / (1024 * 1024)}MB)")
            return resizedFile

        } catch (e: Exception) {
            println("Failed to resize GIF: ${e.message}")
            return null
        }
    }

    private fun downloadImagesForPost(subreddit: String, postId: String, urls: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val saveDir = File("$appDir/$subreddit/images/$postId")
            if (!saveDir.exists()) saveDir.mkdirs()

            val downloadableUrls = urls.filter { url ->
                isDownloadableImageUrl(url) || isDownloadableVideoUrl(url)
            }

            downloadableUrls.forEachIndexed { index, url ->
                try {
                    var actualUrl = url.replace("&amp;", "&")
                    var extension = "jpg"
                    var isVideo = false

                    if (isRedditVideoUrl(actualUrl)) {
                        val videoUrl = getRedditVideoUrl(actualUrl)
                        if (videoUrl != null) {
                            actualUrl = videoUrl
                            extension = "mp4"
                            isVideo = true
                        } else {
                            println("Failed to get video URL for: $actualUrl")
                            return@forEachIndexed
                        }
                    } else {
                        extension = when {
                            actualUrl.contains(".mp4", ignoreCase = true) -> {
                                isVideo = true
                                "mp4"
                            }
                            actualUrl.contains(".webm", ignoreCase = true) -> {
                                isVideo = true
                                "webm"
                            }
                            actualUrl.contains(".gif", ignoreCase = true) -> "gif"
                            actualUrl.contains(".webp", ignoreCase = true) -> "webp"
                            actualUrl.contains(".png", ignoreCase = true) -> "png"
                            actualUrl.contains(".jpg", ignoreCase = true) -> "jpg"
                            actualUrl.contains(".jpeg", ignoreCase = true) -> "jpeg"
                            actualUrl.contains("format=pjpg") -> "jpg"
                            actualUrl.contains("format=png") -> "png"
                            else -> "jpg"
                        }
                    }

                    if (!isDownloadableImageUrl(actualUrl) && !isDownloadableVideoUrl(actualUrl) && !isRedditVideoUrl(url)) {
                        println("Skipping non-downloadable URL: $actualUrl")
                        return@forEachIndexed
                    }

                    val prefix = if (isVideo) "video" else "image"
                    val file = File(saveDir, "${prefix}_${index + 1}.$extension")

                    if (file.exists()) {
                        println("File already exists: ${file.name}")
                        return@forEachIndexed
                    }

                    println("Downloading ${if (isVideo) "video" else "image"}: $actualUrl")

                    val request = Request.Builder()
                        .url(actualUrl)
                        .header("User-Agent", "script:MyRed:1.0 (by u/zikzikkh)")
                        .build()

                    httpClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val contentType = response.header("Content-Type") ?: ""
                            if (contentType.startsWith("image/") || contentType.startsWith("video/") || isVideo) {
                                file.outputStream().use { out ->
                                    response.body?.byteStream()?.copyTo(out)
                                }
                                println("Saved ${if (isVideo) "video" else "image"} to ${file.path}")

                                if (extension == "gif" && !isVideo) {
                                    val resizedGif = createResizedGif(file)
                                    if (resizedGif != null && resizedGif != file) {
                                        file.delete()
                                        resizedGif.renameTo(file)
                                        println("Replaced with resized version: ${file.name}")
                                    }
                                }
                            } else {
                                println("URL didn't return expected content type (Content-Type: $contentType): $actualUrl")
                            }
                        } else {
                            println("Failed to download $actualUrl (${response.code}): ${response.message}")
                        }
                    }
                } catch (e: Exception) {
                    println("Error downloading from $url: ${e.message}")
                }
            }
        }
    }

    private suspend fun getRedditVideoUrl(vRedditUrl: String): String? {
        return try {
            val videoId = vRedditUrl.substringAfterLast("/")

            val possibleUrls = listOf(
                "https://v.redd.it/$videoId/DASH_1080.mp4",
                "https://v.redd.it/$videoId/DASH_720.mp4",
                "https://v.redd.it/$videoId/DASH_480.mp4",
                "https://v.redd.it/$videoId/DASH_360.mp4",
                "https://v.redd.it/$videoId/DASH_240.mp4"
            )

            for (testUrl in possibleUrls) {
                val testRequest = Request.Builder()
                    .url(testUrl)
                    .head()
                    .header("User-Agent", "script:MyRed:1.0 (by u/zikzikkh)")
                    .build()

                httpClient.newCall(testRequest).execute().use { testResponse ->
                    if (testResponse.isSuccessful) {
                        val contentLength = testResponse.header("Content-Length")?.toLongOrNull() ?: 0
                        if (contentLength > 10000) {
                            println("Found Reddit video URL: $testUrl")
                            return testUrl
                        }
                    }
                }
            }

            val apiUrl = "https://www.reddit.com/video/$videoId.json"
            val apiRequest = Request.Builder()
                .url(apiUrl)
                .header("User-Agent", "script:MyRed:1.0 (by u/zikzikkh)")
                .build()

            httpClient.newCall(apiRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val videoUrlRegex = Regex("\"fallback_url\"\\s*:\\s*\"([^\"]+)\"")
                        val matchResult = videoUrlRegex.find(responseBody)
                        val fallbackUrl = matchResult?.groupValues?.get(1)?.replace("\\", "")

                        if (fallbackUrl != null) {
                            println("Found Reddit fallback video URL: $fallbackUrl")
                            return fallbackUrl
                        }
                    }
                }
            }

            null
        } catch (e: Exception) {
            println("Error getting Reddit video URL: ${e.message}")
            null
        }
    }

    private fun isRedditVideoUrl(url: String): Boolean {
        return url.contains("v.redd.it")
    }

    private fun isDownloadableVideoUrl(url: String): Boolean {
        return url.contains("v.redd.it") ||
                url.contains(Regex("\\.(mp4|webm|avi|mov)($|\\?)", RegexOption.IGNORE_CASE))
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

    fun showImageFullScreen(img: File){
        _uiState.update {
            it.copy(
                clickedImage = img
            )
        }
        println("SELECTED IMAGE FOR FULL SCREEN:\n${img.absolutePath}")
    }

    fun exitFullScreenImage(){
        _uiState.update { it.copy(clickedImage = null) }
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
        val showSubredditPostBatches: Boolean = false,
        val clickedImage: File? = null
    )

    data class FetchedSubreddit(
        val subredditFolder: File? = null,
        val isExtended: Boolean = false
    )
}