package com.phipemc.notesapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.phipemc.notesapp.dao.TaskDao;
import com.phipemc.notesapp.entities.Task;

@Database(entities = Task.class, version = 1, exportSchema = false)
public abstract class databaseTasks extends RoomDatabase {

    private static databaseTasks databasetask;

    public static synchronized databaseTasks getDatabase(Context context){
        if (databasetask == null){
            databasetask = Room.databaseBuilder(context, databaseTasks.class, "task_db").build();
        }
        return databasetask;
    }

    public abstract TaskDao dao();
}
