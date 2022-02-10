package com.codewithrish.bonjour.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.codewithrish.bonjour.R;
import com.google.firebase.auth.FirebaseAuth;

public class IntroActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            goToLoginActivity();
        }

        findViewById(R.id.btn_lets_go).setOnClickListener(v -> {
            goToLoginActivity();
        });
    }

    private void goToLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}