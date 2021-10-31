package com.phipemc.notesapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContentInfo;
import android.view.View;
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
    private String selectedColor;
    private View viewSubIndica;
    private ImageView imageNote;
    private String selectedImagePath;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        getSupportActionBar().hide();

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
        //Darle formato a una fecha
        textFecha.setText(
                new SimpleDateFormat("EEEE, dd  MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date())
        );


        ImageView guardar =  findViewById(R.id.img_done_button);
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardar();
            }
        });

        selectedColor = "#333333";
        selectedImagePath = "";
        initExtra();
        setViewSubIndicaColor();
    }

    private void guardar(){
        if(tituloNota.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "El titulo no puede ser un campo vacio", Toast.LENGTH_SHORT).show();
            return;
        }else if(cuerpoNota.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "La nota debe tener algo escrito", Toast.LENGTH_SHORT).show();
            return;
        }

        final Note miNota = new Note();
        miNota.setTitle(tituloNota.getText().toString());
        miNota.setSubtitle(subtituloNota.getText().toString());
        miNota.setNote_text(cuerpoNota.getText().toString());
        miNota.setDate(textFecha.getText().toString());
        miNota.setColor(selectedColor);
        miNota.setImgpath(selectedImagePath);

        @SuppressLint("StaticFieldLeak")
        class guardarNota extends AsyncTask<Void, Void, Void>{
            @Override
            protected Void doInBackground(Void...voids){
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

        final ImageView imgColor1 = layoutExtra.findViewById(R.id.imgColor1);
        final ImageView imgColor2 = layoutExtra.findViewById(R.id.imgColor2);
        final ImageView imgColor3 = layoutExtra.findViewById(R.id.imgColor3);
        final ImageView imgColor4 = layoutExtra.findViewById(R.id.imgColor4);
        final ImageView imgColor5 = layoutExtra.findViewById(R.id.imgColor5);

        layoutExtra.findViewById(R.id.viewColor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedColor="#333333";
                imgColor1.setImageResource(R.drawable.ic_done);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(0);
                setViewSubIndicaColor();
            }
        });

        layoutExtra.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedColor="#FDBE3B";
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(R.drawable.ic_done);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(0);
                setViewSubIndicaColor();
            }
        });

        layoutExtra.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedColor="#FF4842";
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(R.drawable.ic_done);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(0);
                setViewSubIndicaColor();
            }
        });

        layoutExtra.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedColor="#3A52fC";
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(R.drawable.ic_done);
                imgColor5.setImageResource(0);
                setViewSubIndicaColor();
            }
        });

        layoutExtra.findViewById(R.id.viewColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedColor="#000000";
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(R.drawable.ic_done);
                setViewSubIndicaColor();
            }
        });

        layoutExtra.findViewById(R.id.layoutAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if(ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                )!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            CreateNoteActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                }else{
                    selectImage();
                }
            }
        });
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
                Toast.makeText(this, "Permiso Denegado", Toast.LENGTH_SHORT).show();
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
}