package com.example.runtrack;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

/**
 * Host Activity for HUD Settings and Peripherals fragments.
 * Manages the Fragment backstack and container.
 */
public class HudSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hud_settings_host);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_fragment_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings_fragment_container, new SettingsHubMainFragment(), "SETTINGS_HUB_MAIN")
                    .commit();
        }
    }

    /**
     * Navigates to a destination Fragment and adds transaction to the backstack.
     */
    public void navigateToFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                )
                .replace(R.id.settings_fragment_container, fragment, tag)
                .addToBackStack(tag)
                .commit();
    }
}
