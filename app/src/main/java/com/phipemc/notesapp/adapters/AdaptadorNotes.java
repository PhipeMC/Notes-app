package com.phipemc.notesapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.phipemc.notesapp.R;
import com.phipemc.notesapp.entities.Note;

import java.util.List;

public class AdaptadorNotes extends RecyclerView.Adapter<AdaptadorNotes.NoteViewHolder>{

    private List<Note> notas;

    public AdaptadorNotes(List<Note> notes){
        this.notas = notes;
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
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.setNote(notas.get(position));
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

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitulo = itemView.findViewById(R.id.textTitulo);
            textSubtitulo = itemView.findViewById(R.id.textSubtitulo);
            textFecha = itemView.findViewById(R.id.textFecha);
        }

        void setNote(Note nota){
            textTitulo.setText(nota.getTitle());
            if(nota.getSubtitle().trim().isEmpty()){
                textSubtitulo.setVisibility(View.GONE);
            }else{
                textSubtitulo.setText(nota.getSubtitle());
            }
            textFecha.setText(nota.getDate());
        }


    }

}
