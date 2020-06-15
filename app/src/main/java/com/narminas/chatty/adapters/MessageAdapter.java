package com.narminas.chatty.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.narminas.chatty.R;
import com.narminas.chatty.models.Message;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private List<Message> messages;
    private String imageURL;

    public MessageAdapter(Context context, List<Message> messages, String imageURL) {
        this.context = context;
        this.messages = messages;
        this.imageURL = imageURL;
    }

    private FirebaseUser firebaseUser;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT ) {

            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {

            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Message chat = messages.get(position);
        DateFormat dateFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT, Locale.UK);
        String formattedTime = dateFormat.format(chat.getTimeAdded().toDate());
        holder.message_time.setText(formattedTime);
        holder.show_message.setText(chat.getMessage());
        if (imageURL.equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {

            Glide.with(context).load(imageURL).into(holder.profile_image);
        }

        if (position == messages.size() - 1) {
            if (chat.isIsseen()) {
                holder.seen_text_view.setText("Seen");
            } else {
                holder.seen_text_view.setText("Delivered");
            }
        } else {
            holder.seen_text_view.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (messages.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;

        } else {

            return MSG_TYPE_LEFT;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView show_message;
        public CircleImageView profile_image;
        public TextView seen_text_view;
        public TextView message_time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.chat_item_image);
            seen_text_view = itemView.findViewById(R.id.seen_text_view);
            message_time = itemView.findViewById(R.id.message_time);
        }
    }
}
