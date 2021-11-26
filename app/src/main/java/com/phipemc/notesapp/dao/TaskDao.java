package com.phipemc.notesapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.phipemc.notesapp.entities.Note;

import java.util.List;

@Dao
public interface TaskDao {

    @Query("select * from tareas order by id desc")
    List<Note.Tareas> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note.Tareas tareas);

    @Delete
    void deleteNote(Note.Tareas tareas);

}
