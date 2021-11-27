package com.phipemc.notesapp.adapters;

import android.annotation.SuppressLint;
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
import com.phipemc.notesapp.listeners.TaskListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AdaptadorTasks extends RecyclerView.Adapter<AdaptadorTasks.TaskViewHolder>{
    private List<Task> tasks;
    private TaskListener taskListener;
    private Timer timer;
    private List<Task> taskSource;

    //Del main le pasamos los parametros necesarios para actuar con el adapatador
    public AdaptadorTasks(List<Task> tasks, TaskListener taskListener){
        this.tasks = tasks;
        this.taskListener = taskListener;
        taskSource = tasks;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TaskViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.all_notes_layout,
                        parent,
                        false
                )
        );
    }

    //Settea las nuevas notas en la vista segun el desplazamiento, les da el estilo
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.setTask(tasks.get(position));
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskListener.onTaskClicked(tasks.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    @Override
    public int getItemViewType(int position){
        return position;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder{

        TextView textTitulo, textFecha;
        LinearLayout layoutNote;
        RoundedImageView imageNote;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitulo = itemView.findViewById(R.id.textTitulo);
            textFecha = itemView.findViewById(R.id.textSubtitulo);
            layoutNote = itemView.findViewById(R.id.layoutNote);
            imageNote = itemView.findViewById(R.id.imageNote);
        }

        void setTask(Task task){
            //Estructura de la vista previa
            textTitulo.setText(task.getTitle());
            textFecha.setText(task.getDate());

            ///vista previa de la nota
            if(task.getImgpath() != null){
                imageNote.setImageBitmap(BitmapFactory.decodeFile(task.getImgpath()));
                imageNote.setVisibility(View.VISIBLE);
            }else{
                imageNote.setVisibility(View.GONE);
            }
        }

    }

    ///Para la busqueda de tareas
    public void searchTasks(final String searchKeyword){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(searchKeyword.trim().isEmpty()){
                    tasks = taskSource;
                } else {
                    ArrayList<Task> temp = new ArrayList<>();
                    for(Task task : taskSource){
                        if(task.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())){
                            temp.add(task);
                        }
                    }
                    tasks = temp;
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
