package com.example.studentwell;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "todo_table")
public class ToDo {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String task;
    public String time; // you can use a timestamp string for now

    public ToDo(String task, String time) {
        this.task = task;
        this.time = time;
    }
}
