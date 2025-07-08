package com.example.studentwell;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ChartFragment extends Fragment {

    private BarChart barChart;
    private DatabaseReference dbRef;

    public ChartFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        barChart = view.findViewById(R.id.barChart);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            dbRef = FirebaseDatabase.getInstance()
                    .getReference("HealthData")
                    .child(user.getUid())
                    .child("logs");

            loadChartData();
        }

        return view;
    }

    private void loadChartData() {
        dbRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                List<BarEntry> entries = new ArrayList<>();
                List<String> dates = new ArrayList<>();
                int index = 0;

                for (DataSnapshot daySnapshot : task.getResult().getChildren()) {
                    String date = daySnapshot.getKey();
                    Long steps = daySnapshot.child("steps").getValue(Long.class);

                    if (steps != null) {
                        entries.add(new BarEntry(index, steps));
                        dates.add(date);
                        index++;
                    }
                }

                // Set up chart
                BarDataSet dataSet = new BarDataSet(entries, "Steps");
                BarData barData = new BarData(dataSet);
                barChart.setData(barData);
                barChart.getDescription().setText("Weekly Steps");

                XAxis xAxis = barChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                xAxis.setGranularity(1f);
                xAxis.setLabelCount(dates.size());
                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, AxisBase axis) {
                        int i = Math.round(value);
                        if (i >= 0 && i < dates.size()) {
                            return dates.get(i);
                        } else {
                            return "";
                        }
                    }
                });

                YAxis leftAxis = barChart.getAxisLeft();
                YAxis rightAxis = barChart.getAxisRight();
                rightAxis.setEnabled(false);
                leftAxis.setGranularity(1f);
                barChart.invalidate(); // refresh

                // ✅ Step advice logic (inside the same block)
                long totalSteps = 0;
                int days = entries.size();

                for (BarEntry entry : entries) {
                    totalSteps += entry.getY();
                }

                String advice;
                if (days > 0) {
                    float average = totalSteps / (float) days;
                    if (average >= 8000) {
                        advice = "🟢 Great job! You’re staying active. Keep it up!";
                    } else if (average >= 4000) {
                        advice = "🟡 You're doing okay, but try to move more each day.";
                    } else {
                        advice = "🔴 You’ve been inactive lately. Let’s get moving!";
                    }
                } else {
                    advice = "No step data available. Start walking today!";
                }

                TextView adviceText = requireView().findViewById(R.id.adviceText);
                adviceText.setText(advice);
            }
        });
    }

}
