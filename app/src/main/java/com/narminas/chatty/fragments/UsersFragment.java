package com.narminas.chatty.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.narminas.chatty.R;
import com.narminas.chatty.adapters.UserAdapter;
import com.narminas.chatty.auth.UserApi;
import com.narminas.chatty.models.User;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> users;
    private EditText searchUsersEditText;
    private UserApi userApi = UserApi.getInstance();
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    private CollectionReference usersCollectionReference = FirebaseFirestore.getInstance().collection("Users");

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        searchUsersEditText = view.findViewById(R.id.search_users_edit_text);
        searchUsersEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUsers(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        recyclerView = view.findViewById(R.id.users_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        users = new ArrayList<>();
        usersCollectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                users.clear();
                if (searchUsersEditText.getText().toString().equals("") && queryDocumentSnapshots != null) {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        User user = document.toObject(User.class);
                        assert user != null;
                        if (!user.getId().equals(currentUser.getUid())) {
                            users.add(user);
                        }
                    }
                    userAdapter = new UserAdapter(getContext(), users, false, null, null);
                    recyclerView.setAdapter(userAdapter);
                }
            }
        });
        return view;
    }


    private void searchUsers(String strSearch) {
        usersCollectionReference
                .whereGreaterThanOrEqualTo("username", strSearch.toLowerCase())
                .whereLessThanOrEqualTo("username", strSearch.toLowerCase() + '\uf8ff')
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        users.clear();
                        if (task.isSuccessful()) {
                            QuerySnapshot taskResult = task.getResult();
                            assert taskResult != null;
                            List<User> list = taskResult.toObjects(User.class);
                            for (User user : list) {
                                if (!user.getId().equals(currentUser.getUid())) {
                                    users.add(user);
                                }
                            }
                            userAdapter = new UserAdapter(getContext(), users, false, null, null);
                            recyclerView.setAdapter(userAdapter);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        searchUsersEditText.clearFocus();
    }
}
