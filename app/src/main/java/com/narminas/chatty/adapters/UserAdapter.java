package com.narminas.chatty.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.narminas.chatty.MessageActivity;
import com.narminas.chatty.R;
import com.narminas.chatty.models.User;

import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>  {

    private Context context;
    private List<User> users;
    private boolean isChat;
    private Map<String, Integer> unreadMessages;
    private Map<String, String> chatLastMessages;

    public UserAdapter(Context context, List<User> users, boolean isChat, Map<String, Integer> unreadMessages, Map<String, String> chatLastMessages) {
        this.context = context;
        this.users = users;
        this.isChat = isChat;
        this.unreadMessages = unreadMessages;
        this.chatLastMessages = chatLastMessages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = users.get(position);
        String userName = user.getUsername().substring(0, 1).toUpperCase() + user.getUsername().substring(1);
        holder.usernameTextView.setText(userName);
        if (user.getImageURL().equals("default")) {
            holder.userProfileImage.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(context)
                    .load(user.getImageURL())
                    .into(holder.userProfileImage);
        }

        if (isChat) {
            boolean hasUnreadCount = unreadMessages.containsKey(user.getId());
            String lastMessage = chatLastMessages.get(user.getId());
            holder.lastMessage.setText(lastMessage);
            if (hasUnreadCount) {
                holder.unreadCount.setText(String.valueOf(unreadMessages.get(user.getId())));
                holder.unreadCount.setVisibility(View.VISIBLE);
            } else {
                holder.unreadCount.setVisibility(View.INVISIBLE);
            }
        }

        if (isChat) {
            //lastMessage(user.getId(), holder.lastMessage);
        } else {
            holder.lastMessage.setVisibility(View.GONE);
        }

        if (isChat) {
            if (user.getStatus().equals("online")) {
                holder.imgOn.setVisibility(View.VISIBLE);
                holder.imgOff.setVisibility(View.GONE);
            } else {
                holder.imgOn.setVisibility(View.GONE);
                holder.imgOff.setVisibility(View.VISIBLE);
            }
        } else {
            holder.imgOn.setVisibility(View.GONE);
            holder.imgOff.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("userId", user.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

//    private void lastMessage(final String userId, final TextView lastMessage) {
//        theLastMessage = "default";
//        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
//                    Chat chat = snapshot.getValue(Chat.class);
//                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId)
//                            || chat.getReceiver().equals(userId) && chat.getSender().equals(firebaseUser.getUid())) {
//                        theLastMessage = chat.getMessage();
//                    }
//                }
//
//                switch (theLastMessage) {
//                    case "default":
//                        lastMessage.setText("");
//                        break;
//                    default:
//                        lastMessage.setText(theLastMessage);
//                        break;
//                }
//
//                theLastMessage = "default";
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView usernameTextView;
        public CircleImageView userProfileImage;

        public CircleImageView imgOn;
        public CircleImageView imgOff;

        public TextView lastMessage;
        public TextView unreadCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userProfileImage = itemView.findViewById(R.id.user_profile_image);
            usernameTextView = itemView.findViewById(R.id.user_profile_name);
            imgOn = itemView.findViewById(R.id.img_on);
            imgOff = itemView.findViewById(R.id.img_off);
            lastMessage = itemView.findViewById(R.id.last_message);
            unreadCount = itemView.findViewById(R.id.unread_count);
        }
    }
}
