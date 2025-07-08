package com.example.studentwell;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;
import com.example.studentwell.DashboardFragment;

import java.io.IOException;

public class UserInfoFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImage;
    private EditText editUsername, editEmail;
    private Button saveBtn, changeImageBtn, backBtn;
    private Uri imageUri;

    private DatabaseReference userRef;
    private StorageReference storageRef;
    private String uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_info, container, false);

        // Initialize views
        profileImage = view.findViewById(R.id.profileImage);
        editUsername = view.findViewById(R.id.editUsername);
        editEmail = view.findViewById(R.id.editEmail);
        saveBtn = view.findViewById(R.id.saveBtn);
        changeImageBtn = view.findViewById(R.id.changeImageBtn);
        backBtn = view.findViewById(R.id.backBtn);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        storageRef = FirebaseStorage.getInstance().getReference("profile_pictures").child(uid + ".jpg");

        loadUserData();

        changeImageBtn.setOnClickListener(v -> openFileChooser());

        saveBtn.setOnClickListener(v -> {
            saveUserData();
            if (imageUri != null) {
                uploadProfilePicture();
            }
        });

        backBtn.setOnClickListener(v -> {
            // Navigate to DashboardFragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new DashboardFragment())
                    .commit();
        });

        return view;
    }

    private void loadUserData() {
        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                // Set username
                editUsername.setText(snapshot.child("username").getValue(String.class));

                // Set email (non-editable)
                editEmail.setText(snapshot.child("email").getValue(String.class));
                editEmail.setEnabled(false); // disable editing
                editEmail.setTextColor(getResources().getColor(R.color.grey)); // optional: gray text

                // Load profile image from Base64 string
                String base64Str = snapshot.child("profileBase64").getValue(String.class);
                if (base64Str != null && !base64Str.isEmpty()) {
                    Bitmap bitmap = ImageUtils.base64ToBitmap(base64Str);
                    profileImage.setImageBitmap(bitmap);
                } else {
                    profileImage.setImageResource(R.drawable.profile_circle); // fallback/default image
                }
            }
        });
    }


    private void saveUserData() {
        userRef.child("username").setValue(editUsername.getText().toString());
        userRef.child("email").setValue(editEmail.getText().toString());
        Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
    }

    private void uploadProfilePicture() {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
            String base64String = ImageUtils.bitmapToBase64(bitmap);

            userRef.child("profileBase64").setValue(base64String)
                    .addOnSuccessListener(aVoid -> {
                        profileImage.setImageBitmap(bitmap);
                        Toast.makeText(getContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();

                        // 🔁 Refresh nav header image
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).refreshNavHeaderImage();
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error converting image", Toast.LENGTH_SHORT).show();
        }
    }




    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }
}
