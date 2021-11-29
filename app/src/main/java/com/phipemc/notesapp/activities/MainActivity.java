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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.phipemc.notesapp.R;
import com.phipemc.notesapp.adapters.AdaptadorNotes;
import com.phipemc.notesapp.adapters.AdaptadorTasks;
import com.phipemc.notesapp.database.databaseNotes;
import com.phipemc.notesapp.database.databaseTasks;
import com.phipemc.notesapp.entities.Note;
import com.phipemc.notesapp.entities.Task;
import com.phipemc.notesapp.listeners.NotesListener;
import com.phipemc.notesapp.listeners.TaskListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;
    public static final int REQUEST_CODE_SELECT_IMG = 4;
    public static final int REQUEST_CODE_SELECT_PERMISSION = 5;
    public static final int REQUEST_CODE_ADD_TASK = 11;
    public static final int REQUEST_CODE_UPDATE_TASK = 22;
    public static final int REQUEST_CODE_SHOW_TASK = 33;


    private RecyclerView notesRecyclerView;
    private List<Object> listaPapers;
    private List<Note> listaNotas;
    private List<Task> listaTareas;

    private AdaptadorNotes adaptadorNotes;

    private int paperClickedPosition = -1;

    private AlertDialog dialogMenu;

    float x1, x2, y1, y2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //asignamos el clic al FAB
        ImageView addNote = findViewById(R.id.buttonAddNote);
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        //modificamos el grid del recycler view
        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        );

        //Inicializamos el adaptador
        listaPapers = new ArrayList<>();
        adaptadorNotes = new AdaptadorNotes(listaPapers, this);
        notesRecyclerView.setAdapter(adaptadorNotes);

        //Llamamos el metodo encargado de mostrar las notas
        mostarPapers(REQUEST_CODE_SHOW_NOTES, false);

        //obtenemos el campo de la busqueda y añadimos las funcionalidades deacuerdo al listener que nos ofrece y adaptador
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
                if (listaPapers.size() != 0) {
                    adaptadorNotes.searchNotes(editable.toString());
                }
            }
        });

    }

    //Sobrescribir con ambos
    @Override
    public void onNoteClicked(Object papers, int position) {
        paperClickedPosition = position;
        if (papers instanceof Note) {
            Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
            intent.putExtra("isViewOrUpdate", true);
            intent.putExtra("note", (Note) papers);
            startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
        } else {
            Intent intent = new Intent(getApplicationContext(), CreateTaskActivity.class);
            intent.putExtra("isViewOrUpdate", true);
            intent.putExtra("task", (Task) papers);
            startActivityForResult(intent, REQUEST_CODE_UPDATE_TASK);
        }

    }


    /*
     * Mandas llamar el metodo y se ejecuta la clase para hacer de subproceso las acciones que queramos deacuerdo a nuestras
     * variables
     *
     * */
    private void mostarPapers(final int requestCode, final boolean isDeleted) {

        @SuppressLint("StaticFieldLeak")
        class obtenerNotas extends AsyncTask<Void, Void, List<List<Object>>> {
            @Override
            protected  List<List<Object>> doInBackground(Void... voids) {
                List<Object> noteList = new ArrayList<>();
                List<Object> taskList = new ArrayList<>();

                List<Note> n = databaseNotes.getDatabase(getApplicationContext()).dao().getAll();
                List<Task> t = databaseTasks.getDatabase(getApplicationContext()).dao().getAll();

                for (Task task : t) {
                    taskList.add(task);
                }

                for (Note note : n) {
                    noteList.add(note);
                }

                List<List<Object>> paper = new ArrayList<>();
                paper.add(0,noteList);
                paper.add(1,taskList);

                return paper;
            }

            @Override
            protected void onPostExecute(List<List<Object>> paper) {
                super.onPostExecute(paper);
                if (requestCode == REQUEST_CODE_SHOW_NOTES) {
                    listaPapers.addAll(paper.get(0));
                    listaPapers.addAll(paper.get(1));
                    adaptadorNotes.notifyDataSetChanged();
                } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                    listaPapers.add(0, paper.get(0).get(0));
                    adaptadorNotes.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                } else if (requestCode == REQUEST_CODE_ADD_TASK) {
                    listaPapers.add(paper.get(0).size(), paper.get(1).get(0));
                    adaptadorNotes.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    listaPapers.remove(paperClickedPosition);
                    if (isDeleted) {
                        adaptadorNotes.notifyItemRemoved(paperClickedPosition);
                    } else {
                        listaPapers.add(paperClickedPosition, paper.get(0).get(paperClickedPosition));
                        adaptadorNotes.notifyItemChanged(paperClickedPosition);
                    }
                } else if (requestCode == REQUEST_CODE_UPDATE_TASK) {
                    listaPapers.remove(paperClickedPosition);
                    if (isDeleted) {
                        adaptadorNotes.notifyItemRemoved(paperClickedPosition);
                    } else {
                        listaPapers.add(paperClickedPosition, paper.get(1).get(paperClickedPosition-paper.get(0).size()));
                        adaptadorNotes.notifyItemChanged(paperClickedPosition);
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

    //investigado sobre como mostrar imagenes
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

    ///checar tiene solo notas
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            mostarPapers(REQUEST_CODE_ADD_NOTE, false);
        } else if (requestCode == REQUEST_CODE_ADD_TASK && resultCode == RESULT_OK) {
            mostarPapers(REQUEST_CODE_ADD_TASK, false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                mostarPapers(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }
        } else if (requestCode == REQUEST_CODE_UPDATE_TASK && resultCode == RESULT_OK) {
            if (data != null) {
                mostarPapers(REQUEST_CODE_UPDATE_TASK, data.getBooleanExtra("isNoteDeleted", false));
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


    private void showDialog() {
        if (dialogMenu == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View v = LayoutInflater.from(this).inflate(
                    R.layout.layout_menu, (ViewGroup) findViewById(R.id.layoutMenuContainer)
            );
            builder.setView(v);
            dialogMenu = builder.create();
            if (dialogMenu.getWindow() != null) {
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
                            REQUEST_CODE_ADD_TASK
                    );
                }
            });
        }

        dialogMenu.show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();
                if (x1 > x2) {
                    Intent i = new Intent(MainActivity.this, CreateTaskActivity.class);
                    startActivity(i);
                }
                break;
        }
        return false;
    }


}
