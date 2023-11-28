package com.tdtu.myapplication.model.user;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

import java.util.Date;




public class User implements Parcelable {
    private String id;
    private String name="User";
    private String email;
    private int age=0;
    private String phoneNumber="";
    private String role="";
    private String password="";
    private Date lastLogin=null;
    private String imageUrl=null;

    public User() {
    }

    private String status="Locked";

    public User(String id, String name, String email, int age, String phoneNumber, String status, String role, String password, Date lastLogin, String imageUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.role = role;
        this.password = password;
        this.lastLogin = lastLogin;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public User(FirebaseUser user){
        this.id = user.getUid();
        this.email = user.getEmail();
    }


    protected User(Parcel in) {
        id = in.readString();
        name = in.readString();
        email = in.readString();
        age = in.readInt();
        phoneNumber = in.readString();
        status = in.readString();
        role = in.readString();
        password = in.readString();
        imageUrl = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeInt(age);
        dest.writeString(phoneNumber);
        dest.writeString(status);
        dest.writeString(role);
        dest.writeString(password);
        dest.writeString(imageUrl);
    }
}
