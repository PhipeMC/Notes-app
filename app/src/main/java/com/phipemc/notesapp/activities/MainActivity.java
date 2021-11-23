package com.phipemc.notesapp.activities;

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

public class MainActivity extends AppCompatActivity implements NotesListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView addNote = findViewById(R.id.buttonAddNote);
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );
        listaNotas = new ArrayList<>();
        adaptadorNotes = new AdaptadorNotes(listaNotas, this);
        notesRecyclerView.setAdapter(adaptadorNotes);

        mostarNotas(REQUEST_CODE_SHOW_NOTES, false);

        EditText inputSearch = findViewById(R.id.inputSearch);
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

    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    private void mostarNotas(final int requestCode, final boolean isDeleted) {

        @SuppressLint("StaticFieldLeak")
        class obtenerNotas extends AsyncTask<Void, Void, List<Note>> {
            @Override
            protected List<Note> doInBackground(Void... voids) {
                return databaseNotes.getDatabase(getApplicationContext()).dao().getAll();
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
        if (intent.resolveActivity(getPackageManager()) != null) {
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
                Toast.makeText(this, "Permiso Denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            mostarNotas(REQUEST_CODE_ADD_NOTE, false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                mostarNotas(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }
        } else if (requestCode == REQUEST_CODE_SELECT_IMG && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectImgUri = data.getData();
                if (selectImgUri != null) {
                    try {
                        String selectedImgPath = getPathFromUri(selectImgUri);
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "image");
                        intent.putExtra("imagePath", selectedImgPath);
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void showDialog(){
        if(dialogMenu == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View v = LayoutInflater.from(this).inflate(
                    R.layout.layout_menu, (ViewGroup) findViewById(R.id.layoutMenuContainer)
            );
            builder.setView(v);
            dialogMenu = builder.create();
            if(dialogMenu.getWindow() != null){
                dialogMenu.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            v.findViewById(R.id.note_menu).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogMenu.dismiss();
                    startActivityForResult(
                            new Intent(getApplicationContext(), CreateNoteActivity.class),
                            REQUEST_CODE_ADD_NOTE
                    );
                }
            });

            v.findViewById(R.id.task_menu).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogMenu.dismiss();
                    startActivityForResult(
                            new Intent(getApplicationContext(), CreateTaskActivity.class),
                            REQUEST_CODE_ADD_NOTE
                    );
                }
            });
        }

        dialogMenu.show();
    }
}