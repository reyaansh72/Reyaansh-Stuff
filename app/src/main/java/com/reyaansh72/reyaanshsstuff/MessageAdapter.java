package com.reyaansh72.reyaanshsstuff;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    public interface OnMessageClickListener {
        void onMessageClick(MessageItem message);
    }

    private final List<MessageItem> messageList;
    private final String currentUserId;
    private final OnMessageClickListener clickListener;

    public MessageAdapter(List<MessageItem> messageList, String currentUserId, OnMessageClickListener clickListener) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageItem msg = messageList.get(position);
        boolean isMe = msg.getSenderId() != null && msg.getSenderId().equals(currentUserId);

        // Alignment
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageBubbleLayout.getLayoutParams();
        if (isMe) {
            params.gravity = Gravity.END;
        } else {
            params.gravity = Gravity.START;
        }
        holder.messageBubbleLayout.setLayoutParams(params);

        // Text
        holder.tvMessageText.setText(msg.getText());

        // Reply banner
        if (msg.getReplyToText() != null && !msg.getReplyToText().isEmpty()) {
            holder.tvReplyBanner.setVisibility(View.VISIBLE);
            holder.tvReplyBanner.setText("Replying to: " + msg.getReplyToText());
        } else {
            holder.tvReplyBanner.setVisibility(View.GONE);
        }

        // Media
        if (msg.getMediaUrl() != null && !msg.getMediaUrl().isEmpty()) {
            holder.tvMediaIndicator.setVisibility(View.VISIBLE);
            holder.tvMediaIndicator.setText("📎 [" + msg.getMediaType().toUpperCase() + "] " + msg.getMediaUrl());
        } else {
            holder.tvMediaIndicator.setVisibility(View.GONE);
        }

        // Reactions
        if (msg.getReactions() != null && !msg.getReactions().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : msg.getReactions().entrySet()) {
                sb.append(entry.getValue()).append(" ");
            }
            holder.tvReactions.setVisibility(View.VISIBLE);
            holder.tvReactions.setText(sb.toString().trim());
        } else {
            holder.tvReactions.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onMessageClick(msg);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout messageBubbleLayout;
        TextView tvReplyBanner;
        TextView tvMessageText;
        TextView tvMediaIndicator;
        TextView tvReactions;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageBubbleLayout = itemView.findViewById(R.id.messageBubbleLayout);
            tvReplyBanner = itemView.findViewById(R.id.tvReplyBanner);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvMediaIndicator = itemView.findViewById(R.id.tvMediaIndicator);
            tvReactions = itemView.findViewById(R.id.tvReactions);
        }
    }
}