package com.codewithrish.bonjour.activities;

import static com.codewithrish.bonjour.utils.AppConstants.ROOMS;
import static com.codewithrish.bonjour.utils.AppConstants.USERS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codewithrish.bonjour.R;
import com.codewithrish.bonjour.databinding.ActivityCallingBinding;
import com.codewithrish.bonjour.models.InterfaceJava;
import com.codewithrish.bonjour.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CallingActivity extends AppCompatActivity {

    private static final String TAG = "CallingActivity";

    private ActivityCallingBinding binding;
    private String uid;
    private String username, friendsUsername, createdBy;
    private boolean isPeerConnected = false;
    private FirebaseFirestore db;
    private boolean isAudio = true;
    private boolean isVideo = true;
    private boolean pageExit = false ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        username = getIntent().getStringExtra("username");
        String incoming = getIntent().getStringExtra("incoming");
        boolean isAvailable = getIntent().getBooleanExtra("isAvailable", false);
        createdBy = getIntent().getStringExtra("createdBy");

        Log.d(TAG, "onCreate: " + username + " " + incoming + " " + isAvailable + " " + createdBy);

//        friendsUsername = "";
//        if (incoming.equalsIgnoreCase(friendsUsername))
//            friendsUsername = incoming;
        friendsUsername = incoming;
        setupWebView();
        binding.btnMic.setOnClickListener(v -> {
            isAudio = !isAudio;
            callJavaScriptFunction("javascript:toggleAudio(\""+isAudio+"\")");
            if (isAudio) {
                binding.btnMic.setImageResource(R.drawable.btn_unmute_normal);
            } else {
                binding.btnMic.setImageResource(R.drawable.btn_mute_normal);
            }
        });
        binding.btnVideo.setOnClickListener(v -> {
            isVideo = !isVideo;
            callJavaScriptFunction("javascript:toggleVideo(\""+isVideo+"\")");
            if (isVideo) {
                binding.btnVideo.setImageResource(R.drawable.btn_video_normal);
            } else {
                binding.btnVideo.setImageResource(R.drawable.btn_video_muted);
            }
        });
        binding.btnDisconnectCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    void setupWebView() {
        binding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        binding.webView.addJavascriptInterface(new InterfaceJava(this), "Android");

        loadVideoCall();
    }

    private void loadVideoCall() {
        String filePath = "file:android_asset/call.html";
        binding.webView.loadUrl(filePath);

        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                initializePeer();
            }
        });
    }

    private void initializePeer() {
        uid = getUniqueId();
        callJavaScriptFunction("javascript:init(\""+uid+"\")");
        if (createdBy.equalsIgnoreCase(username)) {
            Log.d(TAG, "initializePeer: " + "if");
            if(pageExit)
                return;
            db.collection(USERS).document(username).update("connId", uid);
            db.collection(USERS).document(username).update("isAvailable", true);
            binding.groupLoading.setVisibility(View.GONE);
            binding.grpCallControls.setVisibility(View.VISIBLE);

            db.collection(USERS).document(friendsUsername).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    User user = task.getResult().toObject(User.class);
                    Glide.with(CallingActivity.this).load(user.getProfile())
                            .into(binding.imgProfile);
                    binding.txtName.setText(user.getName());
                    binding.txtCity.setText(user.getCity());
                }
            });
        } else {
            Log.d(TAG, "initializePeer: " + "else");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    friendsUsername = createdBy;
                    db.collection(USERS).document(friendsUsername).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            User user = task.getResult().toObject(User.class);
                            Glide.with(CallingActivity.this).load(user.getProfile())
                                    .into(binding.imgProfile);
                            binding.txtName.setText(user.getName());
                            binding.txtCity.setText(user.getCity());
                        }
                    });
                    db.collection(USERS).document(friendsUsername).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot snapshot = task.getResult();
                            if (snapshot.get("connId") != null) {
                                Log.d(TAG, "onComplete: " + snapshot.get("connId"));
                                sendCallRequest();
                            }
                        }
                    });
                }
            }, 5000);
        }
    }

    public void onPeerConnected() {
        isPeerConnected = true;
    }

    private void sendCallRequest() {
        if (!isPeerConnected) {
            Toast.makeText(this, "Check your internet connection", Toast.LENGTH_SHORT).show();
            return;
        }
        listenConnId();
    }

    private void listenConnId() {
        db.collection(USERS).document(friendsUsername).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot snapshot = task.getResult();
                if (snapshot.get("connId") == null) {
                    Log.d(TAG, "listenConnId: " + "connId null Hai");
                    return;
                }
                binding.groupLoading.setVisibility(View.GONE);
                binding.grpCallControls.setVisibility(View.VISIBLE);
                String connId = (String) snapshot.get("connId");
                callJavaScriptFunction("javascript:startCall(\""+connId+"\")");
            }
        });
    }

    private void callJavaScriptFunction(String function) {
        binding.webView.post(new Runnable() {
            @Override
            public void run() {
                binding.webView.evaluateJavascript(function, null);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pageExit = true;
        db.collection(USERS).document(createdBy).delete();
        finish();

    }

    String getUniqueId(){
        return UUID.randomUUID().toString();
    }
}