package be.florien.ampacheplayer.view.player.songlist

import android.content.ComponentName
import android.content.ServiceConnection
import android.databinding.Bindable
import android.os.IBinder
import be.florien.ampacheplayer.BR
import be.florien.ampacheplayer.di.ActivityScope
import be.florien.ampacheplayer.persistence.local.LocalDataManager
import be.florien.ampacheplayer.persistence.local.model.Song
import be.florien.ampacheplayer.player.AudioQueue
import be.florien.ampacheplayer.player.DummyPlayerController
import be.florien.ampacheplayer.player.PlayerController
import be.florien.ampacheplayer.player.PlayerService
import be.florien.ampacheplayer.view.BaseVM
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

/**
 * Display a list of accounts and play it upon selection.
 */

@ActivityScope
class SongListFragmentVm
@Inject constructor(
//        private val persistenceManager: PersistenceManager,
        private val audioQueueManager: AudioQueue,
        private val localDataManager: LocalDataManager
//        private val navigator: Navigator,
//        private val displayHelper: DisplayHelper
) : BaseVM() {

    @get:Bindable
    var isLoadingAll: Boolean = false
        set(value) {
            notifyPropertyChanged(BR.loadingAll)
            field = value
        }

    var player: PlayerController = DummyPlayerController()

    internal var connection: PlayerConnection = PlayerConnection()
    private val songList = mutableListOf<Song>()

    /**
     * Constructor
     */
    init {
        Timber.tag(this.javaClass.simpleName)
        subscribe(audioQueueManager.positionObservable.observeOn(AndroidSchedulers.mainThread()), onNext = {
            notifyPropertyChanged(BR.listPosition)
            notifyPropertyChanged(BR.currentSong)
        })
        subscribe(localDataManager.getSongs().observeOn(AndroidSchedulers.mainThread()), onNext =  {
            songList.clear()
            songList.addAll(it)
            notifyPropertyChanged(BR.currentAudioQueue)
        })
    }

    /**
     * Public methods
     */
    @Bindable
    fun getCurrentAudioQueue() = songList

    @Bindable
    fun getCurrentSong() = if (songList.size > 0) {
        songList[0]
    } else {
        Song()
    }

    @Bindable
    fun getListPosition() = 0

    fun refreshSongs() {
        isLoadingAll = audioQueueManager.itemsCount == 0

//        subscribe(
//                persistenceManager.updateSongs().subscribeOn(AndroidSchedulers.mainThread()),
//                { _ ->
//                    isLoadingAll = false
//                    notifyPropertyChanged(BR.currentAudioQueue)
//                },
//                { throwable ->
//                    isLoadingAll = false
//                    when (throwable) {
//                        is SessionExpiredException -> {
//                            Timber.i(throwable, "The session token is expired")
//                            navigator.goToConnection()
//                        }
//                        is WrongIdentificationPairException -> {
//                            Timber.i(throwable, "Couldn't reconnect the user: wrong user/pwd")
//                            navigator.goToConnection()
//                        }
//                        is SocketTimeoutException, is NoServerException -> {
//                            Timber.e(throwable, "Couldn't connect to the webservice")
//                            displayHelper.notifyUserAboutError("Couldn't connect to the webservice")
//                        }
//                        else -> {
//                            Timber.e(throwable, "Unknown error")
//                            displayHelper.notifyUserAboutError("Couldn't connect to the webservice")
//                            navigator.goToConnection()
//                        }
//                    }
//                })

    }


    fun play(position: Int) {
//        audioQueueManager.listPosition = position
//        player.play()
    }

    /**
     * Inner class
     */
    inner class PlayerConnection : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            player = DummyPlayerController()
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            player = (service as PlayerService.LocalBinder).service
        }
    }
}