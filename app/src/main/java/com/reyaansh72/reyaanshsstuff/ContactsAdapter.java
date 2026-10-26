package com.reyaansh72.reyaanshsstuff;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    public interface OnContactClickListener {
        void onContactClick(UserItem user);
    }

    private final List<UserItem> contactList;
    private final OnContactClickListener clickListener;

    public ContactsAdapter(List<UserItem> contactList, OnContactClickListener clickListener) {
        this.contactList = contactList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        UserItem user = contactList.get(position);
        holder.tvContactUsername.setText("@" + user.getUsername());
        holder.tvContactEmail.setText(user.getEmail() != null ? user.getEmail() : "Tap to chat");

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onContactClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvContactUsername;
        TextView tvContactEmail;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContactUsername = itemView.findViewById(R.id.tvContactUsername);
            tvContactEmail = itemView.findViewById(R.id.tvContactEmail);
        }
    }
}