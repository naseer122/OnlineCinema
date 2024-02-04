package com.example.onlinecinema;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private List<Messages> messageList;

    public ChatAdapter(List<Messages> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Messages message = messageList.get(position);
        holder.bindMessage(message);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView userNameTextView;
        private TextView messageTextView;
        private TextView timestampTextView; // Add this TextView


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.user_name);
            messageTextView = itemView.findViewById(R.id.message_text);
            timestampTextView = itemView.findViewById(R.id.timestamp_text);
        }

        public void bindMessage(Messages message) {
            userNameTextView.setText(message.getUserName()); // Replace with user's name logic
            messageTextView.setText(message.getText());
            timestampTextView.setText(new StringBuilder().append(message.getFormattedTimestamp()).append("\n").append(message.getDate()).toString());
        }
    }
}
