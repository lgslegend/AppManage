
package com.codeartist.applocker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.codeartist.applocker.R;
import com.codeartist.applocker.utility.Constants;
import com.codeartist.applocker.utility.Preferences;

/**
 * Created by bjit-16 on 12/29/16.
 */

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.applock_activity_settings);
        getSupportActionBar().setTitle(getString(R.string.settings));

        TextView resetlockpsd = ((TextView) findViewById(R.id.resetlockpsd));
        resetlockpsd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(SettingsActivity.this, PatternSetterActivity.class));
                } catch (Exception e) {
                }
            }
        });


        int lockType = Preferences.loadInt(this, Constants.KEY_LOCKER_TYPE, Constants.PATTERN_LOCK);
        if (lockType == Constants.PATTERN_LOCK) {
            ((RadioButton) findViewById(R.id.radio_patternLock)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.radio_numberLock)).setChecked(true);
        }

        int accuracy = Preferences.loadInt(this, Constants.KEY_LOCKER_ACCURACY,
                Constants.ACCURACY_HIGH);
        if (accuracy == Constants.ACCURACY_VERY_HIGH) {
            ((RadioButton) findViewById(R.id.radio_very_high)).setChecked(true);
        } else if (accuracy == Constants.ACCURACY_HIGH) {
            ((RadioButton) findViewById(R.id.radio_high)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.radio_low)).setChecked(true);
        }
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        int lockType = Preferences.loadInt(this, Constants.KEY_LOCKER_TYPE, Constants.PATTERN_LOCK);

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_very_high:
                Log.e("pressed", "very high");
                if (checked)
                    // Pirates are the best
                    Preferences.save(this, Constants.KEY_LOCKER_ACCURACY,
                            Constants.ACCURACY_VERY_HIGH);
                break;
            case R.id.radio_high:
                if (checked)
                    // Ninjas rule
                    Preferences.save(this, Constants.KEY_LOCKER_ACCURACY, Constants.ACCURACY_HIGH);
                break;

            case R.id.radio_low:
                if (checked)
                    // Pirates are the best
                    Preferences.save(this, Constants.KEY_LOCKER_ACCURACY, Constants.ACCURACY_LOW);
                break;

            case R.id.radio_patternLock:

                if (lockType == Constants.NUMBER_LOCK) {
                    startActivityForResult(new Intent(this, PatternSetterActivity.class),
                            AppManagerActivity.REQUEST_CODE);
                    return;
                }
                if (checked)
                    // Pirates are the best
                    Preferences.save(this, Constants.KEY_LOCKER_TYPE, Constants.PATTERN_LOCK);
                break;

            case R.id.radio_numberLock:
                if (lockType == Constants.PATTERN_LOCK) {
                    startActivityForResult(new Intent(this, PasswordSetterActivity.class),
                            AppManagerActivity.REQUEST_CODE);
                }
                if (checked)
                    Preferences.save(this, Constants.KEY_LOCKER_TYPE, Constants.NUMBER_LOCK);
                // Pirates are the best
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int lockType = Preferences.loadInt(this, Constants.KEY_LOCKER_TYPE, Constants.PATTERN_LOCK);
        if (resultCode == Activity.RESULT_OK && requestCode == AppManagerActivity.REQUEST_CODE) {
            lockType = data.getIntExtra(Constants.KEY_LOCKER_TYPE, Constants.PATTERN_LOCK);
            Preferences.save(this, Constants.KEY_LOCKER_TYPE, lockType);
        }
        if (lockType == Constants.PATTERN_LOCK) {
            ((RadioButton) findViewById(R.id.radio_patternLock)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.radio_numberLock)).setChecked(true);
        }

    }
}
