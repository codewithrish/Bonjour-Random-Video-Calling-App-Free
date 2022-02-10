package com.codewithrish.bonjour.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.codewithrish.bonjour.R;
import com.codewithrish.bonjour.databinding.ActivityMoreCoinsBinding;

public class MoreCoinsActivity extends AppCompatActivity {

    private ActivityMoreCoinsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMoreCoinsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}