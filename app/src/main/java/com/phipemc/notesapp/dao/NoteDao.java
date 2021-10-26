package com.phipemc.notesapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.phipemc.notesapp.entities.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("select * from notes order by id desc")
    List<Note> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);

    @Delete
    void deleteNote(Note note);
}
