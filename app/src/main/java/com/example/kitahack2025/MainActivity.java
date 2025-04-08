package com.example.kitahack2025;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "FirestoreExample";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this); // Optional but safe
        db = FirebaseFirestore.getInstance();

        Button btnAdd = findViewById(R.id.getStartedButton);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
            startActivity(intent);
        });
    }
}
