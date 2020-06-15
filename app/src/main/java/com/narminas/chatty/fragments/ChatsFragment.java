package com.narminas.chatty.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1.StructuredQuery;
import com.narminas.chatty.R;
import com.narminas.chatty.adapters.UserAdapter;
import com.narminas.chatty.models.Chat;
import com.narminas.chatty.models.Message;
import com.narminas.chatty.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatsFragment extends Fragment {

    private RecyclerView chatsRecyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private List<Chat> chatLists;
    Map<String, Integer> chatUnreadMessages;
    Map<String, String> chatLastMessages;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference chatsReference = db.collection("Chats");
    private CollectionReference usersReference = db.collection("Users");
    private CollectionReference messageReference = db.collection("Messages");


    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        chatLists = new ArrayList<>();
        userList = new ArrayList<>();
        chatUnreadMessages = new HashMap<>();
        chatLastMessages = new HashMap<>();
        chatsRecyclerView = view.findViewById(R.id.chats_recycler_view);
        chatsRecyclerView.setHasFixedSize(true);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        unreadMessages();
        getChats();
        return view;
    }

    private void getChats() {
        chatLists = new ArrayList<>();
        chatsReference
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable final QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            chatLists.clear();
                            chatLastMessages.clear();
                            final int size = queryDocumentSnapshots.size();
                            for (final DocumentSnapshot document : queryDocumentSnapshots) {
                                Chat chat = document.toObject(Chat.class);
                                assert chat != null;
                                if (chat.getCreatorUserId().equals(firebaseUser.getUid()) || chat.getReplyUserId().equals(firebaseUser.getUid())) {
                                    chatLists.add(chat);
                                    messageReference.whereEqualTo("chatId", document.getId()).orderBy("timeAdded", Query.Direction.DESCENDING).limit(1)
                                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            Message lastMessage = task.getResult().toObjects(Message.class).get(0);
                                            if (lastMessage.getReceiver().equals(firebaseUser.getUid())) {
                                                chatLastMessages.put(lastMessage.getSender(), lastMessage.getMessage());
                                            } else if(lastMessage.getSender().equals(firebaseUser.getUid())) {
                                                chatLastMessages.put(lastMessage.getReceiver(), lastMessage.getMessage());
                                            }
                                            if (chatLastMessages.size() == chatLists.size()) {
                                                getChatUsers(chatLists);
                                            }
                                        }
                                    });
                                }
                            }
                            //getUsers(chatLists);
                            //getChatUsers(chatLists);
                        }
                    }
                });
    }

    private void getChatUsers(final List<Chat> allMyChats) {
        usersReference
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (queryDocumentSnapshots != null) {
                            userList.clear();
                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                User user = document.toObject(User.class);
                                assert user != null;
                                if (!user.getId().equals(firebaseUser.getUid())) {
                                    for (Chat chat : allMyChats) {
                                        if (user.getId().equals(chat.getCreatorUserId()) || user.getId().equals(chat.getReplyUserId())) {
                                            if (!userList.contains(user)) {
                                                userList.add(user);
                                            }
                                        }
                                    }
                                    userAdapter = new UserAdapter(getContext(), userList, true, chatUnreadMessages, chatLastMessages);
                                    chatsRecyclerView.setAdapter(userAdapter);
                                }
                            }
                        }
                    }
                });
    }

    private void getUsers(final List<Chat> allMyChats) {
        usersReference.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.isComplete()) {
                            QuerySnapshot taskResult = task.getResult();
                            userList.clear();
                            List<User> list = taskResult.toObjects(User.class);
                            for (User user : list) {
                                if (!user.getId().equals(firebaseUser.getUid())) {
                                    for (Chat chat : allMyChats) {
                                        if (user.getId().equals(chat.getCreatorUserId()) || user.getId().equals(chat.getReplyUserId())) {
                                            if (!userList.contains(user)) {
                                                userList.add(user);
                                            }
                                        }
                                    }
                                    userAdapter = new UserAdapter(getContext(), userList, true, chatUnreadMessages, chatLastMessages);
                                    chatsRecyclerView.setAdapter(userAdapter);
                                }
                            }
                        }
                    }
                });
    }

    private void unreadMessages() {
        messageReference
                .whereEqualTo("isseen", false)
                .whereEqualTo("receiver", firebaseUser.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        chatUnreadMessages.clear();
                        if (e != null) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            int totalUnread = queryDocumentSnapshots.size();
                            if (totalUnread > 0) {
                                for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                    String sender = snapshot.getString("sender");
                                    if (sender != null) {
                                        if (!chatUnreadMessages.containsKey(sender)) {
                                            chatUnreadMessages.put(sender, 1);
                                        } else {
                                            int oldValue = Integer.parseInt(chatUnreadMessages.get(sender).toString());
                                            int newValue = oldValue + 1;
                                            chatUnreadMessages.put(sender, newValue);
                                        }
                                    }
                                }

                            }
                        }
                    }
                });
    }
}
