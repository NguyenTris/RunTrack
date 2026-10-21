package com.example.runtrack;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Host Activity for HUD Settings and Peripherals fragments.
 */
public class HudSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hud_settings_host);
    }
}
