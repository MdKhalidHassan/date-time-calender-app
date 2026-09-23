package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DateNote
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM date_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<DateNote>>

    @Query("SELECT * FROM date_notes WHERE year = :year AND month = :month AND day = :day ORDER BY timestamp DESC")
    fun getNotesForDay(year: Int, month: Int, day: Int): Flow<List<DateNote>>

    @Query("SELECT * FROM date_notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchNotes(query: String): Flow<List<DateNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: DateNote): Long

    @Update
    suspend fun updateNote(note: DateNote)

    @Query("DELETE FROM date_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}
