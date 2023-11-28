package com.tdtu.myapplication;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.tdtu.myapplication.Admin.MainActivity;
import com.tdtu.myapplication.model.SessionManager;
import com.tdtu.myapplication.model.certificate.Certificate;
import com.tdtu.myapplication.model.certificate.CertificateAdapter;
import com.tdtu.myapplication.model.student.Student;
import com.tdtu.myapplication.model.student.StudentAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CertificateFragment extends Fragment {

    RecyclerView recyclerView;
    private CollectionReference certRef;
    ArrayList<Certificate> certificates;
    CertificateAdapter certificateAdapter;
    FirebaseFirestore db;
    View view;
    private String studentID;
    Button addCert;

    public CertificateFragment(FirebaseFirestore db,String studentID) {
        // Required empty public constructor
        this.db = db;
        this.studentID = studentID;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_certificate, container, false);
        setHasOptionsMenu(true);

        certificates = new ArrayList<>();
        certRef = db.collection("certificates");

        recyclerView = view.findViewById(R.id.rvCertificate);
        TextView title = view.findViewById(R.id.rvStudentIDCert);
        title.setText("Student ID: "+this.studentID);
        addCert = view.findViewById(R.id.btnAddCert);

        setUpRecyclerView();
        SessionManager sessionManager = new SessionManager(view.getContext());
        boolean isNotEmployee = !sessionManager.getKeyRole().equals("Employee");
        if (isNotEmployee){
            addCert.setOnClickListener(v -> {
                Toast.makeText(getContext(),"Add Cert Clicked",Toast.LENGTH_SHORT).show();
                showAddStudentDialog();
            });
        }else {
            addCert.setVisibility(View.GONE);
        }

        // Additional setup


        return view;
    }

    private void setUpRecyclerView() {
        SessionManager sessionManager = new SessionManager(view.getContext());
        boolean isNotEmployee = !sessionManager.getKeyRole().equals("Employee");
        // Set up RecyclerView here, similar to what you did in StudentActivity
        Query query = certRef.whereEqualTo("studentId", this.studentID);
        FirestoreRecyclerOptions<Certificate> options = new FirestoreRecyclerOptions.Builder<Certificate>()
                .setQuery(query, Certificate.class)
                .build();

        if (isNotEmployee){
            certificateAdapter = new CertificateAdapter(options, view.getContext());
        }else {
            certificateAdapter = new CertificateAdapter(options, view.getContext(),false);
        }


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(certificateAdapter);
        recyclerView.setHasFixedSize(true);
        certificateAdapter.updateOptions(options);
        certificateAdapter.startListening();



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
                    String studentID = certificateAdapter.getItem(position).getCertificateId();
                    String studentName = certificateAdapter.getItem(position).getTitle();
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
                        certificateAdapter.deleteItem(position);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        certificateAdapter.notifyItemChanged(position);
                    }
                });
        builder.create().show();
    }

    private void showDatePickerDialog(EditText etDob) {
        Calendar currentDate = Calendar.getInstance();

        if (!etDob.getText().toString().isEmpty()) {
            String[] dobParts = etDob.getText().toString().split("/");
            int year = Integer.parseInt(dobParts[2]);
            int month = Integer.parseInt(dobParts[1]) - 1;
            int day = Integer.parseInt(dobParts[0]);

            currentDate.set(year, month, day);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        etDob.setText(selectedDate);
                    }
                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }
    private void showAddStudentDialog() {
        // Tạo dialog
        final DialogPlus dialog = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(R.layout.activity_add_certificate))
                .setExpanded(true, 1700)
                .create();

        View view = dialog.getHolderView();

        EditText title = view.findViewById(R.id.etAddCertTitle);
        EditText issueDate = view.findViewById(R.id.etAddCertIssueDate);
        EditText organization = view.findViewById(R.id.etAddCertIssueOrg);
        EditText description = view.findViewById(R.id.etAddCertDes);

        Button btnAdd = view.findViewById(R.id.btnCertAdd);
        String student_id = this.studentID;

        dialog.show();

        issueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(issueDate);
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String newTitle = title.getText().toString();
                String newIssueDate = issueDate.getText().toString();
                String newOrg = organization.getText().toString();
                String newDesc = description.getText().toString();

                if ( newIssueDate.matches("\\d{2}/\\d{2}/\\d{4}")
                ) {

                    Map<String, Object> updatedData = new HashMap<>();

                    updatedData.put("title", newTitle);
                    updatedData.put("issueDate", newIssueDate);
                    updatedData.put("issuingOrganization", newOrg);
                    updatedData.put("description", newDesc);
                    updatedData.put("studentId", student_id);

                    db.collection("certificates")
                            .add(updatedData)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(getContext(), "Add Successful", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss(); // Đóng dialog sau khi thêm thành công
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "Add Fail", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(getContext(), "Please Input Correct Information", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}