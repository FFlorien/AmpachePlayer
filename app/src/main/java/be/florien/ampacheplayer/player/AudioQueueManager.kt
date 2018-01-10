package be.florien.ampacheplayer.player

import be.florien.ampacheplayer.persistence.model.Song
import be.florien.ampacheplayer.persistence.DatabaseManager
import io.reactivex.subjects.PublishSubject
import io.realm.Realm
import io.realm.RealmResults
import javax.inject.Inject
import javax.inject.Singleton

const val NO_CURRENT_SONG = -13456

/**
 * Manager for the queue of songs that are playing. It handle filters, random, repeat and addition to the queue
 */
@Singleton
class AudioQueueManager
@Inject constructor(private val databaseManager: DatabaseManager) {

    /**
     * Fields
     */
    private var filters: List<Filter<*>> = mutableListOf()
    val positionObservable: PublishSubject<Int> = PublishSubject.create()
    val itemsCount: Int
        get() = databaseManager.getSongs(filters).size
    var listPosition: Int = 0
        set(value) {
            field = if (value in 0 until databaseManager.getSongs(filters).size) {
                value
            } else {
                NO_CURRENT_SONG
            }
            positionObservable.onNext(field)
        }


    /**
     * Methods
     */
    fun getCurrentSong(): Song = databaseManager.getSongs(filters)[listPosition]

    fun getCurrentAudioQueue(): RealmResults<Song> = databaseManager.getSongs(filters)
}