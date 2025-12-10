package vm

import data.Database
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import api.RedditApi
import data.Auth
import data.RedditResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
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

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

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
        viewModelScope.launch {
            val token = getAccessToken()
            val redditResponse = api.fetchHotPosts(
                accessToken = token,
                subreddit = "food"
            )
            val decodedRedditResponse = json.decodeFromString<RedditResponse>(redditResponse)
            println("Fetched ${decodedRedditResponse.data.children.size} posts")

            val encodeDecoded = json.encodeToString<RedditResponse>(decodedRedditResponse)
            val subreddit = decodedRedditResponse.data.children.first().data.subreddit

            saveFetchedPosts(
                subreddit = subreddit,
                text = redditResponse
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
    }

    fun onSelectSubredditFolder(folder: File){
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedSubredditFolder = folder
                )
            }

            listBatches()
        }
    }

    private fun listBatches(){
        val batches = _uiState.value.selectedSubredditFolder?.listFiles()?.toList()
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
                selectedSubredditBatch = mapOf(batch to decodedContent),
                availableSubredditsDialogShown = false
            )
        }
    }

    fun toggleShowPostBatches(){
        _uiState.update {
            it.copy(showSubredditPostBatches = !it.showSubredditPostBatches)
        }
    }

    fun updateFetchedSubreddits(){
        val files = appDir.listFiles()
        files.forEach { file->
            if(file.isDirectory){
                val list = mutableListOf<File>()
                list.add(file)
                _uiState.update {
                    it.copy(fetchedSubreddits = list)
                }
            }
        }
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
        val selectedSubredditFolder: File? = null,
        val selectedSubredditBatch: Map<File, RedditResponse>? = null,
        val fetchedSubreddits: List<File> = mutableListOf(),
        val fetchedPostBatches: List<File> = mutableListOf(),
        val availableSubredditsDialogShown: Boolean = false,
        val showSubredditPostBatches: Boolean = false
    )
}