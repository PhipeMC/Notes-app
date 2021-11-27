package com.phipemc.notesapp.listeners;

import com.phipemc.notesapp.entities.Note;
import com.phipemc.notesapp.entities.Task;

public interface TaskListener {
    void onTaskClicked(Task tareas, int position);
}
