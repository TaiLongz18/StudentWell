package com.example.studentwell;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText usernameRegister, emailRegister, passwordRegister;
    Button btnRegister;
    TextView loginRedirect;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Bind UI
        usernameRegister = findViewById(R.id.usernameRegister);
        emailRegister = findViewById(R.id.emailRegister);
        passwordRegister = findViewById(R.id.passwordRegister);
        btnRegister = findViewById(R.id.btnRegister);
        loginRedirect = findViewById(R.id.loginRedirect);

        mAuth = FirebaseAuth.getInstance();

        ImageView togglePassword = findViewById(R.id.toggleRegisterPasswordVisibility);
        togglePassword.setOnClickListener(v -> {
            if (passwordRegister.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                passwordRegister.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePassword.setImageResource(R.drawable.ic_eye_open);
            } else {
                passwordRegister.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePassword.setImageResource(R.drawable.ic_eye_closed);
            }
            passwordRegister.setSelection(passwordRegister.length());
        });

        // ✅ Register button logic
        btnRegister.setOnClickListener(v -> {
            String username = usernameRegister.getText().toString().trim();
            String email = emailRegister.getText().toString().trim();
            String password = passwordRegister.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = mAuth.getCurrentUser().getUid();

                            // Create user data
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("username", username);
                            userData.put("email", email);

                            FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(uid)
                                    .setValue(userData)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, LoginActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // ✅ Login redirect
        loginRedirect.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Styled login link
        loginRedirect.setText(Html.fromHtml(getString(R.string.login_redirect_text)));
    }
}
