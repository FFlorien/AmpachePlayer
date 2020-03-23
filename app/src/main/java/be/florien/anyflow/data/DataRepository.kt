package be.florien.anyflow.data

import android.content.SharedPreferences
import be.florien.anyflow.injection.UserScope
import be.florien.anyflow.extension.applyPutLong
import be.florien.anyflow.extension.getDate
import be.florien.anyflow.data.local.LibraryDatabase
import be.florien.anyflow.data.local.model.Album
import be.florien.anyflow.data.local.model.Artist
import be.florien.anyflow.data.local.model.Song
import be.florien.anyflow.data.server.AmpacheConnection
import be.florien.anyflow.data.server.model.AmpacheAlbumList
import be.florien.anyflow.data.server.model.AmpacheArtistList
import be.florien.anyflow.data.server.model.AmpacheError
import be.florien.anyflow.data.server.model.AmpacheSongList
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.*
import javax.inject.Inject

private const val LAST_SONG_UPDATE = "LAST_SONG_UPDATE"
// updated because the art was added , see LibraryDatabase migration 1->2
private const val LAST_ARTIST_UPDATE = "LAST_ARTIST_UPDATE_v1"
private const val LAST_ALBUM_UPDATE = "LAST_ALBUM_UPDATE"

/**
 * Update the local data with the one from the server
 */
@UserScope
class DataRepository
@Inject constructor(
        private val libraryDatabase: LibraryDatabase,
        private val songServerConnection: AmpacheConnection,
        private val sharedPreferences: SharedPreferences) {

    private fun lastAcceptableUpdate() = Calendar.getInstance().apply {
        roll(Calendar.HOUR, false)
    }

    /**
     * Getter with server updates
     */

    fun updateAll(): Completable = updateArtists()
            .concatWith(updateAlbums())
            .concatWith(updateSongs())

    private fun updateSongs(): Completable = getUpToDateList(
            LAST_SONG_UPDATE,
            AmpacheConnection::getSongs,
            AmpacheSongList::error
    ) {
        libraryDatabase.addSongs(it.songs.map(::Song))
    }

    private fun updateArtists(): Completable = getUpToDateList(
            LAST_ARTIST_UPDATE,
            AmpacheConnection::getArtists,
            AmpacheArtistList::error
    ) {
        libraryDatabase.addArtists(it.artists.map(::Artist))
    }

    private fun updateAlbums(): Completable = getUpToDateList(
            LAST_ALBUM_UPDATE,
            AmpacheConnection::getAlbums,
            AmpacheAlbumList::error
    ) {
        libraryDatabase.addAlbums(it.albums.map(::Album))
    }

    /**
     * Private Method
     */

    private fun <SERVER_TYPE> getUpToDateList(
            updatePreferenceName: String,
            getListOnServer: AmpacheConnection.(Calendar) -> Observable<SERVER_TYPE>,
            getError: SERVER_TYPE.() -> AmpacheError,
            saveToDatabase: (SERVER_TYPE) -> Completable)
            : Completable {
        val nowDate = Calendar.getInstance()
        val lastUpdate = sharedPreferences.getDate(updatePreferenceName, 0)
        val lastAcceptableUpdate = lastAcceptableUpdate()

        return if (lastUpdate.before(lastAcceptableUpdate)) {
            songServerConnection
                    .getListOnServer(lastUpdate)
                    .flatMapCompletable { result ->
                        saveToDatabase(result).doFinally {
                            when (result.getError().code) {
                                401 -> songServerConnection.reconnect(songServerConnection.getListOnServer(lastUpdate))
                                else -> Observable.just(result)
                            }
                        }
                    }.doOnComplete {
                        sharedPreferences.applyPutLong(updatePreferenceName, nowDate.timeInMillis)
                    }
        } else {
            Completable.complete()
        }
    }
}