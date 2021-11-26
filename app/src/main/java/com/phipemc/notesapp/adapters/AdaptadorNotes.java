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
import com.phipemc.notesapp.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class AdaptadorNotes extends RecyclerView.Adapter<AdaptadorNotes.NoteViewHolder>{

    private List<Note> notas;
    private NotesListener notesListener;
    private Timer timer;
    private List<Note> notesSource;

    //Del main le pasamos los parametros necesarios para actuar con el adapatador
    public AdaptadorNotes(List<Note> notes, NotesListener notesListener){
        this.notas = notes;
        this.notesListener = notesListener;
        notesSource = notes;
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
        holder.setNote(notas.get(position));
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notesListener.onNoteClicked(notas.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notas.size();
    }

    @Override
    public int getItemViewType(int position){
        return position;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder{

        TextView textTitulo, textSubtitulo;
        LinearLayout layoutNote;
        RoundedImageView imageNote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitulo = itemView.findViewById(R.id.textTitulo);
            textSubtitulo = itemView.findViewById(R.id.textSubtitulo);
            layoutNote = itemView.findViewById(R.id.layoutNote);
            imageNote = itemView.findViewById(R.id.imageNote);
        }

        void setNote(Note nota){
            //Estructura de la vista previa
            textTitulo.setText(nota.getTitle());
            if(nota.getSubtitle().trim().isEmpty()){
                textSubtitulo.setVisibility(View.GONE);
            }else{
                textSubtitulo.setText(nota.getSubtitle());
            }

            //Color de la nota
            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if(nota.getColor() != null){
                gradientDrawable.setColor(Color.parseColor(nota.getColor()));
            }else{
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            ///vista previa de la nota
            if(nota.getImgpath() != null){
                imageNote.setImageBitmap(BitmapFactory.decodeFile(nota.getImgpath()));
                imageNote.setVisibility(View.VISIBLE);
            }else{
                imageNote.setVisibility(View.GONE);
            }
        }

    }

    ///Prara la busqueda de notas
    public void searchNotes(final String searchKeyword){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(searchKeyword.trim().isEmpty()){
                    notas = notesSource;
                } else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for(Note note : notesSource){
                        if(note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())
                            || note.getSubtitle().toLowerCase().contains(searchKeyword.toLowerCase())
                            || note.getNote_text().toLowerCase().contains(searchKeyword.toLowerCase())){
                            temp.add(note);
                        }
                    }
                    notas = temp;
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

    public void cancelTimer(){
        if(timer != null){
            timer.cancel();
        }
    }

}
