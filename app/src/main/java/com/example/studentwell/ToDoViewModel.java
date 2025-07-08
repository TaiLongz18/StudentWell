package com.example.studentwell;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ToDoViewModel extends AndroidViewModel {

    private final ToDoRepository repository;
    private final LiveData<List<ToDo>> allTasks;

    public ToDoViewModel(@NonNull Application application) {
        super(application);
        repository = new ToDoRepository(application);
        allTasks = repository.getAllTasks();
    }

    public void insert(ToDo todo) {
        repository.insert(todo);
    }

    public void delete(ToDo todo) {
        repository.delete(todo);
    }

    public LiveData<List<ToDo>> getAllTasks() {
        return allTasks;
    }
}
