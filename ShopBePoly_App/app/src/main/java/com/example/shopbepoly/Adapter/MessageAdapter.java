package com.example.shopbepoly.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.Chat;
import com.example.shopbepoly.DTO.Message;
import com.example.shopbepoly.R;

import java.util.List;
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private List<Message> messages;
    private String currentUserId;

    public MessageAdapter(Chat chat, List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return message.getFrom().equals(currentUserId) ? TYPE_SENT : TYPE_RECEIVED;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).txtMessage.setText(message.getContent());
        } else {
            ((ReceivedViewHolder) holder).txtMessage.setText(message.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;
        public SentViewHolder(View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessageSent);
        }
    }

    public static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;
        public ReceivedViewHolder(View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessageReceived);
        }
    }
}
