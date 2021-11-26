package com.phipemc.notesapp.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "notes")
public class Note implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "subtitle")
    private String subtitle;

    @ColumnInfo(name = "note_text")
    private String note_text;

    @ColumnInfo(name = "priority")
    private String priority;

    @ColumnInfo(name = "img_path")
    private String imgpath;

    @ColumnInfo(name = "web_link")
    private String web_link;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @ColumnInfo(name = "color")
    private String color;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getNote_text() {
        return note_text;
    }

    public void setNote_text(String note_text) {
        this.note_text = note_text;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getImgpath() {
        return imgpath;
    }

    public void setImgpath(String imgpath) {
        this.imgpath = imgpath;
    }

    public String getWeb_link() {
        return web_link;
    }

    public void setWeb_link(String web_link) {
        this.web_link = web_link;
    }

    @NonNull
    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", date='" + date + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", note_text='" + note_text + '\'' +
                ", priority='" + priority + '\'' +
                ", imgpath='" + imgpath + '\'' +
                ", web_link='" + web_link + '\'' +
                '}';
    }

    @Entity(tableName = "tareas")
    public class Tareas implements Serializable {
        @PrimaryKey(autoGenerate = true)
        private int id;

        @ColumnInfo(name = "title")
        private String title;

        @ColumnInfo(name = "date")
        private String date;

        @ColumnInfo(name = "img_path")
        private String imgpath;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getImgpath() {
            return imgpath;
        }

        public void setImgpath(String imgpath) {
            this.imgpath = imgpath;
        }

        @Override
        public String toString() {
            return "Tareas{" +
                    "id=" + id +
                    ", title='" + title + '\'' +
                    ", date='" + date + '\'' +
                    ", imgpath='" + imgpath + '\'' +
                    '}';
        }
    }
}
