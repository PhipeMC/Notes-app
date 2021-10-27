package com.phipemc.notesapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText tituloNota, subtituloNota, cuerpoNota;
    private TextView textFecha;
    private String selectedColor;
    private View viewSubIndica;

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

        Note miNota = new Note();
        miNota.setTitle(tituloNota.getText().toString());
        miNota.setSubtitle(subtituloNota.getText().toString());
        miNota.setNote_text(cuerpoNota.getText().toString());
        miNota.setDate(textFecha.getText().toString());

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
    }

    private void setViewSubIndicaColor(){
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubIndica.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedColor));
    }
}