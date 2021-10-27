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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_ADD_NOTE = 1;

    private RecyclerView notesRecyclerView;
    private List<Note> listaNotas;
    private AdaptadorNotes adaptadorNotes;

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
        adaptadorNotes = new AdaptadorNotes(listaNotas);
        notesRecyclerView.setAdapter(adaptadorNotes);

        mostarNotas();
    }

    private void mostarNotas(){

        @SuppressLint("StaticFieldLeak")
        class obtenerNotas extends AsyncTask<Void, Void, List<Note>>{
            @Override
            protected List<Note> doInBackground(Void...voids){
                return databaseNotes.getDatabase(getApplicationContext()).dao().getAll();
            }

            @Override
            protected void onPostExecute(List<Note> notas){
                super.onPostExecute(notas);
                if(listaNotas.size() == 0){
                    listaNotas.addAll(notas);
                    adaptadorNotes.notifyDataSetChanged();
                }
                else{
                    listaNotas.add(0, notas.get(0));
                    adaptadorNotes.notifyItemInserted(0);

                }
                notesRecyclerView.smoothScrollToPosition(0);
            }

        }
        new obtenerNotas().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
            mostarNotas();
        }
    }
}