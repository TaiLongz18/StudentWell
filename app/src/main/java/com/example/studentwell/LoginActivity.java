package com.example.studentwell;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText emailLogin, passwordLogin;
    ImageView togglePassword;
    Button btnLogin;
    TextView registerRedirect;
    FirebaseAuth mAuth;

    boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailLogin = findViewById(R.id.emailLogin);
        passwordLogin = findViewById(R.id.passwordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        togglePassword = findViewById(R.id.togglePasswordVisibility);
        registerRedirect = findViewById(R.id.registerRedirect);
        mAuth = FirebaseAuth.getInstance();

        // Load saved email
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedEmail = prefs.getString("email", "");
        emailLogin.setText(savedEmail);

        // Show/hide password toggle
        togglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordLogin.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePassword.setImageResource(R.drawable.ic_eye_closed);
                isPasswordVisible = false;
            } else {
                passwordLogin.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePassword.setImageResource(R.drawable.ic_eye_open);
                isPasswordVisible = true;
            }
            passwordLogin.setSelection(passwordLogin.getText().length());
        });

        // Login logic
        btnLogin.setOnClickListener(v -> {
            String email = emailLogin.getText().toString().trim();
            String password = passwordLogin.getText().toString().trim();

            if (email.isEmpty()) {
                emailLogin.setError("Email is required");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLogin.setError("Invalid email format");
                return;
            }

            if (password.isEmpty()) {
                passwordLogin.setError("Password is required");
                return;
            }

            // Firebase login
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Save email to SharedPreferences
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("email", email);
                            editor.apply();

                            // Go to main activity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        registerRedirect.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }
}
