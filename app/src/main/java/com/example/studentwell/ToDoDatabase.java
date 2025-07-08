package com.example.studentwell;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ToDo.class}, version = 1)
public abstract class ToDoDatabase extends RoomDatabase {

    private static ToDoDatabase instance;

    public abstract ToDoDao toDoDao();

    public static synchronized ToDoDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            ToDoDatabase.class, "todo_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
