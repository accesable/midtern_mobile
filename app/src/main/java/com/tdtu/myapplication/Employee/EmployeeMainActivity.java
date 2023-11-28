package com.tdtu.myapplication.Employee;

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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tdtu.myapplication.Admin.MainActivity;
import com.tdtu.myapplication.LoginActivity;
import com.tdtu.myapplication.R;
import com.tdtu.myapplication.StudentFragment;
import com.tdtu.myapplication.UpdateProfileFragment;
import com.tdtu.myapplication.UserFragment;
import com.tdtu.myapplication.model.SessionManager;
import com.tdtu.myapplication.service.SearchProcessor;

public class EmployeeMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    StudentFragment studentFragment;
    DrawerLayout drawerLayout;

    private CollectionReference studentRef;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    SessionManager sessionManager;
    private boolean isSearching = false;
    private SearchProcessor searchProcessor;
    private UserFragment userFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_main);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        Toolbar toolbar = findViewById(R.id.toolbar_employee);
        setSupportActionBar(toolbar);



        drawerLayout = findViewById(R.id.drawer_layout_employee);

        NavigationView navigationView = findViewById(R.id.nav_view_employee);
        navigationView.setNavigationItemSelectedListener(this);




        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open_nav,R.string.close_nav);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        if(savedInstanceState==null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_employee,new StudentFragment(db)).commit();
            navigationView.setCheckedItem(R.id.nav_student);
        }
        studentFragment = new StudentFragment(db);
        replaceFragment(studentFragment);
        searchProcessor = studentFragment;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        setUserProfile();
    }
    private  void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_employee, fragment);
        fragmentTransaction.commit();
    }
    private void setUserProfile(){
        sessionManager = new SessionManager(getApplicationContext());

// Find the NavigationView
        NavigationView navigationView = findViewById(R.id.nav_view_employee);

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
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_employee, new UpdateProfileFragment(db,mAuth)).commit();
        } else if (itemId == R.id.nav_logout) {
            sessionManager = null;
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(EmployeeMainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_option_employee, menu);

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

}