package com.phipemc.notesapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.phipemc.notesapp.dao.NoteDao;
import com.phipemc.notesapp.entities.Note;

@Database(entities = Note.class, version = 1, exportSchema = false)
public abstract class databaseNotes extends RoomDatabase {

    private static databaseNotes database;

    public static synchronized databaseNotes getDatabase(Context context){
        if (database == null){
            database = Room.databaseBuilder(context, databaseNotes.class, "notes_db").build();
        }
        return database;
    }

    public abstract NoteDao dao();
}
