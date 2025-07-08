package com.example.studentwell;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.Locale;

public class ToDoFragment extends Fragment {

    private ToDoViewModel viewModel;
    private EditText inputTask, inputTime;

    public ToDoFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_todo, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        inputTask = view.findViewById(R.id.inputTask);
        inputTime = view.findViewById(R.id.inputTime);
        Button btnAdd = view.findViewById(R.id.btnAddTask);
        RecyclerView recyclerView = view.findViewById(R.id.todoRecyclerView);

        ToDoAdapter adapter = new ToDoAdapter(todo -> viewModel.delete(todo));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel = new ViewModelProvider(this).get(ToDoViewModel.class);
        viewModel.getAllTasks().observe(getViewLifecycleOwner(), adapter::setTodoList);

        inputTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                    (view1, hourOfDay, minuteOfHour) -> {
                        String formatted = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                        inputTime.setText(formatted);
                    }, hour, minute, true);
            timePickerDialog.show();
        });

        btnAdd.setOnClickListener(v -> {
            String task = inputTask.getText().toString().trim();
            String time = inputTime.getText().toString().trim();

            if (!task.isEmpty() && !time.isEmpty()) {
                viewModel.insert(new ToDo(task, time));
                scheduleNotification(task, time);
                inputTask.setText("");
                inputTime.setText("");
                Toast.makeText(getContext(), "Task added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Please enter task and time", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void scheduleNotification(String task, String time) {
        String[] parts = time.split(":");
        if (parts.length != 2) return;

        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(getContext(), NotificationReceiver.class);
        intent.putExtra("task", task);

        int requestCode = (task + time).hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(),
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Intent settingsIntent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(settingsIntent);
                    return;
                }
            }

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }
}
