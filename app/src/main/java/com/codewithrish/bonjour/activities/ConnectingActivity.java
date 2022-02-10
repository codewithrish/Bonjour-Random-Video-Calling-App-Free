package com.codewithrish.bonjour.activities;

import static com.codewithrish.bonjour.utils.AppConstants.PROFILE;
import static com.codewithrish.bonjour.utils.AppConstants.ROOMS;
import static com.codewithrish.bonjour.utils.AppConstants.STATUS;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.codewithrish.bonjour.databinding.ActivityConnectingBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class ConnectingActivity extends AppCompatActivity {

    private ActivityConnectingBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConnectingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        String profile = getIntent().getStringExtra(PROFILE);
        Glide.with(this)
                .load(profile)
                .into(binding.imgProfile);

        String username = auth.getUid();
        db.collection(ROOMS)
                .whereLessThanOrEqualTo("status", 0)
                .orderBy("status")
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QuerySnapshot value = task.getResult();
                        if (value.size() > 0) {
                            Log.e("TAG", "onEvent: " + "hai room");
                            for (QueryDocumentSnapshot snapshot : value) {
                                Log.e("TAG", "onEvent: " + snapshot);
                                Map<String, Object> update = new HashMap<>();
                                update.put("incoming", username);
                                update.put("status", 1);
                                db.collection(ROOMS)
                                        .document(snapshot.getId())
                                        .update(update);
                                Intent intent = new Intent(ConnectingActivity.this, CallingActivity.class);
                                intent.putExtra("username", username);
                                intent.putExtra("incoming", snapshot.get("incoming").toString());
                                intent.putExtra("createdBy", snapshot.get("createdBy").toString());
                                intent.putExtra("isAvailable", (Boolean) snapshot.get("isAvailable"));
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            HashMap<String, Object> room = new HashMap<>();
                            room.put("incoming", username);
                            room.put("createdBy", username);
                            room.put("isAvailable", true);
                            room.put("status", 0);

                            db.collection(ROOMS).document(username).set(room).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    db.collection(ROOMS).document(username).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                            long checkStatus = (long) value.get("status");
                                            Log.e("TAG", "onEvent: " + "contain status" + checkStatus);
                                            if (checkStatus == 1) {
                                                Log.e("TAG", "onEvent: " + "1 bhi hai");
                                                Intent intent = new Intent(ConnectingActivity.this, CallingActivity.class);
                                                intent.putExtra("username", username);
                                                intent.putExtra("incoming", value.get("incoming").toString());
                                                intent.putExtra("createdBy", value.get("createdBy").toString());
                                                intent.putExtra("isAvailable", (Boolean) value.get("isAvailable"));
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
    }
}