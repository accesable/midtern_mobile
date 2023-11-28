package com.tdtu.myapplication;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.tdtu.myapplication.model.SessionManager;
import com.tdtu.myapplication.model.student.Student;
import com.tdtu.myapplication.model.student.StudentAdapter;
import com.tdtu.myapplication.service.SearchProcessor;


import java.util.ArrayList;


public class StudentFragment extends Fragment implements SearchProcessor ,StudentAdapter.StudentClickListener {

    RecyclerView recyclerView;
    private CollectionReference studentRef;
    ArrayList<Student> studentArrayList;
    StudentAdapter studentAdapter;
    FirebaseFirestore db;
    View view;
    private boolean isSearching = false;
    SessionManager sessionManager;

    public StudentFragment() {
        // Required empty public constructor
    }
    public StudentFragment(FirebaseFirestore db) {
        // Required empty public constructor
        this.db = db;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_student, container, false);
        setHasOptionsMenu(true);

        studentArrayList = new ArrayList<>();
        studentRef = db.collection("students");

        recyclerView = view.findViewById(R.id.rvStudent);
        setUpRecyclerView();

        // Additional setup

        return view;
    }

    private void setUpRecyclerView() {
        sessionManager = new SessionManager(view.getContext());
        boolean isNotEmployee = !sessionManager.getKeyRole().equals("Employee");
        // Set up RecyclerView here, similar to what you did in StudentActivity
        Query query = studentRef.orderBy("ID", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Student> options = new FirestoreRecyclerOptions.Builder<Student>()
                .setQuery(query, Student.class)
                .build();

        if (isNotEmployee){
            studentAdapter = new StudentAdapter(options, view.getContext(),this);
        }else {
            studentAdapter = new StudentAdapter(options, view.getContext(),false,this);
        }


        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(studentAdapter);
        recyclerView.setHasFixedSize(true);
        studentAdapter.updateOptions(options);
        studentAdapter.startListening();



        if (isNotEmployee){
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();
                    String studentID = studentAdapter.getItem(position).getID();
                    String studentName = studentAdapter.getItem(position).getName();
                    showDeleteConfirmationDialog(position, studentName, studentID);
                }
            }).attachToRecyclerView(recyclerView);
        }

    }
    private void showDeleteConfirmationDialog ( final int position, String studentName, String studentID){
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setMessage("Do you want to delete " + studentName + " with ID " + studentID)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        studentAdapter.deleteItem(position);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        studentAdapter.notifyItemChanged(position);
                    }
                });
        builder.create().show();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.iSortIDAscending) {
            setUpRecyclerView();
        } else if (itemId == R.id.iSortIDDescending) {
            sortIdDescending();
        } else if (itemId == R.id.iSortNameAscending) {
            sortNameAscending();
        }
        return true;
    }



    private void sortNameAscending() {
        Query query = studentRef.orderBy("Name", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Student> options = new FirestoreRecyclerOptions.Builder<Student>()
                .setQuery(query, Student.class)
                .build();

        recyclerView = view.findViewById(R.id.rvStudent);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        studentAdapter.updateOptions(options);
        studentAdapter.startListening();
    }

    private void sortIdDescending() {
        Query query = studentRef.orderBy("ID", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Student> options = new FirestoreRecyclerOptions.Builder<Student>()
                .setQuery(query, Student.class)
                .build();
        recyclerView = view.findViewById(R.id.rvStudent);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        studentAdapter.updateOptions(options);
        studentAdapter.startListening();

    }


    @Override
    public void onStart() {
        super.onStart();
        if (studentAdapter != null) {
            studentAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (studentAdapter != null) {
            studentAdapter.stopListening();
        }
    }

    @Override
    public void processSearch(String keyword) {
        Query query = FirebaseFirestore.getInstance()
                .collection("students")
                .whereGreaterThanOrEqualTo("ID", keyword)
                .whereLessThanOrEqualTo("ID", keyword + "\uf8ff")
                .orderBy("ID");

        FirestoreRecyclerOptions<Student> options =
                new FirestoreRecyclerOptions.Builder<Student>()
                        .setQuery(query, Student.class)
                        .build();

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        studentAdapter.updateOptions(options);
        studentAdapter.startListening();
    }

    @Override
    public void clearTextView(SearchView searchView) {
        searchView.setQuery("", false);
        searchView.clearFocus();

        setUpRecyclerView();
    }

    @Override
    public void onStudentInfoClicked(String studentId) {
        Log.w("MYLOG","InterFace Is Called");
        showCertificatesFragment(new CertificateFragment(db,studentId));
    }
    private void showCertificatesFragment(CertificateFragment certificateFragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        String role = sessionManager.getKeyRole();

        if (role.equals("Admin")){
            fragmentTransaction.replace(R.id.fragment_container, certificateFragment);
        } else if (role.equals("Manager")) {
            fragmentTransaction.replace(R.id.fragment_container_manager, certificateFragment);
        } else if (role.equals("Employee")) {
            fragmentTransaction.replace(R.id.fragment_container_employee, certificateFragment);
        }

        fragmentTransaction.commit();
        // Perform the fragment transaction to replace the current fragment with CertificatesFragment
        // You may want to add the transaction to the back stack
    }

    // Additional methods as needed
}
