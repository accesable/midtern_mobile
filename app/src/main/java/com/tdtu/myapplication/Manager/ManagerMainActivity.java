package com.tdtu.myapplication.Manager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.tdtu.myapplication.Admin.MainActivity;
import com.tdtu.myapplication.LoginActivity;
import com.tdtu.myapplication.R;
import com.tdtu.myapplication.RegisterActivity;
import com.tdtu.myapplication.StudentFragment;
import com.tdtu.myapplication.UpdateProfileFragment;
import com.tdtu.myapplication.UserFragment;
import com.tdtu.myapplication.model.SessionManager;
import com.tdtu.myapplication.model.student.Student;
import com.tdtu.myapplication.service.SearchProcessor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;
    private CollectionReference studentRef;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    SessionManager sessionManager;
    private boolean isSearching = false;
    private SearchProcessor searchProcessor;
    private UserFragment userFragment;

    StudentFragment studentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_main);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        Toolbar toolbar = findViewById(R.id.toolbar_manager);
        setSupportActionBar(toolbar);


        bottomNavigationView = findViewById(R.id.bottomNavigationView_manager);
        drawerLayout = findViewById(R.id.drawer_layout_manager);

        NavigationView navigationView = findViewById(R.id.nav_view_manager);
        navigationView.setNavigationItemSelectedListener(this);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open_nav,R.string.close_nav);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        if(savedInstanceState==null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_manager,new StudentFragment(db)).commit();
            navigationView.setCheckedItem(R.id.nav_student);
        }
        studentFragment = new StudentFragment(db);
        replaceFragment(studentFragment);
        searchProcessor = studentFragment;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();
            Toast.makeText(ManagerMainActivity.this,"Bottom menu CLicked "+item.getTitle(),Toast.LENGTH_SHORT).show();

            if (itemId == R.id.bottom_student_manager) {
                studentFragment = new StudentFragment(db);
                replaceFragment(studentFragment);
                searchProcessor = studentFragment;
            } else if (itemId == R.id.bottom_certificate_manager) {
//                replaceFragment(new LibraryFragment());
            }else if (itemId == R.id.bottom_add_manager) {
                showBottomDialog();
            }

            return true;
        });


        setUserProfile();
    }
    private  void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_manager, fragment);
        fragmentTransaction.commit();
    }
    private void setUserProfile(){
        sessionManager = new SessionManager(getApplicationContext());

// Find the NavigationView
        NavigationView navigationView = findViewById(R.id.nav_view_manager);

        // Get the header view at index 0
        View headerView = navigationView.getHeaderView(0);

        // Now find the TextViews inside the header view
        TextView emailView = headerView.findViewById(R.id.nav_header_email);
        TextView usernameView = headerView.findViewById(R.id.nav_header_username);
        ImageView imageViewProfile = headerView.findViewById(R.id.nav_header_imgViewProfile);



        // Set the text for the TextViews
        emailView.setText(sessionManager.getEmail());
        usernameView.setText(sessionManager.getUsername());
        Glide.with(this).load(sessionManager.getKeyImageUrl()).into(imageViewProfile);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Log.d("NavigationView", "Item selected: " + item.getTitle());
        if (itemId == R.id.nav_student) {
            StudentFragment studentFragment = new StudentFragment(db);
            replaceFragment(studentFragment);
            searchProcessor = studentFragment;
        } else if (itemId == R.id.nav_update) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_manager, new UpdateProfileFragment(db,mAuth)).commit();
        } else if (itemId == R.id.nav_logout) {
            sessionManager = null;
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ManagerMainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_option, menu);

        MenuItem item = menu.findItem(R.id.iSearch);
        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSearching = true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                isSearching = false;
                studentFragment.clearTextView(searchView);
                return false;
            }
        });

        // Tìm kiếm
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (searchProcessor != null) {
                    searchProcessor.processSearch(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchProcessor != null) {
                    searchProcessor.processSearch(newText);
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
    private void showBottomDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheetlayout);

        LinearLayout userLayout = dialog.findViewById(R.id.layoutUser);
        LinearLayout addStudentLayout = dialog.findViewById(R.id.layoutAddStudent);
        userLayout.setVisibility(View.GONE);
        LinearLayout importStudentFile = dialog.findViewById(R.id.layoutImportStudentFile);
        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);

        importStudentFile.setOnClickListener(v -> {
            this.openFilePicker();
        });

        LinearLayout exportStudentFile = dialog.findViewById(R.id.layoutExportStudentFile);
        exportStudentFile.setOnClickListener(v -> {
            this.getAllStudents();
        });


        userLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                Intent intent;
                intent = new Intent(ManagerMainActivity.this, RegisterActivity.class);
                startActivity(intent);
                Toast.makeText(ManagerMainActivity.this,"Create a Student is Clicked",Toast.LENGTH_SHORT).show();

            }
        });

        addStudentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                showAddStudentDialog();

            }
        });



        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }
    private void showAddStudentDialog() {
        // Tạo dialog
        final DialogPlus dialog = DialogPlus.newDialog(this)
                .setContentHolder(new ViewHolder(R.layout.activity_update_student))
                .setExpanded(true, 1700)
                .create();

        View view = dialog.getHolderView();

        EditText etId = view.findViewById(R.id.etId);
        EditText etName = view.findViewById(R.id.etName);
        EditText etDob = view.findViewById(R.id.etDob);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPhone = view.findViewById(R.id.etPhone);

        Button btnAdd = view.findViewById(R.id.btnUpdate);

        dialog.show();

        etDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(etDob);
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Id = etId.getText().toString();
                String Name = etName.getText().toString();
                String Dob = etDob.getText().toString();
                String Email = etEmail.getText().toString();
                String Phone = etPhone.getText().toString();

                if (Id.matches("\\d{8}") && !Name.isEmpty() && Dob.matches("\\d{2}/\\d{2}/\\d{4}")
                        && Email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}") && Phone.matches("\\d{10}")) {

                    Map<String, Object> user = new HashMap<>();
                    user.put("ID", Id);
                    user.put("Name", Name);
                    user.put("DoB", Dob);
                    user.put("Email", Email);
                    user.put("phoneNumber", Phone);

                    db.collection("students")
                            .add(user)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(ManagerMainActivity.this, "Add Successful", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss(); // Đóng dialog sau khi thêm thành công
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ManagerMainActivity.this, "Add Fail", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(ManagerMainActivity.this, "Please Input Correct Information", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                this,
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
    public void getAllStudents() {
        db.collection("students")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Student> studentList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Student student = document.toObject(Student.class);
                            studentList.add(student);
                        }
                        // Now you have a list of students
                        // You can pass this list to the method that exports to CSV
                        exportStudentDataToCSV(studentList,this);
                    } else {
                        Log.d("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }
    private void readFileContent(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                Log.w("MYLOG",line);
            }
            inputStream.close();
            reader.close();

            // Here you have the file content in stringBuilder
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception
        }
    }

    private  int PICK_FILE_REQUEST_CODE = 1;
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Set the desired file type (e.g., "text/csv" for CSV files)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a file"), PICK_FILE_REQUEST_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            replaceFragment(new StudentFragment(db));
            Uri fileUri = data.getData();
            try {
                Log.w("MYLOG", fileUri.toString());
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                List<Student> students = parseCSV(inputStream);
                students.forEach(student -> {
                    Map<String, Object> user = new HashMap<>();
                    user.put("ID", student.getID());
                    user.put("Name", student.getName());
                    user.put("DoB", student.getDoB());
                    user.put("Email", student.getEmail());
                    user.put("phoneNumber", student.getPhoneNumber());
                    db.collection("students").add(user);
                });
                Toast.makeText(this, "Add Student Complete.", Toast.LENGTH_SHORT).show();
                replaceFragment(new StudentFragment(db));
                // Now you have a list of students, do something with it
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                // Handle exception
            }
        }
    }
    private List<Student> parseCSV(InputStream inputStream) {
        List<Student> students = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord csvRecord : csvParser) {
                Log.w("STD","In Readding ");
                String ID = csvRecord.get("ID");
                String Name = csvRecord.get("Name");
                String DoB = csvRecord.get("DoB");
                String Email = csvRecord.get("Email");
                String phoneNumber = csvRecord.get("phoneNumber");

                Student student = new Student(ID, Name, DoB, Email, phoneNumber);
                students.add(student);
            }
        } catch (IOException e) {
            Log.w("STD","not Work  : "+e.getMessage());
            e.printStackTrace();
            // Handle exception
        }
        for (Student st : students){
            Log.w("STD",st.toString());
        }

        return students;
    }
    public void exportStudentDataToCSV(List<Student> students, Context context) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, "app_export_students.csv");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri = context.getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
        try {
            OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.append("ID,Name,DoB,Email,PhoneNumber\n");
            for (Student student : students) {
                writer.append(student.getID()).append(",");
                writer.append(student.getName()).append(",");
                writer.append(student.getDoB()).append(",");
                writer.append(student.getEmail()).append(",");
                writer.append(student.getPhoneNumber()).append("\n");
            }
            writer.flush();
            writer.close();
            // Notify user
            Toast.makeText(context, "Exported to Downloads folder", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}