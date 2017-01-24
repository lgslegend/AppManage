
package com.codeartist.applocker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.codeartist.applocker.R;
import com.codeartist.applocker.utility.Constants;
import com.codeartist.applocker.utility.Preferences;
import com.eftimoff.patternview.PatternView;

/**
 * Created by bjit-16 on 12/2/16.
 */

public class PatternSetterActivity extends AppCompatActivity {
    //private String pattern1, pattern2;
    private String patternString;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.applock_pattern_lock_option);
        final String packageName = getIntent().getStringExtra(Constants.KEY_PKG_NAME);
        final PatternView pattern = (PatternView) findViewById(R.id.patternView);
        pattern.setOnPatternDetectedListener(new PatternView.OnPatternDetectedListener() {
            @Override
            public void onPatternDetected() {
                String password = pattern.getPatternString();
                if (patternString == null) {
                    patternString = password;
                    pattern.clearPattern();
                    return;
                }

                if (patternString.equals(password)) {
                    //Toast.makeText(getApplicationContext(), "PATTERN CORRECT", Toast.LENGTH_SHORT).show();
                    Preferences.save(getApplicationContext(),
                            Preferences.KEY_APP_LOCKER_PASSWORD, patternString);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(Constants.KEY_PKG_NAME, packageName);
                    resultIntent.putExtra(Constants.KEY_LOCKER_TYPE, 1);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                    return;
                }
                Toast.makeText(getApplicationContext(), "两次不一致", Toast.LENGTH_SHORT).show();
//               patternView.clearPattern();
            }
        });
       /* final Lock9View lock9View = (Lock9View) findViewById(R.id.lock_9_view);
        lock9View.setCallBack(new Lock9View.CallBack() {
            @Override
            public void onFinish(String password) {
                if (patternString == null) {
                    patternString = password;
                    return;
                }
                if (patternString.equals(password)) {
                    //Toast.makeText(getApplicationContext(), "PATTERN CORRECT", Toast.LENGTH_SHORT).show();
                    Preferences.save(getApplicationContext(),
                            Preferences.KEY_APP_LOCKER_PASSWORD, patternString);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(Constants.KEY_PKG_NAME, packageName);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                    return;
                }
                Toast.makeText(getApplicationContext(), "PATTERN NOT CORRECT", Toast.LENGTH_SHORT).show();
//                patternView.clearPattern();
            }
        });*/
    }
}
