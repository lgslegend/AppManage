
package com.codeartist.applocker.activity;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by bjit-16 on 1/2/17.
 */

public class DummyActivity extends Activity {
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        finish();
        //this.finishAffinity();
    }
}
