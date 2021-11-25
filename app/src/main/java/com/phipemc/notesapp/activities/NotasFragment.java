package com.phipemc.notesapp.activities;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.phipemc.notesapp.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.phipemc.notesapp.R;
import com.phipemc.notesapp.adapters.AdaptadorNotes;
import com.phipemc.notesapp.database.databaseNotes;
import com.phipemc.notesapp.entities.Note;
import com.phipemc.notesapp.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;


public class NotasFragment extends Fragment implements NotesListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;
    public static final int REQUEST_CODE_SELECT_IMG = 4;
    public static final int REQUEST_CODE_SELECT_PERMISSION = 5;


    private RecyclerView notesRecyclerView;
    private List<Note> listaNotas;
    private AdaptadorNotes adaptadorNotes;

    private int noteClickedPosition = -1;

    private AlertDialog dialogAddURL;
    private AlertDialog dialogMenu;

    public NotasFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar o cargar el layout para el Fragment
        View root = inflater.inflate(R.layout.fragment_notas, container, false);
        // Para hacer findViewById, debes hacerlo con la referencia de root que es tu layout
        // Ejemplo: TextView textView = root.findViewById(R.id.textView);

        ImageView addNote = root.findViewById(R.id.buttonAddNote);
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(getActivity().getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                );
            }
        });
        notesRecyclerView = root.findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );
        listaNotas = new ArrayList<>();
        adaptadorNotes = new AdaptadorNotes(listaNotas, this);
        notesRecyclerView.setAdapter(adaptadorNotes);

        mostarNotas(REQUEST_CODE_SHOW_NOTES, false);

        EditText inputSearch = root.findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adaptadorNotes.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (listaNotas.size() != 0) {
                    adaptadorNotes.searchNotes(editable.toString());
                }
            }
        });


        return root;
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getActivity().getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    private void mostarNotas(final int requestCode, final boolean isDeleted) {

        @SuppressLint("StaticFieldLeak")
        class obtenerNotas extends AsyncTask<Void, Void, List<Note>> {
            @Override
            protected List<Note> doInBackground(Void... voids) {
                return databaseNotes.getDatabase(getActivity().getApplicationContext()).dao().getAll();
            }

            @Override
            protected void onPostExecute(List<Note> notas) {
                super.onPostExecute(notas);
                if (requestCode == REQUEST_CODE_SHOW_NOTES) {
                    listaNotas.addAll(notas);
                    adaptadorNotes.notifyDataSetChanged();
                } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                    listaNotas.add(0, notas.get(0));
                    adaptadorNotes.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    listaNotas.remove(noteClickedPosition);
                    if (isDeleted) {
                        adaptadorNotes.notifyItemRemoved(noteClickedPosition);
                    } else {
                        listaNotas.add(noteClickedPosition, notas.get(noteClickedPosition));
                        adaptadorNotes.notifyItemChanged(noteClickedPosition);
                    }
                }
            }

        }
        new obtenerNotas().execute();
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMG);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_SELECT_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(getContext(), "Permiso Denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getActivity().getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

}