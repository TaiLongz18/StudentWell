package com.example.studentwell;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private ViewPager2 splashViewPager;
    private Button btnGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views
        splashViewPager = findViewById(R.id.splashViewPager);
        btnGetStarted = findViewById(R.id.btnGetStarted);

        // Prepare splash content
        List<SplashItem> splashList = new ArrayList<>();
        splashList.add(new SplashItem(R.drawable.splash1, "View progress & stay motivated 📊" ));
        splashList.add(new SplashItem(R.drawable.splash2, "Track your steps effortlessly! 🚶‍♀️"));
        splashList.add(new SplashItem(R.drawable.splash3, "Log your water & sleep daily 💧😴"));
        splashList.add(new SplashItem(R.drawable.splash4, "Explore gyms, parks & more near you 🏞️"));

        // Set adapter
        SplashAdapter adapter = new SplashAdapter(this, splashList);
        splashViewPager.setAdapter(adapter);

        // Dot indicator setup
        CircleIndicator3 indicator = findViewById(R.id.dotsIndicator);
        indicator.setViewPager(splashViewPager);

        // Smooth slide + zoom-out effect
        // Inside SplashActivity.java (update only the transformer)
        splashViewPager.setPageTransformer((page, position) -> {
            float scale = 1 - Math.abs(position * 0.02f); // Zoom out by 2%
            page.setScaleX(scale);
            page.setScaleY(scale);
            // Remove translationX for slide
        });

        // Show "Get Started" on last page only
        splashViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                btnGetStarted.setVisibility(position == splashList.size() - 1 ? View.VISIBLE : View.GONE);
            }
        });

        // Button click -> go to login
        btnGetStarted.setOnClickListener(v -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        });
    }
}
