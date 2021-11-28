package com.phipemc.notesapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.text.Layout;
import android.util.Patterns;
import android.view.ContentInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.phipemc.notesapp.R;
import com.phipemc.notesapp.database.databaseNotes;
import com.phipemc.notesapp.entities.Note;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText tituloNota, subtituloNota, cuerpoNota;
    private TextView textFecha;
    private View viewSubIndica;
    private ImageView imageNote;
    private TextView textWebURL;
    private LinearLayout layoutWebURL;

    private String selectedColor;
    private String selectedImagePath;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;

    private AlertDialog dialogDeleteNote;

    private Note alReadyAvailableNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        //getSupportActionBar().hide();

        //boton de regreso al main
        ImageView imageBack = findViewById(R.id.image_back);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        tituloNota =  findViewById(R.id.inputNoteTitle);
        subtituloNota = findViewById(R.id.inputNoteSubtitle);
        cuerpoNota = findViewById(R.id.inputNote);
        textFecha = findViewById(R.id.textDateTime);
        viewSubIndica =  findViewById(R.id.viewSubtitleIndicator);
        imageNote = findViewById(R.id.imageNote);
        textWebURL = findViewById(R.id.textWebURL);
        layoutWebURL = findViewById(R.id.layoutWebURL);
        //Darle formato a una fecha
        textFecha.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date())
        );

        //boton guardar
        ImageView guardar =  findViewById(R.id.img_done_button);
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardar();
            }
        });

        selectedColor = "#efa639";
        selectedImagePath = "";

        if(getIntent().getBooleanExtra("isViewOrUpdate", false)){
            alReadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdate();
        }

        findViewById(R.id.imageRemoveURL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textWebURL.setText(null);
                layoutWebURL.setVisibility(View.GONE);
            }
        });

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
                    textWebURL.setText(getIntent().getStringExtra("URL"));
                    layoutWebURL.setVisibility(View.VISIBLE);
                }
            }
        }

        initExtra();
        setViewSubIndicaColor();
    }

    //vista para poder ver o editar
    private void setViewOrUpdate(){
        tituloNota.setText(alReadyAvailableNote.getTitle());
        subtituloNota.setText(alReadyAvailableNote.getSubtitle());
        cuerpoNota.setText(alReadyAvailableNote.getNote_text());
        textFecha.setText(alReadyAvailableNote.getDate());

        if(alReadyAvailableNote.getImgpath() != null && !alReadyAvailableNote.getImgpath().trim().isEmpty()){
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alReadyAvailableNote.getImgpath()));
            imageNote.setVisibility(View.VISIBLE);
            findViewById(R.id.imageRemove).setVisibility(View.VISIBLE);
            selectedImagePath = alReadyAvailableNote.getImgpath();
        }

        if(alReadyAvailableNote.getWeb_link() != null && !alReadyAvailableNote.getWeb_link().trim().isEmpty()){
            textWebURL.setText(alReadyAvailableNote.getWeb_link());
            layoutWebURL.setVisibility(View.VISIBLE);
        }
    }

    private void guardar(){
        if(tituloNota.getText().toString().trim().isEmpty()){
            Toast.makeText(this, R.string.note_title_empty, Toast.LENGTH_SHORT).show();
            return;
        }else if(cuerpoNota.getText().toString().trim().isEmpty()){
            Toast.makeText(this, R.string.note_body_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        final Note miNota = new Note();
        miNota.setTitle(tituloNota.getText().toString());
        miNota.setSubtitle(subtituloNota.getText().toString());
        miNota.setNote_text(cuerpoNota.getText().toString());
        miNota.setDate(textFecha.getText().toString());
        miNota.setColor(selectedColor);
        miNota.setImgpath(selectedImagePath);

        if(layoutWebURL.getVisibility() == View.VISIBLE){
            miNota.setWeb_link(textWebURL.getText().toString());
        }

        if(alReadyAvailableNote != null){
            miNota.setId(alReadyAvailableNote.getId());
        }

        @SuppressLint("StaticFieldLeak")
        class guardarNota extends AsyncTask<Void, Void, Void>{
            @Override
            protected Void doInBackground(Void...voids){
                //ojo
                databaseNotes.getDatabase(getApplicationContext()).dao().insertNote(miNota);
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
        new  guardarNota().execute();
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

        layoutExtra.findViewById(R.id.layoutAddImage).setOnClickListener((v) -> {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if(ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                )!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            CreateNoteActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                }else {
                    selectImage();
                }
        });

        layoutExtra.findViewById(R.id.layoutAddUrl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddURLDialog();
            }
        });

        if(alReadyAvailableNote != null){
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
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
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
                    class DeleteNoteTask extends AsyncTask<Void, Void, Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {
                            databaseNotes.getDatabase(getApplicationContext()).dao().deleteNote(alReadyAvailableNote);
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

                    new DeleteNoteTask().execute();
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

    private void setViewSubIndicaColor(){
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubIndica.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedColor));
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