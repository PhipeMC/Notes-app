package com.phipemc.notesapp.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.phipemc.notesapp.R;
import com.phipemc.notesapp.adapters.AdaptadorNotes;
import com.phipemc.notesapp.database.databaseNotes;
import com.phipemc.notesapp.entities.Note;
import com.phipemc.notesapp.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;

    private RecyclerView notesRecyclerView;
    private List<Note> listaNotas;
    private AdaptadorNotes adaptadorNotes;

    private int noteClickedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView addNote = findViewById(R.id.buttonAddNote);
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                );
            }
        });
        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );
        listaNotas =  new ArrayList<>();
        adaptadorNotes = new AdaptadorNotes(listaNotas, this);
        notesRecyclerView.setAdapter(adaptadorNotes);

        mostarNotas(REQUEST_CODE_SHOW_NOTES, false);
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    private void mostarNotas(final int requestCode, final boolean isDeleted){

        @SuppressLint("StaticFieldLeak")
        class obtenerNotas extends AsyncTask<Void, Void, List<Note>>{
            @Override
            protected List<Note> doInBackground(Void...voids){
                return databaseNotes.getDatabase(getApplicationContext()).dao().getAll();
            }

            @Override
            protected void onPostExecute(List<Note> notas){
                super.onPostExecute(notas);
                if(requestCode == REQUEST_CODE_SHOW_NOTES){
                    listaNotas.addAll(notas);
                    adaptadorNotes.notifyDataSetChanged();
                }else if(requestCode == REQUEST_CODE_ADD_NOTE){
                    listaNotas.add(0,notas.get(0));
                    adaptadorNotes.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                }else if(requestCode == REQUEST_CODE_UPDATE_NOTE){
                    listaNotas.remove(noteClickedPosition);
                    if(isDeleted){
                        adaptadorNotes.notifyItemRemoved(noteClickedPosition);
                    }else{
                        listaNotas.add(noteClickedPosition, notas.get(noteClickedPosition));
                        adaptadorNotes.notifyItemChanged(noteClickedPosition);
                    }
                }
            }

        }
        new obtenerNotas().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
            mostarNotas(REQUEST_CODE_ADD_NOTE, false);
        }else if(requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK){
            if(data != null){
                mostarNotas(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }
        }
    }
}