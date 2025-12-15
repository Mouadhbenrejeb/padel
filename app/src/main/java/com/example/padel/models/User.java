package com.example.padel.models;

/**
 * Model class representing a User in Firebase
 */
public class User {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private double credits;

    // Default initial credits for new users (for testing)
    public static final double DEFAULT_INITIAL_CREDITS = 1000.0;

    // Required empty constructor for Firebase
    public User() {
    }

    public User(String id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.credits = DEFAULT_INITIAL_CREDITS;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public double getCredits() {
        return credits;
    }

    public void setCredits(double credits) {
        this.credits = credits;
    }
}
