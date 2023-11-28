package com.tdtu.myapplication;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tdtu.myapplication.Admin.MainActivity;
import com.tdtu.myapplication.model.user.User;

public class RegisterActivity extends AppCompatActivity {

    EditText username;

    Button registerButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    RadioGroup radioGrpRoles;

    TextView roleTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = findViewById(R.id.username);
        registerButton = findViewById(R.id.registerButton);
        radioGrpRoles = findViewById(R.id.idRadioGroup);
        roleTextView = findViewById(R.id.idTVStatus);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        final boolean[] isChecked = {false};
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = username.getText().toString();
                if (!email.isEmpty() && isChecked[0]) {
                    String pwd = "SysUser123";
                    validRegister(email,pwd);
                } else {
                    Toast.makeText(RegisterActivity.this, "Please fill all ", Toast.LENGTH_SHORT).show();
                    username.clearFocus();
                }
            }
        });

        radioGrpRoles.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Get the selected Radio Button
                RadioButton radioButton = group.findViewById(checkedId);
                isChecked[0] = true;

                // on below line we are setting text
                // for our status text view.
                roleTextView.setText(radioButton.getText());
            }
        });
    }

    private void validRegister(String email, String pwd) {
        mAuth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            insertUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }
                    }
                });
    }
    private void insertUser(){
        FirebaseUser user = mAuth.getCurrentUser();
        User userModel = new User(user);

        userModel.setRole(roleTextView.getText().toString());

        // Add a new document with a generated ID
        db.collection("users")
                .document(userModel.getId()).set(userModel)
                .addOnSuccessListener(command -> {
                    Log.d(TAG, "createUserWithEmail Inserted Into FireStore:success");
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding User", e);
                    }
                });
    }
}