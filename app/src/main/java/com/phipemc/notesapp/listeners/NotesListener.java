package com.phipemc.notesapp.listeners;

import com.phipemc.notesapp.entities.Note;

public interface NotesListener {
    void onNoteClicked(Object papers, int position);
}
