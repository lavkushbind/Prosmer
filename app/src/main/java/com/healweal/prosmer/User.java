package com.healweal.prosmer;


import com.google.firebase.database.IgnoreExtraProperties;

// This annotation is useful if you add more fields to your local model
// that you don't want to save to the database.
@IgnoreExtraProperties
public class User {

    public String name;
    public String email;

    // A default, no-argument constructor is required for calls to
    // DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
}