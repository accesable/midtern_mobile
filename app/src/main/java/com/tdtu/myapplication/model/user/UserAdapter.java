package com.tdtu.myapplication.model.user;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.tdtu.myapplication.R;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UserAdapter extends FirestoreRecyclerAdapter<User, UserAdapter.UserViewHolder> {
    private Context context;
    private Fragment fragment;
    private ArrayList<User> studentArrayList;
    private FirebaseFirestore db;
    private CollectionReference studentRef;


    public UserAdapter(@NonNull FirestoreRecyclerOptions<User> options, Context context) {
        super(options);
        this.context = context;
        db = FirebaseFirestore.getInstance();
        studentArrayList = new ArrayList<>();
    }
    public UserAdapter(@NonNull FirestoreRecyclerOptions<User> options, Fragment context) {
        super(options);
        this.fragment = context;
        db = FirebaseFirestore.getInstance();
        studentArrayList = new ArrayList<>();
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

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    // rest of the code...
    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    @Override
    protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {
        holder.tvId.setText(model.getId());
        holder.tvName.setText(model.getName());
        holder.tvEmail.setText(model.getEmail());
        holder.tvPhone.setText(model.getPhoneNumber());
        holder.tvRoles.setText(model.getRole());
        holder.tvStatus.setText(model.getStatus());
        String lastLogin = "Not logined" ;
        if (model.getLastLogin() != null){
            lastLogin = model.getLastLogin().toString();
        }
        holder.tvLastLogin.setText(lastLogin);



        holder.update.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final DialogPlus dialogPlus = DialogPlus.newDialog(holder.tvId.getContext())
                        .setContentHolder(new ViewHolder(R.layout.activity_update_user))
                        .setExpanded(true, 1700)
                        .create();

                View view = dialogPlus.getHolderView();
                Button btnUpdate = view.findViewById(R.id.btnUserUpdate);
                RadioGroup radioGrpRoles = view.findViewById(R.id.idRadioGroup);
                RadioGroup radioGrpStatus = view.findViewById(R.id.idUserStatusRadioGroup);
                TextView status = view.findViewById(R.id.idTVUserStatus);
                TextView role = view.findViewById(R.id.idTVStatus);
                final boolean[] isChecked = {false};
                radioGrpRoles.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        // Get the selected Radio Button
                        RadioButton radioButton = group.findViewById(checkedId);
                        isChecked[0] = true;

                        // on below line we are setting text
                        // for our status text view.
                        role.setText(radioButton.getText());
                    }
                });
                radioGrpStatus.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        // Get the selected Radio Button
                        RadioButton radioButton = group.findViewById(checkedId);
                        isChecked[0] = true;

                        // on below line we are setting text
                        // for our status text view.
                        status.setText(radioButton.getText());
                    }
                });


                dialogPlus.show();
                btnUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!status.getText().toString().equals("Status")){
                            String newStatus = status.getText().toString();
                            model.setStatus(newStatus);
                        }
                        if(!role.getText().toString().equals("Role")){
                            String newRole = role.getText().toString();
                            model.setRole(newRole);
                        }

                            // Cập nhật thông tin vào Firestore
                            db.collection("users").document(getSnapshots().getSnapshot(holder.getAdapterPosition()).getId())
                                    .set(model)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(holder.tvId.getContext(), "Update Successful", Toast.LENGTH_SHORT).show();
                                            dialogPlus.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(holder.tvId.getContext(), "Update Fail", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                    }

                });


            }
        });

    }
    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView  tvId,tvName, tvRoles, tvEmail, tvPhone,tvLastLogin,tvStatus;
        ImageView update;
        ;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            tvId = itemView.findViewById(R.id.tvUserId);
            tvName = itemView.findViewById(R.id.tvUsername);
            tvRoles = itemView.findViewById(R.id.tvRoles);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvPhone = itemView.findViewById(R.id.tvUserPhone);
            update = itemView.findViewById(R.id.ivUserAction);
            tvStatus = itemView.findViewById(R.id.tvAccountStatus);
            tvLastLogin = itemView.findViewById(R.id.tvLastLogin);

        }
    }
}