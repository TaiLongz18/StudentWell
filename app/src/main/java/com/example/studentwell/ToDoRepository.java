package com.example.studentwell;

import android.app.Application;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ToDoRepository {
    private final ToDoDao toDoDao;
    private final LiveData<List<ToDo>> allTasks;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ToDoRepository(Application application) {
        ToDoDatabase db = ToDoDatabase.getInstance(application);
        toDoDao = db.toDoDao();
        allTasks = toDoDao.getAllTasks();
    }

    public void insert(ToDo todo) {
        executor.execute(() -> toDoDao.insert(todo));
    }

    public void delete(ToDo todo) {
        executor.execute(() -> toDoDao.delete(todo));
    }

    public LiveData<List<ToDo>> getAllTasks() {
        return allTasks;
    }
}
