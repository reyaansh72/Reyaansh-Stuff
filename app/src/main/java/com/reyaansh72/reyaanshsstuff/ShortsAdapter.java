package com.reyaansh72.reyaanshsstuff;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ShortsAdapter extends RecyclerView.Adapter<ShortsAdapter.ShortViewHolder> {

    public interface OnCommentClickListener {
        void onCommentClick(ShortItem item);
    }

    private final List<ShortItem> shortsList;
    private final OnCommentClickListener commentClickListener;
    private final String currentUserId;
    private final DatabaseReference databaseRef;

    public ShortsAdapter(List<ShortItem> shortsList, OnCommentClickListener commentClickListener) {
        this.shortsList = shortsList;
        this.commentClickListener = commentClickListener;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        this.databaseRef = FirebaseDatabase.getInstance().getReference("shorts");
    }

    @NonNull
    @Override
    public ShortViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_short, parent, false);
        return new ShortViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShortViewHolder holder, int position) {
        ShortItem item = shortsList.get(position);
        Context context = holder.itemView.getContext();

        // Username
        String username = item.getUsername();
        if (username == null || username.trim().isEmpty()) {
            username = "Anonymous";
        }
        holder.tvShortUsername.setText("@" + username);

        // Title
        holder.tvShortTitle.setText(item.getTitle() != null ? item.getTitle() : "");

        // Setup ExoPlayer
        if (item.getVideoUrl() != null && !item.getVideoUrl().isEmpty()) {
            ExoPlayer player = new ExoPlayer.Builder(context).build();
            holder.shortPlayerView.setPlayer(player);
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(item.getVideoUrl()));
            player.setMediaItem(mediaItem);
            player.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);
            player.prepare();
            player.play();
        }

        // Likes & Comments Count
        holder.tvShortLikeCount.setText(String.valueOf(item.getLikesCount()));
        holder.tvShortCommentCount.setText(String.valueOf(item.getCommentsCount()));

        // Profile Click
        View.OnClickListener profileListener = v -> {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("profile_user_id", item.getUserId());
            intent.putExtra("profile_username", item.getUsername());
            context.startActivity(intent);
        };
        holder.tvShortUsername.setOnClickListener(profileListener);
        holder.btnShortAvatar.setOnClickListener(profileListener);

        // Check if current user is owner for edit/delete
        if (currentUserId.equals(item.getUserId())) {
            holder.btnShortEdit.setVisibility(View.VISIBLE);
            holder.btnShortDelete.setVisibility(View.VISIBLE);

            holder.btnShortDelete.setOnClickListener(v -> {
                if (item.getId() != null) {
                    databaseRef.child(item.getId()).removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Short deleted!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else {
            holder.btnShortEdit.setVisibility(View.GONE);
            holder.btnShortDelete.setVisibility(View.GONE);
        }

        // Like click
        holder.btnShortLike.setOnClickListener(v -> {
            if (currentUserId.isEmpty() || item.getId() == null) return;
            DatabaseReference shortRef = databaseRef.child(item.getId());
            DatabaseReference userLikeRef = shortRef.child("likes").child(currentUserId);

            userLikeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean currentlyLiked = snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                    long currentLikes = item.getLikesCount();

                    if (currentlyLiked) {
                        userLikeRef.removeValue();
                        long newLikes = Math.max(0, currentLikes - 1);
                        shortRef.child("likesCount").setValue(newLikes);
                        item.setLikesCount(newLikes);
                        holder.tvShortLikeCount.setText(String.valueOf(newLikes));
                        holder.btnShortLike.setImageResource(R.drawable.ic_favorite_border_24);
                    } else {
                        userLikeRef.setValue(true);
                        long newLikes = currentLikes + 1;
                        shortRef.child("likesCount").setValue(newLikes);
                        item.setLikesCount(newLikes);
                        holder.tvShortLikeCount.setText(String.valueOf(newLikes));
                        holder.btnShortLike.setImageResource(R.drawable.ic_favorite_24);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        });

        // Comment click
        holder.btnShortComment.setOnClickListener(v -> {
            if (commentClickListener != null) {
                commentClickListener.onCommentClick(item);
            }
        });

        // Share click
        holder.btnShortShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Watch @" + item.getUsername() + "'s Short:\n" + item.getTitle() + "\n" + item.getVideoUrl());
            context.startActivity(Intent.createChooser(shareIntent, "Share Short"));
        });
    }

    @Override
    public int getItemCount() {
        return shortsList.size();
    }

    static class ShortViewHolder extends RecyclerView.ViewHolder {
        PlayerView shortPlayerView;
        TextView tvShortUsername;
        TextView tvShortTitle;
        TextView tvShortLikeCount;
        TextView tvShortCommentCount;
        ImageButton btnShortAvatar;
        ImageButton btnShortLike;
        ImageButton btnShortComment;
        ImageButton btnShortShare;
        ImageButton btnShortEdit;
        ImageButton btnShortDelete;

        public ShortViewHolder(@NonNull View itemView) {
            super(itemView);
            shortPlayerView = itemView.findViewById(R.id.shortPlayerView);
            tvShortUsername = itemView.findViewById(R.id.tvShortUsername);
            tvShortTitle = itemView.findViewById(R.id.tvShortTitle);
            tvShortLikeCount = itemView.findViewById(R.id.tvShortLikeCount);
            tvShortCommentCount = itemView.findViewById(R.id.tvShortCommentCount);
            btnShortAvatar = itemView.findViewById(R.id.btnShortAvatar);
            btnShortLike = itemView.findViewById(R.id.btnShortLike);
            btnShortComment = itemView.findViewById(R.id.btnShortComment);
            btnShortShare = itemView.findViewById(R.id.btnShortShare);
            btnShortEdit = itemView.findViewById(R.id.btnShortEdit);
            btnShortDelete = itemView.findViewById(R.id.btnShortDelete);
        }
    }
}