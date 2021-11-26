package com.phipemc.notesapp.listeners;

import com.phipemc.notesapp.entities.Note;

public interface TaskListener {
    void onTaskClicked(Note.Tareas tareas, int position);
}
