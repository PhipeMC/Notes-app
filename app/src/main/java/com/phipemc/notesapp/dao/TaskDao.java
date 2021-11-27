package com.phipemc.notesapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.phipemc.notesapp.entities.Note;
import com.phipemc.notesapp.entities.Task;

import java.util.List;

@Dao
public interface TaskDao {

    @Query("select * from tareas order by id desc")
    List<Task> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Task tareas);

    @Delete
    void deleteNote(Task tareas);

}
