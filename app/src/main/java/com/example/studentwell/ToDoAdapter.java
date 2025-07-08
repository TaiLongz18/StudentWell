package com.example.studentwell;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder> {

    private List<ToDo> todoList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDelete(ToDo todo);
    }

    public ToDoAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setTodoList(List<ToDo> list) {
        this.todoList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ToDoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo, parent, false);
        return new ToDoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoViewHolder holder, int position) {
        ToDo todo = todoList.get(position);
        holder.textTask.setText(todo.task);
        holder.textTime.setText(todo.time);
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(todo));
    }

    @Override
    public int getItemCount() {
        return todoList == null ? 0 : todoList.size();
    }

    static class ToDoViewHolder extends RecyclerView.ViewHolder {
        TextView textTask, textTime;
        ImageButton btnDelete;

        public ToDoViewHolder(@NonNull View itemView) {
            super(itemView);
            textTask = itemView.findViewById(R.id.textTask);
            textTime = itemView.findViewById(R.id.textTime);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
