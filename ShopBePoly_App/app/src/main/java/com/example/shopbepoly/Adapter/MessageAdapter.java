package com.example.shopbepoly.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.DTO.Message;
import com.example.shopbepoly.R;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private Context context;
    private List<Message> messageList;
    private String currentUserId;
    private String UPLOADS_BASE = ApiClient.IMAGE_URL;

    public MessageAdapter(Context context, List<Message> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message.getFrom().getId().equals(currentUserId)) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView txtMessageSent;
        ImageView imgAvatarSent;
        ImageView imgStatusSent;
        TextView txtNameSent;

        SentMessageHolder(@NonNull View itemView) {
            super(itemView);
            txtMessageSent = itemView.findViewById(R.id.txtMessageSent);
            imgAvatarSent = itemView.findViewById(R.id.imgAvatarSent);
            imgStatusSent = itemView.findViewById(R.id.imgStatusSent);
            txtNameSent = itemView.findViewById(R.id.txtNameSent);
        }

        void bind(Message message) {
            txtNameSent.setText(message.getFrom().getName());
            txtMessageSent.setText(message.getContent());
            String avatarFileName = message.getFrom().getAvatar();
            String avatarUrl = UPLOADS_BASE + avatarFileName;

            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .transform(new CircleCrop())
                    .into(imgAvatarSent);

            if (message.getFrom().isOnline()) {
                imgStatusSent.setVisibility(View.VISIBLE);
            } else {
                imgStatusSent.setVisibility(View.GONE);
            }
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView txtMessageReceived;
        ImageView imgAvatarReceived;
        ImageView imgStatusReceived;
        TextView txtNameReceived;

        ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);
            txtMessageReceived = itemView.findViewById(R.id.txtMessageReceived);
            imgAvatarReceived = itemView.findViewById(R.id.imgAvatarReceived);
            imgStatusReceived = itemView.findViewById(R.id.imgStatusReceived);
            txtNameReceived = itemView.findViewById(R.id.txtNameReceived);
        }

        void bind(Message message) {
            txtNameReceived.setText(message.getFrom().getName());

            txtMessageReceived.setText(message.getContent());
            String avatarFileName = message.getFrom().getAvatar();
            String avatarUrl = UPLOADS_BASE + avatarFileName;

            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .transform(new CircleCrop())
                    .into(imgAvatarReceived);

            if (message.getFrom().isOnline()) {
                imgStatusReceived.setVisibility(View.VISIBLE);
            } else {
                imgStatusReceived.setVisibility(View.GONE);
            }
        }
    }
}