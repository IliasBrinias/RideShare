package com.unipi.diplomaThesis.rideshare.Model;

import android.app.Activity;

import javax.inject.Singleton;

public class ActiveActivity {
    // Static variable reference of single_instance
    // of type Singleton
    private static ActiveActivity activity = null;

    // Declaring a variable of type String
    public Activity a;

    // Constructor
    // Here we will be creating private constructor
    // restricted to this class itself
    private ActiveActivity()
    {
        a = null;
    }

    // Static method
    // Static method to create instance of Singleton class
    public static Singleton getInstance()
    {
        if (activity == null)
            activity = new ActiveActivity();

        return (Singleton) activity;
    }
}
