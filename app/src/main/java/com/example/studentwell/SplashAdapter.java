package com.example.studentwell;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SplashAdapter extends RecyclerView.Adapter<SplashAdapter.SplashViewHolder> {

    private final Context context;
    private final List<SplashItem> splashItems;

    public SplashAdapter(Context context, List<SplashItem> splashItems) {
        this.context = context;
        this.splashItems = splashItems;
    }

    @NonNull
    @Override
    public SplashViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_splash, parent, false);
        return new SplashViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SplashViewHolder holder, int position) {
        SplashItem item = splashItems.get(position);
        holder.splashImage.setImageResource(item.imageRes);
        holder.splashText.setText(item.text);

        // Apply zoom-out animation
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.zoom_out);
        holder.splashImage.startAnimation(anim);
    }

    @Override
    public int getItemCount() {
        return splashItems.size();
    }

    // ✅ Changed to public static
    public static class SplashViewHolder extends RecyclerView.ViewHolder {
        ImageView splashImage;
        TextView splashText;

        public SplashViewHolder(@NonNull View itemView) {
            super(itemView);
            splashImage = itemView.findViewById(R.id.splashImage);
            splashText = itemView.findViewById(R.id.splashText);
        }
    }
}
