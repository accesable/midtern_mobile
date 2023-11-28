package com.tdtu.myapplication;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tdtu.myapplication.service.SimplePasswordHasher;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class UpdateProfileFragment extends Fragment {


    EditText username,password,age,phoneNumber;
    TextView tvUsername,tvEmail,tvAge,tvPhoneNumber;
    Button uploadProfilePic,update;
    FirebaseFirestore db;

    // view for image view
    private ImageView imageView;

    // Uri indicates, where the image will be picked from
    private Uri filePath;
    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;

    FirebaseAuth firebaseAuth;
    private final int PICK_IMAGE_REQUEST = 22;
    public UpdateProfileFragment(FirebaseFirestore db, FirebaseAuth firebaseAuth) {
        // Required empty public constructor
        this.db = db;
        this.firebaseAuth = firebaseAuth;

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_update_profile, container, false);
        username = view.findViewById(R.id.updateUsername);
        password = view.findViewById(R.id.updatePassword);
        age = view.findViewById(R.id.updateAge);
        phoneNumber = view.findViewById(R.id.updatePhoneNumber);

        tvAge = view.findViewById(R.id.tv_update_age);
        tvEmail = view.findViewById(R.id.tv_update_email);
        tvUsername = view.findViewById(R.id.tv_update_username);
        tvPhoneNumber = view.findViewById(R.id.tv_update_phone);
        imageView = view.findViewById(R.id.imgView);

        update = view.findViewById(R.id.updateBtn);
        uploadProfilePic = view.findViewById(R.id.updateProfilePic);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        setTextView();

        uploadProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SelectImage();
            }
        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpdate();
            }
        });
        return view;
    }
    // Select Image method
    private void SelectImage()
    {

        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }
    private void setTextView(){
        String userId = firebaseAuth.getCurrentUser().getUid(); // Get the unique user ID
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String username = document.getString("name"); // Assuming 'username' field exists
                        String email = document.getString("email");
                        int age = document.get("age",Integer.class);
                        String phoneNumber = document.getString("phoneNumber");
                        tvAge.setText("Age: "+age);
                        tvEmail.setText("Email: "+email);
                        tvPhoneNumber.setText("Phone Number: "+phoneNumber);
                        tvUsername.setText("Username: "+username);
                        // Now you have the username, you can store it in a session manager or use it directly

                    }
                }
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void setUpdate() {
        if (filePath != null) {
            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("profileImages/" + firebaseAuth.getCurrentUser().getUid());

            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageURL = uri.toString();
                                    // Store this URL in Firestore or your preferred database
                                    saveImageUrlToDatabase(imageURL);
                                }
                            });
                            Toast.makeText(getActivity(), "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int)progress + "%");
                        }
                    });
        }else {
            saveImageUrlToDatabase(null);
        }

//        Map<String, Object> updates = new HashMap<>();

    }
    private void saveImageUrlToDatabase(String imageUrl) {
        String userId = firebaseAuth.getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();
        if (imageUrl!=null){
            Log.w("MYTAG",imageUrl);
            updates.put("imageUrl", imageUrl);
        }
        if (username.getText()!= null && !username.getText().toString().isEmpty()) {
            Log.w("MYTAG",username.getText().toString());
            updates.put("name", username.getText().toString());
        }
        if (password.getText()!= null && !password.getText().toString().isEmpty()) {
            Log.w("MYTAG",password.getText().toString());
            firebaseAuth.getCurrentUser().updatePassword(password.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User password updated.");
                        updates.put("password", SimplePasswordHasher.hashPassword(password.getText().toString(),SimplePasswordHasher.generateSalt()));
                    }else {
                        Toast.makeText(getActivity(), "Invalid password input", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        if (age.getText()!= null&& !age.getText().toString().isEmpty()) {
            Log.w("MYTAG",age.getText().toString());
            updates.put("age", Integer.parseInt(age.getText().toString()));
        }
        if (phoneNumber.getText()!= null&& !phoneNumber.getText().toString().isEmpty()) {
            Log.w("MYTAG",phoneNumber.getText().toString());
            updates.put("phoneNumber", phoneNumber.getText().toString());
        }

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "Update Success", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Update Failure", Toast.LENGTH_SHORT).show());
    }


}