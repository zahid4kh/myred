package testingVideoPlayer

import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent

class VideoPlayerService {
    private var mediaPlayerComponent: EmbeddedMediaPlayerComponent? = null
    private var currentUrl: String? = null

    fun createPlayer(url: String): EmbeddedMediaPlayerComponent {
        val component = EmbeddedMediaPlayerComponent()
        mediaPlayerComponent = component
        currentUrl = url

        component.mediaPlayer().media().prepare(url)

        return component
    }

    fun getPlayerManager(): PlayerManager {
        val component = mediaPlayerComponent ?: throw IllegalStateException("Player not created")

        return object : PlayerManager {
            private val player = component.mediaPlayer()

            override fun play() {
                if (player.media().isValid) {
                    player.controls().play()
                } else {
                    currentUrl?.let { url -> player.media().play(url) }
                }
            }

            override fun pause() {
                player.controls().pause()
            }

            override fun restart() {
                player.controls().stop()
                currentUrl?.let { url -> player.media().play(url) }
            }

            override fun isPlaying(): Boolean {
                return player.status().isPlaying
            }
        }
    }

    fun release() {
        mediaPlayerComponent?.mediaPlayer()?.release()
        mediaPlayerComponent = null
        currentUrl = null
    }
}