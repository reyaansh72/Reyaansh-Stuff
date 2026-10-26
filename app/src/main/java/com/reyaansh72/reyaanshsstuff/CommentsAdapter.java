package com.reyaansh72.reyaanshsstuff;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private final List<CommentItem> commentList;

    public CommentsAdapter(List<CommentItem> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentItem item = commentList.get(position);

        String username = item.getUsername();
        if (username == null || username.trim().isEmpty()) {
            username = "Anonymous User";
        }
        holder.tvCommentUser.setText("@" + username);

        holder.tvCommentText.setText(item.getText());

        CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                item.getTimestamp(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        );
        holder.tvCommentTime.setText(relativeTime);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommentUser;
        TextView tvCommentTime;
        TextView tvCommentText;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommentUser = itemView.findViewById(R.id.tvCommentUser);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
        }
    }
}