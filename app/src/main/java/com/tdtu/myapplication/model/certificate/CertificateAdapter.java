package com.tdtu.myapplication.model.certificate;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.tdtu.myapplication.R;
import com.tdtu.myapplication.model.student.Student;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CertificateAdapter extends FirestoreRecyclerAdapter<Certificate, CertificateAdapter.CertificateViewHolder> {
    private Context context;
    private Fragment fragment;
    private ArrayList<Certificate> certificates;
    private FirebaseFirestore db;
    private CollectionReference studentRef;
    private boolean isEditable = true;


    public CertificateAdapter(@NonNull FirestoreRecyclerOptions<Certificate> options, Context context) {
        super(options);
        this.context = context;
        db = FirebaseFirestore.getInstance();
        certificates = new ArrayList<>();
    }
    public CertificateAdapter(@NonNull FirestoreRecyclerOptions<Certificate> options, Context context,boolean isEditable) {
        super(options);
        this.context = context;
        db = FirebaseFirestore.getInstance();
        certificates = new ArrayList<>();
        this.isEditable = isEditable;
    }
    public CertificateAdapter(@NonNull FirestoreRecyclerOptions<Certificate> options, Fragment context) {
        super(options);
        this.fragment = context;
        db = FirebaseFirestore.getInstance();
        certificates = new ArrayList<>();
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
                context,
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





    @Override
    protected void onBindViewHolder(@NonNull CertificateAdapter.CertificateViewHolder holder, int position, @NonNull Certificate model) {
        holder.tvTitle.setText(model.getTitle());
        holder.tvStdId.setText(model.getStudentId());
        holder.tvIssueDate.setText(model.getIssueDate());
        holder.tvIssueOrg.setText(model.getIssuingOrganization());
        holder.tvCertDes.setText(model.getDescription());





        holder.update.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final DialogPlus dialogPlus = DialogPlus.newDialog(holder.tvTitle.getContext())
                        .setContentHolder(new ViewHolder(R.layout.activity_update_certificate))
                        .setExpanded(true, 1700)
                        .create();


                FirebaseFirestore db = FirebaseFirestore.getInstance();

                View view = dialogPlus.getHolderView();

                EditText title = view.findViewById(R.id.etCertTitle);
                EditText issueDate = view.findViewById(R.id.etCertIssueDate);
                EditText organization = view.findViewById(R.id.etCertIssueOrg);
                EditText description = view.findViewById(R.id.etCertDes);

                issueDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDatePickerDialog(issueDate);
                    }
                });
                Button btnUpdate = view.findViewById(R.id.btnCertUpdate);
//
//                int unit = ContextCompat.getColor(holder.tvTitle.getContext(), R.color.unit);
//                btnUpdate.setBackgroundColor(unit);

//                ID.setText(model.getID());
//                Name.setText(model.getName());
//                DoB.setText(model.getDoB());
//                Email.setText(model.getEmail());
//                phoneNumber.setText(model.getPhoneNumber());
                title.setText(model.getTitle());
                issueDate.setText(model.getIssueDate());
                organization.setText(model.getIssuingOrganization());
                description.setText(model.getDescription());

                dialogPlus.show();

                btnUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newTitle = title.getText().toString();
                        String newIssueDate = issueDate.getText().toString();
                        String newOrg = organization.getText().toString();
                        String newDesc = description.getText().toString();

                        issueDate.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showDatePickerDialog(issueDate);
                            }
                        });

                        // Tạo một đối tượng Student mới với thông tin đã được cập nhật
                        if ( newIssueDate.matches("\\d{2}/\\d{2}/\\d{4}")
                                ) {
                            Map<String, Object> updatedData = new HashMap<>();

                            updatedData.put("title", newTitle);
                            updatedData.put("issueDate", newIssueDate);
                            updatedData.put("issuingOrganization", newOrg);
                            updatedData.put("description", newDesc);


                            // Cập nhật thông tin vào Firestore
                            db.collection("certificates").document(getSnapshots().getSnapshot(holder.getAdapterPosition()).getId())
                                    .update(updatedData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(holder.tvTitle.getContext(), "Update Successful", Toast.LENGTH_SHORT).show();
                                            dialogPlus.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(holder.tvTitle.getContext(), "Update Fail", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }else {
                            Toast.makeText(holder.tvTitle.getContext(), "Please Input Correct Information", Toast.LENGTH_SHORT).show();
                        }
                    }

                });


            }
        });
    }

    @NonNull
    @Override
    public CertificateAdapter.CertificateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.certificate_item, parent, false);
        return new CertificateAdapter.CertificateViewHolder(view,isEditable);
    }

    // rest of the code...
    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class CertificateViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvStdId, tvIssueDate, tvIssueOrg, tvCertDes;
        ImageView update,detail;
        Button addCertBtn;
        ;
        public CertificateViewHolder(@NonNull View itemView,boolean isEditable) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvCertTitle);
            tvStdId = itemView.findViewById(R.id.tvCertStdId);
            tvIssueDate = itemView.findViewById(R.id.tvCertIssueDate);
            tvIssueOrg = itemView.findViewById(R.id.tvCertIssueOrg);
            tvCertDes = itemView.findViewById(R.id.tvCertDes);
            update = itemView.findViewById(R.id.ivCertAction);
            detail = itemView.findViewById(R.id.ivCertDetail);
            if (!isEditable){
                update.setVisibility(View.GONE);
                detail.setVisibility(View.GONE);
            }

        }
    }
}