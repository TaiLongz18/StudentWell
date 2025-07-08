package com.example.studentwell;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.studentwell.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme from saved preference
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_theme_enabled", false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get the navigation header view
        View headerView = binding.navigationView.getHeaderView(0);
        View navHeaderContainer = headerView.findViewById(R.id.navHeaderContainer);

        // Apply top padding for status bar
        ViewCompat.setOnApplyWindowInsetsListener(navHeaderContainer, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            v.setPadding(
                    v.getPaddingLeft(),
                    topInset + 16,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });
        ViewCompat.requestApplyInsets(navHeaderContainer);

        // Set Toolbar title
        binding.toolbarTitle.setText("Dashboard");

        // Load default fragment
        loadFragment(new DashboardFragment());

        // Hamburger icon opens drawer
        binding.topAppBar.setNavigationOnClickListener(v ->
                binding.drawerLayout.openDrawer(GravityCompat.START)
        );

        // Bottom navigation item selection
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "";

            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragment();
                title = "Dashboard";
            } else if (id == R.id.nav_chart) {
                selectedFragment = new ChartFragment();
                title = "Weekly Chart";
            } else if (id == R.id.nav_time) {
                selectedFragment = new ToDoFragment();
                title = "Time Management";
            } else if (id == R.id.nav_map) {
                selectedFragment = new MapFragment();
                title = "Find Healthy Places";
            } else if (id == R.id.nav_about) {
                selectedFragment = new AboutFragment();
                title = "About App";
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                binding.toolbarTitle.setText(title);
                return true;
            }

            return false;
        });

        // Navigation drawer menu logic
        binding.navigationView.setNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            String title = "";

            int id = item.getItemId();
            if (id == R.id.nav_user_info) {
                fragment = new UserInfoFragment();
                title = "User Info";
            } else if (id == R.id.nav_faq) {
                fragment = new FaqFragment();
                title = "FAQ";
            } else if (id == R.id.nav_about) {
                fragment = new AboutFragment();
                title = "About App";
            } else if (id == R.id.nav_logout) {
                showLogoutDialog();
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            if (fragment != null) {
                loadFragment(fragment);
                binding.toolbarTitle.setText(title);
            }

            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        refreshNavHeaderImage();
    }

    public void refreshNavHeaderImage() {
        View headerView = binding.navigationView.getHeaderView(0);
        ImageView navImage = headerView.findViewById(R.id.nav_header_image);
        TextView navUsername = headerView.findViewById(R.id.nav_header_username);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String name = snapshot.child("username").getValue(String.class);
                String imgBase64 = snapshot.child("profileBase64").getValue(String.class);

                navUsername.setText(name != null ? name : "Student");

                if (imgBase64 != null && !imgBase64.isEmpty()) {
                    navImage.setImageBitmap(ImageUtils.base64ToBitmap(imgBase64));
                } else {
                    navImage.setImageResource(R.drawable.profile_circle);
                }
            }
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
