package com.fuxdevs.dropndispense;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.fuxdevs.dropndispense.auth.LoginActivity;
import com.fuxdevs.dropndispense.user.ParcelViewActivity;
import com.fuxdevs.dropndispense.user.UserMainActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private FirebaseAuth mAuth;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force app to use light theme only
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize main thread handler
        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        ImageView splashLogo = findViewById(R.id.splashLogo);
        TextView appNameText = findViewById(R.id.appNameText);
        View loadingIndicator = findViewById(R.id.loadingIndicator);

        // Load and start animation
        Animation fadeScaleIn = AnimationUtils.loadAnimation(this, R.anim.fade_scale_in);
        splashLogo.startAnimation(fadeScaleIn);
        appNameText.startAnimation(fadeScaleIn);

        // Show loading indicator after animation
        loadingIndicator.setVisibility(View.INVISIBLE);
        mainHandler.postDelayed(() -> loadingIndicator.setVisibility(View.VISIBLE), 1000);

        // Navigate after splash delay
        mainHandler.postDelayed(() -> {
            // Check if activity was opened from a notification
            String parcelTrackNo = getIntent().getStringExtra("parcel_track_no");
            
            if (mAuth.getCurrentUser() != null && parcelTrackNo != null && !parcelTrackNo.isEmpty()) {
                // User is authenticated and notification has parcel tracking number
                Intent intent = new Intent(MainActivity.this, ParcelViewActivity.class);
                intent.putExtra("parcel_track_no", parcelTrackNo);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else if (mAuth.getCurrentUser() == null) {
                // User is not signed in, navigate to LoginActivity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                // User is signed in, navigate to UserMainActivity
                Intent intent = new Intent(MainActivity.this, UserMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            finish();
        }, SPLASH_DELAY);        // Initialize main thread handler
    }
}