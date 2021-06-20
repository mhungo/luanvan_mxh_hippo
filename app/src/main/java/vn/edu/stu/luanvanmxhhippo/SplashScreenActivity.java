package vn.edu.stu.luanvanmxhhippo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreenActivity extends AppCompatActivity {

    private ImageView imgLogo;
    private TextView textFrom, txtMhungo;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        addControls();

        //check internet and load screen
        if (isOnline()) {
            load();
        } else {
            try {
                new AlertDialog.Builder(SplashScreenActivity.this)
                        .setTitle(R.string.txt_error)
                        .setMessage(R.string.txt_checkinternet)
                        .setCancelable(false)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                load();
                            }
                        }).show();
            } catch (Exception e) {

            }
        }
    }

    //add controls
    private void addControls() {
        imgLogo = findViewById(R.id.logoHippoInsta);
        textFrom = findViewById(R.id.textFrom);
        txtMhungo = findViewById(R.id.textMhungo);
    }

    //check connect internet
    private boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            return false;
        }
        return true;
    }

    //load screen
    private void load() {
        imgLogo = (ImageView) findViewById(R.id.logoHippoInsta);
        txtMhungo = (TextView) findViewById(R.id.textMhungo);
        textFrom = (TextView) findViewById(R.id.textFrom);

        imgLogo.animate().alpha(0f).setDuration(0);
        txtMhungo.animate().alpha(0f).setDuration(0);

        imgLogo.animate().alpha(1f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                txtMhungo.animate().alpha(1f).setDuration(800);

            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseAuth = FirebaseAuth.getInstance();
                if (firebaseAuth.getCurrentUser() != null) {
                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                   /* if (firebaseUser.isEmailVerified()) {

                    } else {
                        Intent intent = new Intent(SplashScreenActivity.this, VerificationEmailActivity.class);
                        startActivity(intent);
                        finish();*/

                } else {
                    Intent intent = new Intent(SplashScreenActivity.this, StartActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 2000);
    }

}