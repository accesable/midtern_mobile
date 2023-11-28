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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tdtu.myapplication.Admin.MainActivity;
import com.tdtu.myapplication.Employee.EmployeeMainActivity;
import com.tdtu.myapplication.Manager.ManagerMainActivity;
import com.tdtu.myapplication.model.SessionManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    EditText username;
    EditText password;
    Button loginButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = username.getText().toString();
                String pwd = password.getText().toString();
                if (email!=null || !email.isEmpty() || pwd!=null || !pwd.isEmpty()) {
                    validSignIn(email,pwd);
                } else {
                    Toast.makeText(LoginActivity.this, "Please fill all ", Toast.LENGTH_SHORT).show();
                    username.clearFocus();
                }
            }
        });
    }
    private void validSignIn(String email,String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success : ");
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            getUser(firebaseUser);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }
                    }
                });
    }
    private void getUser(FirebaseUser firebaseUser){
        String userId = firebaseUser.getUid(); // Get the unique user ID
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String username = document.getString("name"); // Assuming 'username' field exists
                        String email = document.getString("email");
                        String imageUrl = document.getString("imageUrl");
                        String role = document.getString("role");
                        String status = document.getString("status");
                        if (status.equals("Active")){
                            // Now you have the username, you can store it in a session manager or use it directly
                            SessionManager sessionManager = new SessionManager(getApplicationContext());
                            sessionManager.setUsername(username);
                            sessionManager.setEmail(email);
                            sessionManager.setKeyImageUrl(imageUrl);
                            sessionManager.setKeyRole(role);
                            updateUI(firebaseUser,role);
                        }else {
                            Toast.makeText(LoginActivity.this, "InActive Account please contact the administrator for allow access",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                }else {
                    Toast.makeText(LoginActivity.this, "No Account please contact the administrator for allow accessing",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void updateUI(FirebaseUser firebaseUser,String role) {

        if (firebaseUser != null) {
            // Prepare the data to update
            Map<String, Object> dataToUpdate = new HashMap<>();
            dataToUpdate.put("lastLogin", new Date());

            // Update Firestore
            db.collection("users").document(firebaseUser.getUid())
                    .update(dataToUpdate)
                    .addOnSuccessListener(aVoid -> {
                        // Firestore updated successfully, now start the new activity
                        Intent intent ;
                        if (role.equals("Admin")){
                            intent = new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(intent);
                        } else if (role.equals("Employee")) {
                            intent = new Intent(LoginActivity.this, EmployeeMainActivity.class);
                            startActivity(intent);
                        }else if (role.equals("Manager")) {
                            intent = new Intent(LoginActivity.this, ManagerMainActivity.class);
                            startActivity(intent);
                        }else {
                            Toast.makeText(LoginActivity.this,"No User Role",Toast.LENGTH_SHORT).show();
                        }

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(LoginActivity.this,"Database not respond Please try again later",Toast.LENGTH_SHORT).show();
                    });
        } else {
            // User is not logged in, handle this case
        }
    }

}