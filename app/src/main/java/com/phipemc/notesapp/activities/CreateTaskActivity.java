package com.phipemc.notesapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.phipemc.notesapp.R;
import com.phipemc.notesapp.database.databaseNotes;
import com.phipemc.notesapp.database.databaseTasks;
import com.phipemc.notesapp.entities.Note;
import com.phipemc.notesapp.entities.Task;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateTaskActivity extends AppCompatActivity {
    private EditText taskName;
    private TextView datetime;
    private Button alarmBtn;
    private ImageView imageNote;
    private AlertDialog dialogDeleteNote;
    private View viewSubIndica;

    private String selectedColor;
    private Task taskHecha;
    private String selectedImagePath;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;


    //private ActivityMainBinding

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_create);

        ///Btono retornar
        ImageView imgBack = findViewById(R.id.image_back_task);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //Boton guardar tarea
        ImageView imgSave = findViewById(R.id.img_done_button_task);
        imgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardar();
            }
        });

        ///Tomar elementos de mi lay
        taskName = findViewById(R.id.inputTaskName);
        datetime = findViewById(R.id.textDateTimeTask);
        imageNote = findViewById(R.id.imageNote);

        datetime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date())
        );

        if(getIntent().getBooleanExtra("isViewOrUpdate", false)){
            taskHecha = (Task) getIntent().getSerializableExtra("task");
            setViewOrUpdate();
        }

        findViewById(R.id.imageRemove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                findViewById(R.id.imageRemove).setVisibility(View.GONE);
                selectedImagePath = "";
            }
        });

        //extra
        if(getIntent().getBooleanExtra("isFromQuickActions", false)){
            String type = getIntent().getStringExtra("quickActionsType");
            if(type != null){
                if(type.equals("image")){
                    selectedImagePath = getIntent().getStringExtra("imagePath");
                    imageNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                    imageNote.setVisibility(View.VISIBLE);
                }else if(type.equals("URL")){
                    //textWebURL.setText(getIntent().getStringExtra("URL"));
                    //layoutWebURL.setVisibility(View.VISIBLE);
                }
            }
        }
        selectedColor = "#375481";
        selectedImagePath = "";

        initExtra();

    }

    //Al ingresar a la pantalla que se rellene con lo que ya se tenia
    private void setViewOrUpdate() {
        taskName.setText(taskHecha.getTitle());
        datetime.setText(taskHecha.getDate());

        if(taskHecha.getImgpath() != null && !taskHecha.getImgpath().trim().isEmpty()){
            imageNote.setImageBitmap(BitmapFactory.decodeFile(taskHecha.getImgpath()));
            imageNote.setVisibility(View.VISIBLE);
            findViewById(R.id.imageRemove).setVisibility(View.VISIBLE);
            selectedImagePath = taskHecha.getImgpath();
        }
    }

    private void guardar(){
        if(taskName.getText().toString().trim().isEmpty()){
            Toast.makeText(this, R.string.note_title_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        final Task miTarea = new Task();
        miTarea.setTitle(taskName.getText().toString());
        miTarea.setDate(datetime.getText().toString());
        miTarea.setColor(selectedColor);
        miTarea.setImgpath(selectedImagePath);

        //if(layoutWebURL.getVisibility() == View.VISIBLE){
          //  miNota.setWeb_link(textWebURL.getText().toString());
        //}

        if(taskHecha != null){
            miTarea.setId(taskHecha.getId());
        }

        @SuppressLint("StaticFieldLeak")
        class guardarTarea extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void...voids){
                databaseTasks.getDatabase(getApplicationContext()).dao().insertNote(miTarea);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                Intent intent =  new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new  guardarTarea().execute();
    }

    private void initExtra(){
        final LinearLayout layoutExtra = findViewById(R.id.layoutExtra);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutExtra);
        layoutExtra.findViewById(R.id.textExtra).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }else{
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        //imagen
        layoutExtra.findViewById(R.id.layoutAddImage).setOnClickListener((v) -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if(ContextCompat.checkSelfPermission(
                    getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            )!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                        CreateTaskActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION
                );
            }else {
                selectImage();
            }
        });

        //dialogos
        layoutExtra.findViewById(R.id.layoutAddUrl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddURLDialog();
            }
        });

        if(taskHecha != null){
            layoutExtra.findViewById(R.id.layoutDelete).setVisibility(View.VISIBLE);
            layoutExtra.findViewById(R.id.layoutDelete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteDialog();
                }
            });
        }
    }

    private void showDeleteDialog(){
        if(dialogDeleteNote == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateTaskActivity.this);
            View v = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note, (ViewGroup) findViewById(R.id.layoutDeleteNoteContainer)
            );
            builder.setView(v);
            dialogDeleteNote = builder.create();
            if(dialogDeleteNote.getWindow() != null){
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            v.findViewById(R.id.textDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    class DeleteTask extends AsyncTask<Void, Void, Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {
                            databaseTasks.getDatabase(getApplicationContext()).dao().deleteNote(taskHecha);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDeleted", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }

                    new DeleteTask().execute();
                }
            });

            v.findViewById(R.id.textCancelNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogDeleteNote.dismiss();
                }
            });
        }

        dialogDeleteNote.show();
    }

    private void selectImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else{
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if(data != null){
                Uri selectedImageUri = data.getData();
                if(selectedImageUri != null){
                    try{
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.imageRemove).setVisibility(View.VISIBLE);

                        selectedImagePath = getPathFromUri(selectedImageUri);
                    }catch (Exception exception){
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private String getPathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if(cursor == null){
            filePath = contentUri.getPath();
        }else{
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    private void showAddURLDialog(){

    }
}
