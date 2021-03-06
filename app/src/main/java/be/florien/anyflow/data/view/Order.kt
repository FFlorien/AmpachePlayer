package be.florien.anyflow.data.view

class Order(val priority: Int, val subject: Long, val ordering: Int, val argument: Int = -1) {
    constructor(priority: Int, subject: Long) : this(priority, subject, RANDOM, Math.random().times(RANDOM_MULTIPLIER).toInt())
    constructor(precisePosition: Int, song: Song) : this(PRIORITY_LAST, song.id, PRECISE_POSITION, precisePosition)

    val orderingType
        get() = when (ordering) {
            ASCENDING -> Ordering.ASCENDING
            DESCENDING -> Ordering.DESCENDING
            PRECISE_POSITION -> Ordering.PRECISE_POSITION
            RANDOM -> Ordering.RANDOM
            else -> Ordering.RANDOM
        }

    val orderingSubject
        get() = when (subject) {
            SUBJECT_ALL -> Subject.ALL
            SUBJECT_ARTIST -> Subject.ARTIST
            SUBJECT_ALBUM_ARTIST -> Subject.ALBUM_ARTIST
            SUBJECT_ALBUM -> Subject.ALBUM
            SUBJECT_YEAR -> Subject.YEAR
            SUBJECT_GENRE -> Subject.GENRE
            SUBJECT_TRACK -> Subject.TRACK
            SUBJECT_TITLE -> Subject.TITLE
            else -> Subject.TRACK
        }

    companion object {

        const val PRIORITY_LAST = 20
        const val ASCENDING = 1
        const val DESCENDING = -1
        const val PRECISE_POSITION = -2
        const val RANDOM = -3
        const val RANDOM_MULTIPLIER = 1000
        const val SUBJECT_ALL = -1L
        const val SUBJECT_ARTIST = -2L
        const val SUBJECT_ALBUM_ARTIST = -3L
        const val SUBJECT_ALBUM = -4L
        const val SUBJECT_YEAR = -5L
        const val SUBJECT_GENRE = -6L
        const val SUBJECT_TRACK = -7L
        const val SUBJECT_TITLE = -8L
    }

    enum class Subject {
        ALL,
        ARTIST,
        ALBUM_ARTIST,
        ALBUM,
        YEAR,
        GENRE,
        TRACK,
        TITLE
    }

    enum class Ordering {
        ASCENDING,
        DESCENDING,
        PRECISE_POSITION,
        RANDOM
    }

}