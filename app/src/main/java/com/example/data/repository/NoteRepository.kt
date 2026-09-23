package com.example.data.repository

import com.example.data.dao.NoteDao
import com.example.data.model.DateNote
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

    val allNotes: Flow<List<DateNote>> = noteDao.getAllNotes()

    fun getNotesForDay(year: Int, month: Int, day: Int): Flow<List<DateNote>> =
        noteDao.getNotesForDay(year, month, day)

    fun searchNotes(query: String): Flow<List<DateNote>> =
        noteDao.searchNotes(query)

    suspend fun insertNote(note: DateNote): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: DateNote) = noteDao.updateNote(note)

    suspend fun deleteNote(id: Long) = noteDao.deleteNoteById(id)
}
