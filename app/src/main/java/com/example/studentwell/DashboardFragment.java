package com.example.studentwell;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class DashboardFragment extends Fragment implements SensorEventListener {

    private static final int ACTIVITY_RECOGNITION_CODE = 200;

    private TextView stepCountText;
    private EditText inputWater, inputSleep;
    private Button btnSaveWater, btnSaveSleep;
    private BarChart barChart;

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private boolean isSensorAvailable;
    private int stepCount = 0, initialStepCount = -1;

    private DatabaseReference dbRef;
    private FirebaseUser user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference("HealthData").child(user.getUid());

        stepCountText = view.findViewById(R.id.stepCountText);
        inputWater = view.findViewById(R.id.inputWater);
        inputSleep = view.findViewById(R.id.inputSleep);
        btnSaveWater = view.findViewById(R.id.btnSaveWater);
        btnSaveSleep = view.findViewById(R.id.btnSaveSleep);
        barChart = view.findViewById(R.id.barChart);

        SharedPreferences prefs = requireActivity().getSharedPreferences("StepPrefs", Context.MODE_PRIVATE);
        initialStepCount = prefs.getInt("initialStep", -1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, ACTIVITY_RECOGNITION_CODE);
            }
        }

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            isSensorAvailable = true;
        } else {
            stepCountText.setText("Step Counter Sensor Not Available!");
            isSensorAvailable = false;
        }

        btnSaveWater.setOnClickListener(v -> {
            String water = inputWater.getText().toString().trim();
            if (!water.isEmpty()) {
                dbRef.child("logs").child(getToday()).child("water").setValue(water);
                Toast.makeText(getContext(), "Water intake saved", Toast.LENGTH_SHORT).show();
                loadChartData();
            } else {
                Toast.makeText(getContext(), "Please enter water amount", Toast.LENGTH_SHORT).show();
            }
        });

        btnSaveSleep.setOnClickListener(v -> {
            String sleep = inputSleep.getText().toString().trim();
            if (!sleep.isEmpty()) {
                dbRef.child("logs").child(getToday()).child("sleep").setValue(sleep);
                Toast.makeText(getContext(), "Sleep hours saved", Toast.LENGTH_SHORT).show();
                loadChartData();
            } else {
                Toast.makeText(getContext(), "Please enter sleep hours", Toast.LENGTH_SHORT).show();
            }
        });

        loadChartData();

        return view;
    }

    private String getToday() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private void loadChartData() {
        dbRef.child("logs").child(getToday()).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                float water = snapshot.child("water").getValue() != null ? Float.parseFloat(snapshot.child("water").getValue().toString()) : 0f;
                float sleep = snapshot.child("sleep").getValue() != null ? Float.parseFloat(snapshot.child("sleep").getValue().toString()) : 0f;

                List<BarEntry> entries = new ArrayList<>();
                entries.add(new BarEntry(0, water));
                entries.add(new BarEntry(1, sleep));

                BarDataSet dataSet = new BarDataSet(entries, "Today's Logs");
                dataSet.setColors(new int[]{R.color.teal_700, R.color.purple_700}, requireContext());
                BarData data = new BarData(dataSet);
                data.setBarWidth(0.4f);

                barChart.setData(data);

                String[] labels = {"Water (ml)", "Sleep (hr)"};
                XAxis xAxis = barChart.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularity(1f);
                xAxis.setGranularityEnabled(true);

                YAxis leftAxis = barChart.getAxisLeft();
                leftAxis.setGranularity(1f);
                barChart.getAxisRight().setEnabled(false);
                barChart.getDescription().setEnabled(false);
                barChart.setFitBars(true);
                barChart.invalidate();
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int currentSteps = (int) event.values[0];

        if (initialStepCount == -1 || currentSteps < initialStepCount) {
            initialStepCount = currentSteps;
            SharedPreferences.Editor editor = requireActivity()
                    .getSharedPreferences("StepPrefs", Context.MODE_PRIVATE).edit();
            editor.putInt("initialStep", initialStepCount);
            editor.apply();
        }

        stepCount = currentSteps - initialStepCount;
        if (stepCount < 0) stepCount = 0;

        stepCountText.setText("Steps: " + stepCount);
        dbRef.child("logs").child(getToday()).child("steps").setValue(stepCount);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onResume() {
        super.onResume();
        if (isSensorAvailable) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isSensorAvailable) {
            sensorManager.unregisterListener(this);
        }
    }
}
