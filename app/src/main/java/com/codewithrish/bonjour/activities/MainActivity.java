package com.codewithrish.bonjour.activities;

import static com.codewithrish.bonjour.utils.AppConstants.PROFILE;
import static com.codewithrish.bonjour.utils.AppConstants.USERS;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codewithrish.bonjour.databinding.ActivityMainBinding;
import com.codewithrish.bonjour.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private long coins;
    private String profileUrl;

    private String[] permissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private int requestCode = 1;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.show();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        db.collection(USERS).document(auth.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                dialog.dismiss();
                User user = value.toObject(User.class);
                Glide.with(MainActivity.this)
                        .load(user.getProfile())
                        .into(binding.imgProfile);
                binding.txtCoinsBal.setText("You have: " + user.getCoins());
                coins = user.getCoins();
                profileUrl = user.getProfile();
            }
        });

        binding.btnFindFriends.setOnClickListener(v -> {
            if (isPermissionsGranted()) {
                if (coins > 5) {
                    coins = coins - 5;
                    db.collection(USERS)
                            .document(currentUser.getUid())
                            .update("coins", coins);
                    Intent intent = new Intent(MainActivity.this, ConnectingActivity.class);
                    intent.putExtra(PROFILE, profileUrl);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Insufficient Coins ...", Toast.LENGTH_SHORT).show();
                }
            } else {
                requestPermissions();
            }
        });
        binding.btnReward.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MoreCoinsActivity.class));
        });
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    private boolean isPermissionsGranted() {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}