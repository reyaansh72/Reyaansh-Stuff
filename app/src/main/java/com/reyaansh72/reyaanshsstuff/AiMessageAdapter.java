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

public class AiMessageAdapter extends RecyclerView.Adapter<AiMessageAdapter.AiMsgViewHolder> {

    private final List<AiMessageItem> messageList;

    public AiMessageAdapter(List<AiMessageItem> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public AiMsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ai_message, parent, false);
        return new AiMsgViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AiMsgViewHolder holder, int position) {
        AiMessageItem msg = messageList.get(position);
        holder.tvAiMessage.setText(msg.getText());

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.bubbleLayout.getLayoutParams();
        if (msg.isUser()) {
            params.gravity = Gravity.END;
        } else {
            params.gravity = Gravity.START;
        }
        holder.bubbleLayout.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class AiMsgViewHolder extends RecyclerView.ViewHolder {
        LinearLayout bubbleLayout;
        TextView tvAiMessage;

    public AiMsgViewHolder(@NonNull View itemView) {
            super(itemView);
            bubbleLayout = itemView.findViewById(R.id.aiBubbleLayout);
            tvAiMessage = itemView.findViewById(R.id.tvAiMessageText);
        }
    }
}