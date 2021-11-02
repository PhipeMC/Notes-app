package com.phipemc.notesapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

import java.util.List;

public class AdaptadorNotes extends RecyclerView.Adapter<AdaptadorNotes.NoteViewHolder>{

    private List<Note> notas;
    private NotesListener notesListener;

    public AdaptadorNotes(List<Note> notes, NotesListener notesListener){
        this.notas = notes;
        this.notesListener = notesListener;
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

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, final int position) {
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

        TextView textTitulo, textSubtitulo, textFecha;
        LinearLayout layoutNote;
        RoundedImageView imageNote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitulo = itemView.findViewById(R.id.textTitulo);
            textSubtitulo = itemView.findViewById(R.id.textSubtitulo);
            //textFecha = itemView.findViewById(R.id.textFecha);
            layoutNote = itemView.findViewById(R.id.layoutNote);
            imageNote = itemView.findViewById(R.id.imageNote);
        }

        void setNote(Note nota){
            textTitulo.setText(nota.getTitle());
            if(nota.getSubtitle().trim().isEmpty()){
                textSubtitulo.setVisibility(View.GONE);
            }else{
                textSubtitulo.setText(nota.getSubtitle());
            }
            //textFecha.setText(nota.getDate());

            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if(nota.getColor() != null){
                gradientDrawable.setColor(Color.parseColor(nota.getColor()));
            }else{
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            if(nota.getImgpath() != null){
                imageNote.setImageBitmap(BitmapFactory.decodeFile(nota.getImgpath()));
                imageNote.setVisibility(View.VISIBLE);
            }else{
                imageNote.setVisibility(View.GONE);
            }
        }

    }
}
