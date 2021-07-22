package vn.edu.stu.luanvanmxhhippo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import vn.edu.stu.Util.Constant;

public class VerificationEmailActivity extends AppCompatActivity {

    private MaterialButton btn_continue;
    private String fullname, username, email, password;
    private FirebaseAuth firebaseAuth;
    private String current_user_id;
    private String user_token;

    private ProgressDialog progressDialog;

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_email);


        addControls();
        getDataIntent();
        getToken();
        addEvents();

    }

    private void getDataIntent() {
        SharedPreferences prefs = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        username = prefs.getString(Constant.USER_USERNAME, "none");
        fullname = prefs.getString(Constant.USER_FULLNAME, "none");
        email = prefs.getString(Constant.USER_EMAIL, "none");
        password = prefs.getString(Constant.USER_PASS, "none");
    }

    private void addEvents() {
        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                                        registerAccount();
                                    } else {
                                        Snackbar.make(btn_continue, R.string.txt_please_check_email, Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
            }
        });
    }

    private void addControls() {
        btn_continue = findViewById(R.id.btn_continue);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    //Get Token device
    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<String> task) {
                if (task.isSuccessful()) {
                    String tokens = task.getResult();
                    user_token = tokens.toString();
                }
            }
        });
    }

    private void registerAccount() {
        progressDialog = new ProgressDialog(VerificationEmailActivity.this);
        progressDialog.setTitle(getString(R.string.txt_create_account));
        progressDialog.setMessage(getString(R.string.txt_please_wait));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        current_user_id = FirebaseAuth.getInstance().getUid();
        //Add to database status user
        HashMap<String, String> status_User = new HashMap<>();
        status_User.put(Constant.STATUS_USERID, current_user_id);
        status_User.put(Constant.STATUS_STATUS, Constant.DEFAULT_STATUS_OFFLINE);
        status_User.put(Constant.STATUS_TIMESTAMP, System.currentTimeMillis() + "");

        DatabaseReference referenceStatus = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_STATUS)
                .child(current_user_id);
        referenceStatus.setValue(status_User);

        //Add to database token user
        HashMap<String, String> upload_token_user = new HashMap<>();
        upload_token_user.put(Constant.TOKEN_TOKEN, user_token);
        DatabaseReference referenceToken = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_TOKENS)
                .child(current_user_id);
        referenceToken.setValue(upload_token_user);

        //Add to database user info
        HashMap<String, String> new_user = new HashMap<>();
        new_user.put(Constant.USER_ID, current_user_id);
        new_user.put(Constant.USER_EMAIL, email);
        new_user.put(Constant.USER_USERNAME, username);
        new_user.put(Constant.USER_FULLNAME, fullname);
        new_user.put(Constant.USER_ENABLE, Constant.DEFAULT_USER_ENABLE);
        new_user.put(Constant.USER_IMGBACKGROUND, Constant.IMAGE_PROFILE);
        new_user.put(Constant.USER_TIMESTAMP, System.currentTimeMillis() + "");
        new_user.put(Constant.USER_GENDER, Constant.GENDER_DEFAULT);
        new_user.put(Constant.USER_BIRTHDAY, Constant.BIRTHDAY_DEFAULT);
        new_user.put(Constant.USER_IMAGEURL, Constant.IMAGE_PROFILE);
        new_user.put(Constant.USER_TOKEN, user_token);
        new_user.put(Constant.USER_BIO, Constant.BIO_DEFAULT);

        DatabaseReference referenceUser = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS)
                .child(current_user_id);

        referenceUser.setValue(new_user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    firebaseAuth.signOut();
                    Toast.makeText(VerificationEmailActivity.this, R.string.txt_create_susccess, Toast.LENGTH_SHORT).show();

                    //Go to login screen
                    Intent intent = new Intent(VerificationEmailActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(VerificationEmailActivity.this, R.string.txt_error, Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(VerificationEmailActivity.this, R.string.txt_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            //statusOffline();
            super.onBackPressed();
            System.exit(0);
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.txt_please_back_again, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

}