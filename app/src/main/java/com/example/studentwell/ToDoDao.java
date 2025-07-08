package com.example.studentwell;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Dao
public interface ToDoDao {

    @Insert
    void insert(ToDo todo);

    @Delete
    void delete(ToDo todo);

    @Query("SELECT * FROM todo_table ORDER BY id DESC")
    LiveData<List<ToDo>> getAllTasks();
}
