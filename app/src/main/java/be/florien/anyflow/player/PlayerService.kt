package be.florien.anyflow.player

import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LifecycleService
import androidx.media.session.MediaButtonReceiver
import be.florien.anyflow.AnyFlowApp
import javax.inject.Inject


/**
 * Service used to handle the media player.
 */
class PlayerService : LifecycleService() {

    /**
     * Injection
     */

    @Inject
    internal lateinit var playerController: PlayerController

    @Inject
    internal lateinit var playingQueue: PlayingQueue

    /**
     * Fields
     */

    private val iBinder = LocalBinder()
    private val pendingIntent: PendingIntent by lazy {
        val intent = packageManager?.getLaunchIntentForPackage(packageName)
        PendingIntent.getActivity(this@PlayerService, 0, intent, 0)
    }
    private val mediaSession: MediaSessionCompat by lazy {
        MediaSessionCompat(this, MEDIA_SESSION_NAME).apply {
            setSessionActivity(pendingIntent)
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onSeekTo(pos: Long) {
                    playerController.seekTo(pos)
                }

                override fun onSkipToPrevious() {
                    playingQueue.listPosition--
                }

                override fun onPlay() {
                    playerController.resume()
                }

                override fun onSkipToNext() {
                    playingQueue.listPosition++
                }

                override fun onPause() {
                    playerController.pause()
                }
            })
            isActive = true
        }
    }
    private val notificationBuilder: PlayerNotificationBuilder by lazy { PlayerNotificationBuilder(this, mediaSession) }

    private val isPlaying
        get() = playerController.isPlaying()

    private val hasPrevious
        get() = playingQueue.listPosition > 0

    private val hasNext
        get() = playingQueue.listPosition < playingQueue.queueSize - 1

    /**
     * Lifecycle
     */

    override fun onCreate() {
        super.onCreate()
        (application as AnyFlowApp).userComponent?.inject(this)

        playerController.stateChangeNotifier.observe(this) {
            if (!isPlaying) {
                stopForeground(false)
            }
            val playbackState = when (it) {
                PlayerController.State.BUFFER -> PlaybackStateCompat.STATE_BUFFERING
                PlayerController.State.RECONNECT -> PlaybackStateCompat.STATE_BUFFERING
                PlayerController.State.PLAY -> PlaybackStateCompat.STATE_PLAYING
                PlayerController.State.PAUSE -> PlaybackStateCompat.STATE_PAUSED
                else -> PlaybackStateCompat.STATE_NONE
            }

            notificationBuilder.lastPlaybackState = playbackState
            notificationBuilder.updateNotification(isPlaying, hasPrevious, hasNext)
        }
        playerController.playTimeNotifier.observe(this) {
            notificationBuilder.lastPosition = it
        }
        playingQueue.currentSong.observe(this) {
            notificationBuilder.updateMediaSession(it)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return iBinder
    }

    /**
     * Inner classes
     */

    inner class LocalBinder : Binder() {
        val service: PlayerController
            get() = playerController
    }

    /**
     * Private Methods
     */

    companion object {
        const val MEDIA_SESSION_NAME = "AnyFlow player"
    }
}