package com.fuxdevs.dropndispense.user;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.fuxdevs.dropndispense.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserMainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_main);

        // Initialize views
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set up navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                selectedFragment = new UserHomeFragment();
            } else if (itemId == R.id.navigation_parcels) {
                selectedFragment = new UserParcelsFragment();
            } else if (itemId == R.id.navigation_profile) {
                selectedFragment = new UserProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
                return true;
            }
            return false;
        });

        // Set initial fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new UserHomeFragment())
                .commit();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply only the top inset to the root so content isn't pushed up on devices
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);

            // Apply bottom inset as padding to the BottomNavigationView so its
            // contents (icons/labels) are positioned above the system navigation
            // / gesture area while the view itself stays anchored to the bottom.
            if (bottomNavigationView != null) {
                bottomNavigationView.setPadding(
                    bottomNavigationView.getPaddingLeft(),
                    bottomNavigationView.getPaddingTop(),
                    bottomNavigationView.getPaddingRight(),
                    systemBars.bottom
                );
            }

            return insets;
        });
    }
}