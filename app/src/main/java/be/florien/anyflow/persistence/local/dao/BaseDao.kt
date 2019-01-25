package be.florien.anyflow.persistence.local.dao

import androidx.room.*

@Dao
interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(items: List<T>)

    @Update
    fun update(vararg items: T)

    @Delete
    fun delete(vararg items: T)
}