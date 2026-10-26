package com.reyaansh72.reyaanshsstuff;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

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

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideoViewHolder> {

    public interface OnCommentClickListener {
        void onCommentClick(ShortItem item);
    }

    private final List<ShortItem> videoList;
    private final OnCommentClickListener commentClickListener;
    private final String currentUserId;
    private final DatabaseReference databaseRef;

    public VideosAdapter(List<ShortItem> videoList, OnCommentClickListener commentClickListener) {
        this.videoList = videoList;
        this.commentClickListener = commentClickListener;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        this.databaseRef = FirebaseDatabase.getInstance().getReference("shorts");
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        ShortItem item = videoList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvVideoTitle.setText(item.getTitle() != null ? item.getTitle() : "Untitled Video");
        holder.tvVideoCreator.setText("@" + (item.getUsername() != null ? item.getUsername() : "User"));

        if (item.getVideoUrl() != null && !item.getVideoUrl().isEmpty()) {
            ExoPlayer player = new ExoPlayer.Builder(context).build();
            holder.videoPlayerView.setPlayer(player);
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(item.getVideoUrl()));
            player.setMediaItem(mediaItem);
            player.prepare();
        }

        holder.tvVideoLikeCount.setText(String.valueOf(item.getLikesCount()));
        holder.tvVideoCommentCount.setText(String.valueOf(item.getCommentsCount()));

        holder.tvVideoCreator.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("profile_user_id", item.getUserId());
            intent.putExtra("profile_username", item.getUsername());
            context.startActivity(intent);
        });

        holder.btnVideoLike.setOnClickListener(v -> {
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
                        holder.tvVideoLikeCount.setText(String.valueOf(newLikes));
                        holder.btnVideoLike.setImageResource(R.drawable.ic_favorite_border_24);
                    } else {
                        userLikeRef.setValue(true);
                        long newLikes = currentLikes + 1;
                        shortRef.child("likesCount").setValue(newLikes);
                        item.setLikesCount(newLikes);
                        holder.tvVideoLikeCount.setText(String.valueOf(newLikes));
                        holder.btnVideoLike.setImageResource(R.drawable.ic_favorite_24);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        });

        holder.btnVideoComment.setOnClickListener(v -> {
            if (commentClickListener != null) {
                commentClickListener.onCommentClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        PlayerView videoPlayerView;
        TextView tvVideoTitle;
        TextView tvVideoCreator;
        ImageButton btnVideoLike;
        TextView tvVideoLikeCount;
        ImageButton btnVideoComment;
        TextView tvVideoCommentCount;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoPlayerView = itemView.findViewById(R.id.videoPlayerView);
            tvVideoTitle = itemView.findViewById(R.id.tvVideoTitle);
            tvVideoCreator = itemView.findViewById(R.id.tvVideoCreator);
            btnVideoLike = itemView.findViewById(R.id.btnVideoLike);
            tvVideoLikeCount = itemView.findViewById(R.id.tvVideoLikeCount);
            btnVideoComment = itemView.findViewById(R.id.btnVideoComment);
            tvVideoCommentCount = itemView.findViewById(R.id.tvVideoCommentCount);
        }
    }
}