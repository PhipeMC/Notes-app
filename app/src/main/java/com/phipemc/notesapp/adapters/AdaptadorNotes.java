package com.phipemc.notesapp.adapters;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.phipemc.notesapp.R;
import com.phipemc.notesapp.entities.Note;
import com.phipemc.notesapp.entities.Task;
import com.phipemc.notesapp.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class AdaptadorNotes extends RecyclerView.Adapter<AdaptadorNotes.NoteViewHolder> {

    private List<Object> papers;
    private NotesListener notesListener;
    private Timer timer;
    private List<Object> papersSource;

    //Del main le pasamos los parametros necesarios para actuar con el adapatador
    public AdaptadorNotes(List<Object> papers, NotesListener notesListener) {
        this.papers = papers;
        this.notesListener = notesListener;
        papersSource = papers;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.all_notes_layout,
                        parent,
                        false
                )
        );
    }

    //Settea las nuevas notas en la vista segun el desplazamiento, les da el estilo
    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.setPaper(papers.get(position));
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notesListener.onNoteClicked(papers.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return papers.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView textTitulo, textSubtitulo, textFecha;
        LinearLayout layoutNote;
        RoundedImageView imageNote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitulo = itemView.findViewById(R.id.textTitulo);
            textSubtitulo = itemView.findViewById(R.id.textSubtitulo);
            textFecha = itemView.findViewById(R.id.textFecha);
            layoutNote = itemView.findViewById(R.id.layoutNote);
            imageNote = itemView.findViewById(R.id.imageNote);
        }

        void setPaper(Object paper) {
            if (paper instanceof Task) {
                //Es una tarea
                Task task = (Task) paper;

                //Apariencia de la tarea
                textTitulo.setText(task.getTitle());
                textFecha.setText(task.getDate());
                textSubtitulo.setVisibility(View.GONE);

                //Color de la tarea
                GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
                if (task.getColor() != null) {
                    gradientDrawable.setColor(Color.parseColor(task.getColor()));
                } else {
                    gradientDrawable.setColor(Color.parseColor("#333333"));
                }

                //Vista previa de la tarea con la imagen, SOLO IMAGEN
                if (task.getImgpath() != null) {
                    imageNote.setImageBitmap(BitmapFactory.decodeFile(task.getImgpath()));
                    imageNote.setVisibility(View.VISIBLE);
                } else {
                    imageNote.setVisibility(View.GONE);
                }

            } else {
                //Es una nota
                Note note = (Note) paper;

                //Apariencia de la nota
                textTitulo.setText(note.getTitle());
                textFecha.setText(note.getDate());
                if (note.getSubtitle().trim().isEmpty()) {
                    textSubtitulo.setVisibility(View.GONE);
                } else {
                    textSubtitulo.setText(note.getSubtitle());
                }

                //Color de la nota
                GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
                if (note.getColor() != null) {
                    gradientDrawable.setColor(Color.parseColor(note.getColor()));
                } else {
                    gradientDrawable.setColor(Color.parseColor("#333333"));
                }

                //Vista previa de la nota con la imagen, SOLO IMAGEN
                if (note.getImgpath() != null) {
                    imageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImgpath()));
                    imageNote.setVisibility(View.VISIBLE);
                } else {
                    imageNote.setVisibility(View.GONE);
                }
            }
        }

    }

    ///Prara la busqueda de notas
    public void searchNotes(final String searchKeyword) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyword.trim().isEmpty()) {
                    papers = papersSource;
                } else {
                    ArrayList<Object> temp = new ArrayList<>();
                    for (Object paper : papersSource) {
                        if(paper instanceof Task){
                            Task t = (Task) paper;
                            if(t.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())){
                                temp.add(paper);
                            }
                        }else{
                            Note n = (Note) paper;
                            if (n.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                    || n.getSubtitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                    || n.getNote_text().toLowerCase().contains(searchKeyword.toLowerCase())){
                                temp.add(paper);
                            }

                        }


                    }
                    papers = temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

}
