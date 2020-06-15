package com.narminas.chatty;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.narminas.chatty.adapters.MessageAdapter;
import com.narminas.chatty.models.Message;
import com.narminas.chatty.models.User;
import com.narminas.chatty.util.Status;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    private ImageButton attachButton;
    private CircleImageView chatUserImageView;
    private TextView chatUserNameTextView;
    private ImageButton sendMessageButton;
    private EditText messageEditText;

    private MessageAdapter messageAdapter;
    private List<Message> messages;
    private RecyclerView messageRecyclerView;
    LinearLayoutManager layoutManager;

    private FirebaseUser firebaseUser;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference messageReference = db.collection("Messages");
    private CollectionReference chatReference = db.collection("Chats");
    private DocumentReference messageUserReference;

    private String currentChatId;
    private String messageUserId;
    private User messageUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        messageUserId = getIntent().getExtras().getString("userId");
        messageUserReference = db.collection("Users").document(messageUserId);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        attachButton = findViewById(R.id.attach_button);
        chatUserImageView = findViewById(R.id.chat_user_image);
        chatUserNameTextView = findViewById(R.id.chat_user_username);
        sendMessageButton = findViewById(R.id.send_message_button);
        messageEditText = findViewById(R.id.send_message_edit_text);

        messageRecyclerView = findViewById(R.id.messages_recycler_view);
        messageRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        messageRecyclerView.setLayoutManager(layoutManager);

        messageUserReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                messageUser = documentSnapshot.toObject(User.class);
                chatUserNameTextView.setText(messageUser.getUsername());
                if (messageUser.getImageURL().equals("default")) {
                    chatUserImageView.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext())
                            .load(messageUser.getImageURL())
                            .into(chatUserImageView);
                }
                currentChatId = getChatId();//getCurrentChatId();
            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(message)) {
                    sendMessage(message);
                } else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message!", Toast.LENGTH_SHORT).show();
                }
                messageEditText.setText("");
            }
        });

    }

    private String getCurrentChatId() {
        Query firstQuery = chatReference
                .whereEqualTo("creatorUserId", firebaseUser.getUid())
                .whereEqualTo("replyUserId", messageUserId);
        firstQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    DocumentSnapshot snapshot = queryDocumentSnapshots.getDocuments().get(0);
                    currentChatId = snapshot.getId();
                } else {
                    Query reverseQuery = chatReference
                            .whereEqualTo("creatorUserId", messageUserId)
                            .whereEqualTo("replyUserId", firebaseUser.getUid());
                    reverseQuery.get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots2) {
                                    if (!queryDocumentSnapshots2.isEmpty()) {
                                        DocumentSnapshot snapshot1 = queryDocumentSnapshots2.getDocuments().get(0);
                                        currentChatId = snapshot1.getId();
                                    } else {
                                        currentChatId = null;
                                    }
                                }
                            });
                }
            }
        });
        return currentChatId;
    }

    private String getChatId() {
        Query firstQuery = chatReference
                .whereEqualTo("creatorUserId", firebaseUser.getUid())
                .whereEqualTo("replyUserId", messageUserId);
        firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                    DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                    currentChatId = document.getId();
                    getMessages(messageUser.getImageURL());
                } else {
                    Query reverseQuery = chatReference
                            .whereEqualTo("creatorUserId", messageUserId)
                            .whereEqualTo("replyUserId", firebaseUser.getUid());
                    reverseQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                                DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                                currentChatId = document.getId();
                                getMessages(messageUser.getImageURL());
                            }
                        }
                    });
                }
            }
        });
        return  currentChatId;
    }

    private String createChatWithReturnId() {
        Map<String, Object> chat = new HashMap<>();
        chat.put("creatorUserId", firebaseUser.getUid());
        chat.put("replyUserId", messageUserId);
        DocumentReference result = chatReference.add(chat).getResult();
        assert result != null;
        currentChatId = result.getId();
        return currentChatId;
    }

    private void sendMessage(String message) {
        if (currentChatId == null) {
            currentChatId = createChatWithReturnId();
        }
        Map<String, Object> objectHashMap = new HashMap<>();
        objectHashMap.put("chatId", currentChatId);
        objectHashMap.put("sender", firebaseUser.getUid());
        objectHashMap.put("receiver", messageUserId);
        objectHashMap.put("message", message);
        objectHashMap.put("isseen", false);
        objectHashMap.put("timeAdded", new Timestamp(new Date()));
        messageReference
                .add(objectHashMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        documentReference.get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        //Message messageAdded = documentSnapshot.toObject(Message.class);
                                        //messages.add(messageAdded);
                                        //messageAdapter.notifyItemInserted(messages.size() - 1);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getMessages(final String imageUrl) {
        String id = currentChatId;
        messages = new ArrayList<>();
        messageReference
                .whereEqualTo("chatId", currentChatId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(MessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            boolean isEmpty = queryDocumentSnapshots.isEmpty();
                            if (!queryDocumentSnapshots.isEmpty()) {
                                messages.clear();
                                for (DocumentSnapshot document : queryDocumentSnapshots) {
                                    Message message = document.toObject(Message.class);
                                    messages.add(message);
                                }
                                messages.sort(new Comparator<Message>() {
                                    @Override
                                    public int compare(Message message, Message t1) {
                                        return (int) (message.getTimeAdded().getSeconds() - t1.getTimeAdded().getSeconds());
                                    }
                                });
                                messageAdapter = new MessageAdapter(MessageActivity.this, messages, imageUrl);
                                messageRecyclerView.setAdapter(messageAdapter);
                                layoutManager.scrollToPosition(messages.size() - 1);
                                messageAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }


    private void status(String status) {
        db.collection("Users").document(firebaseUser.getUid()).update("status", status);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status(Status.ONLINE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        status(Status.OFFLINE);
    }
}
