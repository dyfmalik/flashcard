package kr.co.bit.osf.flashcard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;


public class SplashActivity extends Activity{
    Handler h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        h = new Handler();
        h.postDelayed(mrun, 1000);
    }

    Runnable mrun = new Runnable() {
        @Override
        public void run() {
            Intent i = new Intent(SplashActivity.this, BoxListActivity.class);
            //Intent i = new Intent(SplashActivity.this, SignUpActivity.class);
            startActivity(i);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        }
    };


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        h.removeCallbacks(mrun);
    }
}
