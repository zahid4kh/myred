package data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class AppSettings(
    val darkMode: Boolean = false,
    val lastTokenRefresh: Long = 0L
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
    val id: String = "",
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
    val createdUtc: Double = 0.0,
    val url: String = "",
    @SerialName("url_overridden_by_dest")
    val urlOverridenByDest: String = "",
    @SerialName("media_only")
    val mediaOnly: Boolean = false,
    val thumbnail: String = "",
    val preview: Preview? = null,
    @SerialName("gallery_data")
    val galleryData: GalleryData? = null,
    @SerialName("media_metadata")
    val mediaMetadata: Map<String, MediaMeta>? = null
)

fun ChildrenData.allVideoUrls(): List<String> {
    val result = mutableListOf<String>()

    if (urlOverridenByDest.isNotEmpty() && urlOverridenByDest.contains("redgifs.com/watch/", ignoreCase = true)) {
        result.add(urlOverridenByDest)
    }

    if (url.isNotEmpty() && url != urlOverridenByDest && url.contains("redgifs.com/watch/", ignoreCase = true)) {
        result.add(url)
    }

    if (urlOverridenByDest.isNotEmpty() && urlOverridenByDest.contains("v.redd.it")) {
        result.add(urlOverridenByDest)
    }

    if (url.isNotEmpty() && url != urlOverridenByDest && url.contains("v.redd.it")) {
        result.add(url)
    }

    if (isVideo) {
        if (urlOverridenByDest.isNotEmpty() &&
            !urlOverridenByDest.contains("v.redd.it") &&
            !urlOverridenByDest.contains("redgifs.com/watch/", ignoreCase = true) &&
            urlOverridenByDest.contains(Regex("\\.(mp4|webm|avi|mov)($|\\?)", RegexOption.IGNORE_CASE))) {
            result.add(urlOverridenByDest)
        }

        if (url.isNotEmpty() && url != urlOverridenByDest &&
            !url.contains("v.redd.it") &&
            !url.contains("redgifs.com/watch/", ignoreCase = true) &&
            url.contains(Regex("\\.(mp4|webm|avi|mov)($|\\?)", RegexOption.IGNORE_CASE))) {
            result.add(url)
        }
    }

    preview?.images?.forEach { img ->
        img.variants?.mp4?.source?.url?.let { mp4Url ->
            val cleanUrl = mp4Url.replace("&amp;", "&")
            result.add(cleanUrl)
        }
    }

    return result.distinct()
}

fun ChildrenData.allImageUrls(): List<String> {
    val result = mutableListOf<String>()

    fun getImageFilename(url: String): String {
        return url.substringAfterLast("/").substringBefore("?")
    }

    val seenFilenames = mutableSetOf<String>()

    if (urlOverridenByDest.isNotEmpty() && urlOverridenByDest.contains("i.redd.it") &&
        !urlOverridenByDest.contains("v.redd.it")) {
        result.add(urlOverridenByDest)
        seenFilenames.add(getImageFilename(urlOverridenByDest))
    }

    if (url.isNotEmpty() && url.contains("i.redd.it") && !url.contains("v.redd.it") &&
        url != urlOverridenByDest) {
        val filename = getImageFilename(url)
        if (!seenFilenames.contains(filename)) {
            result.add(url)
            seenFilenames.add(filename)
        }
    }

    if (galleryData != null && mediaMetadata != null) {
        val galleryUrls = galleryData.items.mapNotNull { item ->
            mediaMetadata[item.mediaId]?.s?.url?.replace("&amp;", "&")
        }
        galleryUrls.forEach { galleryUrl ->
            val filename = getImageFilename(galleryUrl)
            if (!seenFilenames.contains(filename)) {
                result.add(galleryUrl)
                seenFilenames.add(filename)
            }
        }
    }

    preview?.images?.forEach { img ->
        img.source?.url?.let { previewUrl ->
            val cleanUrl = previewUrl.replace("&amp;", "&")
            val hasRedGifs = url.contains("redgifs.com/watch/", ignoreCase = true) ||
                    urlOverridenByDest.contains("redgifs.com/watch/", ignoreCase = true)
            if (!cleanUrl.contains("v.redd.it") && !hasRedGifs) {
                val filename = getImageFilename(cleanUrl)
                if (!seenFilenames.contains(filename)) {
                    result.add(cleanUrl)
                    seenFilenames.add(filename)
                }
            }
        }
    }

    return result
}

fun ChildrenData.getLinkUrl(): String? {
    val hasImages = allImageUrls().isNotEmpty()
    return if (!hasImages) url.takeIf { it.isNotEmpty() } else null
}

@Serializable
data class Preview(
    val enabled: Boolean = false,
    val images: List<Image> = emptyList()
)

@Serializable
data class Image(
    val id: String = "",
    val resolutions: List<Resolution> = emptyList(),
    val source: Source? = null,
    val variants: Variants? = null
)


@Serializable
data class Variants(
    val gif: GifVariant? = null,
    val mp4: Mp4Variant? = null
)

@Serializable
data class GifVariant(
    val source: Source? = null,
    val resolutions: List<Resolution> = emptyList()
)

@Serializable
data class Mp4Variant(
    val source: Source? = null,
    val resolutions: List<Resolution> = emptyList()
)

@Serializable
data class Resolution(
    val height: Int = 0,
    val url: String = "",
    val width: Int = 0
)


@Serializable
data class Source(
    val height: Int = 0,
    val url: String = "",
    val width: Int = 0
)

@Serializable
data class GalleryData(
    val items: List<GalleryItem> = emptyList()
)

@Serializable
data class GalleryItem(
    @SerialName("media_id")
    val mediaId: String = "",
    val id: Long = 0L
)

@Serializable
data class MediaMeta(
    val status: String = "",
    val e: String = "",
    val m: String = "",
    val p: List<PreviewImage> = emptyList(),
    val s: FullImage? = null,
    val id: String = ""
)

@Serializable
data class PreviewImage(
    val x: Int = 0,
    val y: Int = 0,
    @SerialName("u")
    val url: String = ""
)

@Serializable
data class FullImage(
    val x: Int = 0,
    val y: Int = 0,
    @SerialName("u")
    val url: String = ""
)
