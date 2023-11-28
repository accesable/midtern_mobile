package com.tdtu.myapplication;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.tdtu.myapplication.model.student.Student;
import com.tdtu.myapplication.model.user.User;
import com.tdtu.myapplication.model.user.UserAdapter;
import com.tdtu.myapplication.service.SearchProcessor;


import java.util.ArrayList;


public class UserFragment extends Fragment implements SearchProcessor  {

    RecyclerView recyclerView;
    private CollectionReference userRef;
    ArrayList<User> userArrayList;
    UserAdapter userAdapter;
    FirebaseFirestore db;
    View view;

    public UserFragment() {
        // Required empty public constructor
    }
    public UserFragment(FirebaseFirestore db) {
        // Required empty public constructor
        this.db = db;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_user, container, false);
        setHasOptionsMenu(true);

        userArrayList = new ArrayList<>();
        userRef = db.collection("users");

        recyclerView = view.findViewById(R.id.rvUser);
        setUpRecyclerView();

        // Additional setup

        return view;
    }
    private void setUpRecyclerView() {
        // Set up RecyclerView here, similar to what you did in StudentActivity
        Query query = userRef.orderBy("name", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        userAdapter = new UserAdapter(options, view.getContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(userAdapter);
        recyclerView.setHasFixedSize(true);
        userAdapter.updateOptions(options);
        userAdapter.startListening();


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                String studentID = userAdapter.getItem(position).getId();
                String studentName = userAdapter.getItem(position).getName();
                showDeleteConfirmationDialog(position, studentName, studentID);
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void showDeleteConfirmationDialog ( final int position, String studentName, String studentID){
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setMessage("Do you want to delete User " + studentName + " with ID " + studentID)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        userAdapter.deleteItem(position);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        userAdapter.notifyItemChanged(position);
                    }
                });
        builder.create().show();
    }
    @Override
    public void processSearch(String keyword) {
        Query query = FirebaseFirestore.getInstance()
                .collection("users")
                .orderBy("name")
                .startAt(keyword)
                .endAt(keyword + "\uf8ff");

        FirestoreRecyclerOptions<User> options =
                new FirestoreRecyclerOptions.Builder<User>()
                        .setQuery(query, User.class)
                        .build();

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        userAdapter.updateOptions(options);
        userAdapter.startListening();
    }

    @Override
    public void clearTextView(SearchView searchView) {
        searchView.setQuery("", false);
        searchView.clearFocus();

        setUpRecyclerView();
    }


}