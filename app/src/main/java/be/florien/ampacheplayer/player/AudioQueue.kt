package be.florien.ampacheplayer.player

import be.florien.ampacheplayer.di.UserScope
import be.florien.ampacheplayer.persistence.SongsDatabase
import be.florien.ampacheplayer.persistence.model.Song
import io.reactivex.subjects.PublishSubject
import io.realm.RealmResults
import javax.inject.Inject

const val NO_CURRENT_SONG = -13456

/**
 * Manager for the queue of songs that are playing. It handle filters, random, repeat and addition to the queue
 */
@UserScope
class AudioQueueManager
@Inject constructor(private val songsDatabase: SongsDatabase) {

    /**
     * Fields
     */
    private var filters: MutableList<Filter<*>> = mutableListOf()
    val positionObservable: PublishSubject<Int> = PublishSubject.create()
    val itemsCount: Int
        get() = songsDatabase.getSongs(filters).size
    var listPosition: Int = 0
        set(value) {
            field = if (value in 0 until songsDatabase.getSongs(filters).size) {
                value
            } else {
                NO_CURRENT_SONG
            }
            positionObservable.onNext(field)
        }


    /**
     * Methods
     */
    fun getCurrentSong(): Song {
        val songs = songsDatabase.getSongs(filters)
        return if (songs.size <= listPosition) Song() else songs[listPosition] ?: Song()
    }

    fun getCurrentAudioQueue(): RealmResults<Song> = songsDatabase.getSongs(filters)

    fun addFilter(filter: Filter<*>) = filters.add(filter)

}