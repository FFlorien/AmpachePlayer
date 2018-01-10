package be.florien.ampacheplayer.view.player

import android.content.ComponentName
import android.content.ServiceConnection
import android.databinding.Bindable
import android.os.IBinder
import be.florien.ampacheplayer.BR
import be.florien.ampacheplayer.player.AudioQueueManager
import be.florien.ampacheplayer.player.NO_CURRENT_SONG
import be.florien.ampacheplayer.player.DummyPlayerController
import be.florien.ampacheplayer.player.PlayerController
import be.florien.ampacheplayer.player.PlayerService
import be.florien.ampacheplayer.view.BaseVM
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the PlayerActivity
 */
class PlayerActivityVM
@Inject
constructor(private val audioQueueManager: AudioQueueManager) : BaseVM() {
    private val playerControllerIdentifierBase = "playerControllerId"

    private var playerControllerNumber = 0
    internal val connection: PlayerConnection = PlayerConnection()
    private var isBackKeyPreviousSong: Boolean = false
    var player: PlayerController = DummyPlayerController()

    private var playBackTime: Long = 0L

    /**
     * Constructor
     */
    init {
        Timber.tag(this.javaClass.simpleName)
    }

    fun play() {
        player.play()
    }

    fun playPause() {
        player.apply {
            if (isPlaying()) {
                pause()
            } else {
                resume()
            }
        }
    }

    fun next() {
        audioQueueManager.listPosition += 1
    }

    fun replayOrPrevious() {
        if (isBackKeyPreviousSong) {
            audioQueueManager.listPosition -= 1
        } else {
            player.play()
        }
    }

    @Bindable
    fun getCurrentDuration(): Int {
        return playBackTime.toInt()
    }

    @Bindable
    fun getTotalDuration(): Int {
        return audioQueueManager.getCurrentSong().time
    }

    @Bindable
    fun getPlayTimeDisplay(): String {
        val minutesDisplay = String.format("%02d", (playBackTime / 60))
        val secondsDisplay = String.format("%02d", (playBackTime % 60))
        return "$minutesDisplay:$secondsDisplay"
    }

    @Bindable
    fun isNextPossible(): Boolean = audioQueueManager.listPosition < audioQueueManager.itemsCount - 1 && audioQueueManager.listPosition != NO_CURRENT_SONG

    @Bindable
    fun isPreviousPossible(): Boolean = audioQueueManager.listPosition != 0 || playBackTime > 10


    /**
     * Private methods
     */

    private fun initController(controller: PlayerController) {
        player = controller
        playerControllerNumber += 1
        subscribe(
                observable = audioQueueManager.positionObservable.observeOn(AndroidSchedulers.mainThread()),
                onNext = {
                    notifyPropertyChanged(BR.nextPossible)
                    notifyPropertyChanged(BR.previousPossible)
                    notifyPropertyChanged(BR.totalDuration)
                })
        subscribe(
                observable = player.playTimeNotifier,
                onNext = {
                    playBackTime = it / 1000
                    isBackKeyPreviousSong = playBackTime < 10
                    notifyPropertyChanged(BR.previousPossible)
                    notifyPropertyChanged(BR.playTimeDisplay)
                    notifyPropertyChanged(BR.currentDuration)
                },
                onError = {
                    Timber.e(it, "error while retrieving the playtime")
                })
        subscribe(
                observable = player.songNotifier,
                onNext = {
                    //todo display song played
                },
                onError = {
                    Timber.e(it, "error while displaying the current song")
                },
                containerKey = playerControllerIdentifierBase + playerControllerNumber)
    }

    /**
     * Inner class
     */
    inner class PlayerConnection : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            dispose(playerControllerIdentifierBase + playerControllerNumber)
            initController(DummyPlayerController())
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            dispose(playerControllerIdentifierBase + playerControllerNumber)
            initController((service as PlayerService.LocalBinder).service)
        }
    }
}