package be.florien.ampacheplayer.player

import be.florien.ampacheplayer.persistence.local.model.DbFilter

sealed class Filter<T>(
        var clause: String,
        var argument: T) {

    open class StringFilter(clause: String, argument: String) : Filter<String>(clause, argument)

    open class LongFilter(clause: String, argument: Long) : Filter<Long>(clause, argument)

    /**
     * String filters
     */
    class TitleIs(argument: String) : StringFilter(TITLE_IS, argument)

    class TitleContain(argument: String) : StringFilter(TITLE_CONTAIN, argument)

    class GenreIs(argument: String) : StringFilter(GENRE_IS, argument)

    /**
     * Long filters
     */

    class SongIs(argument: Long) : LongFilter(SONG_ID, argument)

    class ArtistIs(argument: Long) : LongFilter(ARTIST_ID, argument)

    class AlbumArtistIs(argument: Long) : LongFilter(ALBUM_ARTIST_ID, argument)

    class AlbumIs(argument: Long) : LongFilter(ALBUM_ID, argument)

    companion object {
        private const val TITLE_IS = "title = "
        private const val TITLE_CONTAIN = "title LIKE "
        private const val GENRE_IS = "song.genre = "
        private const val SONG_ID = "song.id = "
        private const val ARTIST_ID = "song.artistId = "
        private const val ALBUM_ARTIST_ID = "song.albumArtistId = "
        private const val ALBUM_ID = "song.albumId = "

        fun toTypedFilter(fromDb: DbFilter): Filter<*> {
            return when (fromDb.clause) {
                TITLE_IS -> TitleIs(fromDb.argument)
                TITLE_CONTAIN -> TitleContain(fromDb.argument)
                GENRE_IS -> GenreIs(fromDb.argument)
                SONG_ID -> SongIs(fromDb.argument.toLong())
                ARTIST_ID -> ArtistIs(fromDb.argument.toLong())
                ALBUM_ARTIST_ID -> AlbumArtistIs(fromDb.argument.toLong())
                ALBUM_ID -> AlbumIs(fromDb.argument.toLong())
                else -> TitleIs("")
            }
        }

        fun toDbFilter(fromApp: Filter<*>): DbFilter {
            return when (fromApp) {
                is StringFilter -> DbFilter(0, fromApp.clause, fromApp.argument)
                is LongFilter -> DbFilter(0, fromApp.clause, fromApp.argument.toString())
            }
        }
    }
}