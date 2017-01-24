
package com.codeartist.applocker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.codeartist.applocker.R;
import com.codeartist.applocker.utility.Constants;
import com.codeartist.applocker.utility.Preferences;

/**
 * Created by bjit-16 on 11/23/16.
 */

public class PasswordSetterActivity extends AppCompatActivity {
    private String password1, password2;
    PinLockView mPinLockView;
    private static final String TAG = "pinnnnnn";
    private IndicatorDots mIndicatorDots;
    private TextView setPassword;
    String packageName;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
      /*  setContentView(R.layout.PatternView);
        mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);
        mPinLockView = (PinLockView) findViewById(R.id.pin_lock_view);
        setPassword = (TextView) findViewById(R.id.profile_name);
        mPinLockView.setPinLockListener(mPinLockListener);
        mPinLockView.attachIndicatorDots(mIndicatorDots);

        mPinLockView.setPinLength(6);
        mPinLockView.setTextColor(getResources().getColor(R.color.white));
        packageName = getIntent().getStringExtra(Constants.KEY_PKG_NAME);*/
        setContentView(R.layout.applock_activity_password_setter);
        final EditText password = (EditText) findViewById(R.id.editText_password);
        final TextView setPassword = (TextView) findViewById(R.id.textView_setPassword);
        Button check = (Button) findViewById(R.id.button_check);
        final String packageName = getIntent().getStringExtra(Constants.KEY_PKG_NAME);
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String temp = password.getEditableText().toString();
                if (temp == null) {
                    Toast.makeText(PasswordSetterActivity.this, "请先设置密码",
                            Toast.LENGTH_LONG).show();
                } else if (password1 == null) {
                    password1 = temp;
                    password.setText("");
                    setPassword.setText("确认密码");
                } else if (password2 == null) {
                    password2 = temp;
                    if (password1.equals(password2)) {
                        Preferences.save(getApplicationContext(),
                                Preferences.KEY_APP_LOCKER_PASSWORD, password1);
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(Constants.KEY_PKG_NAME, packageName);
                        resultIntent.putExtra(Constants.KEY_LOCKER_TYPE, 2);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    } else {
                        Toast.makeText(PasswordSetterActivity.this, "错误的密码",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private PinLockListener mPinLockListener = new PinLockListener() {
        @Override
        public void onComplete(String pin) {
            Log.d(TAG, "Pin complete: " + pin);
            String temp = pin;
            if (temp == null) {
                Toast.makeText(PasswordSetterActivity.this, "Please set the password.",
                        Toast.LENGTH_LONG).show();
            } else if (password1 == null) {
                password1 = temp;
                mPinLockView.resetPinLockView();
                setPassword.setText("确认密码");
            } else if (password2 == null) {
                password2 = temp;
                if (password1.equals(password2)) {
                    Preferences.save(getApplicationContext(),
                            Preferences.KEY_APP_LOCKER_PASSWORD, password1);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(Constants.KEY_PKG_NAME, packageName);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(PasswordSetterActivity.this, "错误的密码",
                            Toast.LENGTH_LONG).show();
                }
            }
        }


        @Override
        public void onEmpty() {
            Log.d(TAG, "Pin empty");
        }

        @Override
        public void onPinChange(int pinLength, String intermediatePin) {
            Log.d(TAG, "Pin changed, new length " + pinLength + " with intermediate pin " + intermediatePin);
        }
    };
}
