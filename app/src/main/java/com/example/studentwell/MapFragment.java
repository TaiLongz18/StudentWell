package com.example.studentwell;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MapFragment extends Fragment {

    public MapFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        Button btnPark = view.findViewById(R.id.btnPark);
        Button btnGym = view.findViewById(R.id.btnGym);
        Button btnSports = view.findViewById(R.id.btnSports);

        btnPark.setOnClickListener(v -> openGoogleMaps("park near me"));
        btnGym.setOnClickListener(v -> openGoogleMaps("gym near me"));
        btnSports.setOnClickListener(v -> openGoogleMaps("sports center near me"));

        return view;
    }

    private void openGoogleMaps(String query) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }
}
