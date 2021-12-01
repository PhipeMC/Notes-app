package com.phipemc.notesapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.WorkManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.phipemc.notesapp.R;
import com.phipemc.notesapp.database.databaseNotes;
import com.phipemc.notesapp.database.databaseTasks;
import com.phipemc.notesapp.entities.Note;
import com.phipemc.notesapp.entities.Task;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class CreateTaskActivity extends AppCompatActivity {
    Button selefecha, selehora;
    TextView tvfecha, tvhora;
    Button guardar, eliminar;

    Calendar actual = Calendar.getInstance();
    Calendar calendar = Calendar.getInstance();

    private int minuos, hora, dia, mes, anio;

    private EditText taskName;
    private TextView datetime;
    private Button alarmBtn;
    private ImageView imageNote;
    private VideoView videoNote;
    private AlertDialog dialogDeleteNote;
    private View viewSubIndica;

    private Task taskHecha;
    private String selectedColor;
    private String selectedImagePath;
    private String selectedVideoPath;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private static final int CAMERA_REQUEST_CODE = 1001;
    private static final int REQUEST_VIDEO_CAPTURE = 1;
    private static final int REQUEST_CODE_SELECT_VIDEO = 3;

    //private ActivityMainBinding

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_create);
        selefecha = findViewById(R.id.btn_selefecha);
        selehora = findViewById(R.id.btn_selehora);
        tvfecha = findViewById(R.id.tv_fecha);
        tvhora = findViewById(R.id.tv_hora);

        //Seleccionar la fecha y hora
        selefecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anio = actual.get(Calendar.YEAR);
                mes = actual.get(Calendar.MONTH);
                dia = actual.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                        calendar.set(Calendar.DAY_OF_MONTH, d);
                        calendar.set(Calendar.MONTH, m);
                        calendar.set(Calendar.YEAR, y);

                        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                        String strDate = format.format(calendar.getTime());
                        tvfecha.setText(strDate);
                    }
                }, anio, mes, dia);
                datePickerDialog.show();
            }
        });

        selehora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hora = actual.get(Calendar.HOUR_OF_DAY);
                minuos = actual.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int h, int m) {
                        calendar.set(Calendar.HOUR_OF_DAY, h);
                        calendar.set(Calendar.MINUTE, m);

                        tvhora.setText(String.format("%02d:%02d", h, m));
                    }
                }, hora, minuos, false);
                timePickerDialog.show();
            }
        });

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
        videoNote = findViewById(R.id.videoNote);

        datetime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date())
        );

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
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

        findViewById(R.id.videoRemove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //videoNote.setBackground(R.drawable.ic_launcher_background);
                videoNote.setVisibility(View.GONE);
                findViewById(R.id.videoRemove).setVisibility(View.GONE);
                selectedVideoPath = "";
            }
        });
        selectedColor = "#375481";
        selectedImagePath = "";
        selectedVideoPath = "";

        initExtra();

    }

    //Al ingresar a la pantalla que se rellene con lo que ya se tenia
    private void setViewOrUpdate() {
        taskName.setText(taskHecha.getTitle());
        datetime.setText(taskHecha.getDate());

        if (taskHecha.getImgpath() != null && !taskHecha.getImgpath().trim().isEmpty()) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(taskHecha.getImgpath()));
            imageNote.setVisibility(View.VISIBLE);
            findViewById(R.id.imageRemove).setVisibility(View.VISIBLE);
            selectedImagePath = taskHecha.getImgpath();
        }

        if (taskHecha.getVidpath() != null && !taskHecha.getVidpath().trim().isEmpty()) {
            videoNote.setVideoPath(taskHecha.getVidpath());
            videoNote.setVisibility(View.VISIBLE);
            videoNote.start();
            findViewById(R.id.videoRemove).setVisibility(View.VISIBLE);
            selectedVideoPath = taskHecha.getVidpath();
        }
    }

    private void guardar() {
        if (taskName.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.note_title_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        final Task miTarea = new Task();
        miTarea.setTitle(taskName.getText().toString());
        miTarea.setDate(datetime.getText().toString());
        miTarea.setColor(selectedColor);
        miTarea.setImgpath(selectedImagePath);
        miTarea.setVidpath(selectedVideoPath);

        //if(layoutWebURL.getVisibility() == View.VISIBLE){
        //  miNota.setWeb_link(textWebURL.getText().toString());
        //}

        if (taskHecha != null) {
            miTarea.setId(taskHecha.getId());
        }
        guardarAlarma(miTarea.getTitle());

        @SuppressLint("StaticFieldLeak")
        class guardarTarea extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                databaseTasks.getDatabase(getApplicationContext()).dao().insertNote(miTarea);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new guardarTarea().execute();
    }

    private void guardarAlarma(String tag1) {
        String tag = tag1;
        Long AlertTime = calendar.getTimeInMillis() - System.currentTimeMillis();
        int random = (int) (Math.random() * 50 + 1);

        Data data = guardarData("Notificacion", tag, random);
        Workmanagernoti.Guardar(AlertTime, data, tag1);

        Toast.makeText(CreateTaskActivity.this, "Recordatorio Guardado", Toast.LENGTH_SHORT).show();
    }

    private void Eliminar(String tag) {
        WorkManager.getInstance(this).cancelAllWorkByTag(tag);
        Toast.makeText(CreateTaskActivity.this, "Recordatorio Eliminado", Toast.LENGTH_SHORT).show();
    }

    private Data guardarData(String titulo, String detalle, int id) {
        return new Data.Builder()
                .putString("titulo", titulo)
                .putString("detalle", detalle)
                .putInt("id", id).build();
    }

    private void initExtra() {
        final LinearLayout layoutExtra = findViewById(R.id.layoutExtra);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutExtra);

        layoutExtra.findViewById(R.id.textExtra).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        //imagen
        layoutExtra.findViewById(R.id.layoutAddImage).setOnClickListener((v) -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if (ContextCompat.checkSelfPermission(
                    getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        CreateTaskActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION
                );
            } else {
                selectImage();
            }
        });

        layoutExtra.findViewById(R.id.layoutAddCapture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        });

        layoutExtra.findViewById(R.id.layoutAddVideo).setOnClickListener((v) -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if (ContextCompat.checkSelfPermission(
                    getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        CreateTaskActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION
                );
            } else {
                selectVideo();
            }
        });

        layoutExtra.findViewById(R.id.layoutAddVideoCapture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
                }

            }
        });

        if (taskHecha != null) {
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

    private void showDeleteDialog() {
        if (dialogDeleteNote == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateTaskActivity.this);
            View v = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note, (ViewGroup) findViewById(R.id.layoutDeleteNoteContainer)
            );
            builder.setView(v);
            dialogDeleteNote = builder.create();
            if (dialogDeleteNote.getWindow() != null) {
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            v.findViewById(R.id.textDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    class DeleteTask extends AsyncTask<Void, Void, Void> {

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
                    Eliminar(taskName.getText().toString());
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

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    private void selectVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.imageRemove).setVisibility(View.VISIBLE);

                        selectedImagePath = getPathFromUri(selectedImageUri);
                    } catch (Exception exception) {
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                try {
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    imageNote.setImageBitmap(bitmap);
                    imageNote.setVisibility(View.VISIBLE);
                    findViewById(R.id.imageRemove).setVisibility(View.VISIBLE);

                    Uri tempUri = getImageUri(getApplicationContext(), bitmap);

                    // CALL THIS METHOD TO GET THE ACTUAL PATH
                    File finalFile = new File(getRealPathFromURI(tempUri));

                    selectedImagePath = getPathFromUri(tempUri);
                } catch (Exception ex) {
                    Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (requestCode == REQUEST_CODE_SELECT_VIDEO && resultCode == RESULT_OK) {
            if (data != null) {
                Uri capturedVideoUri = data.getData();
                if (capturedVideoUri != null) {
                    try {
                        videoNote.setVideoURI(capturedVideoUri);
                        videoNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.videoRemove).setVisibility(View.VISIBLE);
                        selectedVideoPath = getPathFromUri(capturedVideoUri);
                        videoNote.start();
                        videoNote.resolveAdjustedSize(80, 80);
                    } catch (Exception ex) {
                        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri capturedVideoUri = data.getData();
                if (capturedVideoUri != null) {
                    try {
                        videoNote.setVideoURI(capturedVideoUri);
                        videoNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.videoRemove).setVisibility(View.VISIBLE);
                        selectedVideoPath = getPathFromUri(capturedVideoUri);
                        videoNote.start();
                        videoNote.resolveAdjustedSize(80, 80);
                    } catch (Exception ex) {
                        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
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

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }
}
