package data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class AppSettings(
    val darkMode: Boolean = false
)

@Serializable
data class Auth(
    @SerialName("access_token")
    val accessToken: String = "",
    @SerialName("token_type")
    val tokenType: String = "",
    @SerialName("expires_in")
    val expiresIn: Int = 0,
    val scope: String = ""
)

@Serializable
data class RedditResponse(
    val kind: String = "",
    val data: Data = Data()
)

@Serializable
data class Data(
    val after: String = "",
    val children: List<Children> = emptyList()
)


@Serializable
data class Children(
    val kind: String = "",
    val data: ChildrenData = ChildrenData()
)


@Serializable
data class ChildrenData(
    val subreddit: String = "",
    val selftext: String = "",
    @SerialName("author_fullname")
    val authorFullName: String = "",
    val title: String = "",
    @SerialName("subreddit_name_prefixed")
    val subredditNamePrefixed: String = "",
    @SerialName("subreddit_type")
    val subredditType: String = "",
    val ups: Int = 0,
    val downs: Int = 0,
    @SerialName("created")
    val createdAt: Double = 0.0,
    @SerialName("over_18")
    val over18: Boolean = false,
    val author: String = "",
    @SerialName("is_video")
    val isVideo: Boolean = false,
    @SerialName("created_utc")
    val createdUtc: Double = 0.0
)