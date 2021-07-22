package vn.edu.stu.luanvanmxhhippo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import vn.edu.stu.Util.Constant;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout txt_login_email, txt_login_pass;
    private TextView txt_login_fogotpass, txt_login_notaccount;
    private MaterialButton btn_login_login;

    private CircularProgressIndicator circularProgressIndicator;

    private String tokens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        addControls();
        addEvents();

    }

    private void addEvents() {
        //Click login
        btn_login_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                showProgress();
                String email = txt_login_email.getEditText().getText().toString().trim();
                String pass = txt_login_pass.getEditText().getText().toString().trim();

                if (checkaccount() == true) {
                    login(email, pass);
                } else {
                    HideProgress();
                }
            }
        });

        //Click not account
        txt_login_notaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        //Click fogot pass
        txt_login_fogotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, FogotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }


    private boolean checkaccount() {
        boolean kq;
        String email = txt_login_email.getEditText().getText().toString();
        String pass = txt_login_pass.getEditText().getText().toString();

        //check empty email
        if (email.isEmpty()) {
            txt_login_email.setError(getString(R.string.txt_email_require));
        } else {
            txt_login_email.setErrorEnabled(false);
        }

        //check empty pass
        if (pass.isEmpty()) {
            txt_login_pass.setError(getString(R.string.txt_pass_require));
        } else {
            txt_login_pass.setErrorEnabled(false);
        }

        if (email.isEmpty() || pass.isEmpty()) {
            kq = false;
        } else {
            kq = true;
            //Disable error
            txt_login_email.setErrorEnabled(false);
            txt_login_pass.setErrorEnabled(false);
        }
        return kq;
    }

    private void login(String email, String pass) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            HideProgress();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                            updateToken();
                            updateTokenUser();
                        } else {
                            HideProgress();
                            Toast.makeText(LoginActivity.this, getString(R.string.txt_error_email_pass), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                HideProgress();
                Toast.makeText(LoginActivity.this, getString(R.string.txt_error_email_pass), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateTokenUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<String> task) {
                    if (task.isSuccessful()) {
                        tokens = task.getResult();
                        /*Toast.makeText(LoginActivity.this, tokens, Toast.LENGTH_SHORT).show();*/

                        String userid = firebaseUser.getUid();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS)
                                .child(userid);
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put(Constant.USER_TOKEN, tokens);
                        reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    //Toast.makeText(LoginActivity.this, "Update token successfully", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                }
            });
        }
    }

    private void updateToken() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser.getUid() != null) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<String> task) {
                    if (task.isSuccessful()) {
                        tokens = task.getResult();
                        /* Toast.makeText(LoginActivity.this, tokens, Toast.LENGTH_SHORT).show();*/

                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put(Constant.TOKEN_TOKEN, tokens);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_TOKENS)
                                .child(firebaseUser.getUid());
                        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                }
                            }
                        });
                    }
                }
            });

        }
    }

    private void showProgress() {
        circularProgressIndicator.setVisibility(View.VISIBLE);
    }

    private void HideProgress() {
        circularProgressIndicator.setVisibility(View.INVISIBLE);
    }

    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void addControls() {
        txt_login_email = findViewById(R.id.text_login_inputlayout_email);
        txt_login_pass = findViewById(R.id.text_login_inputlayout_password);
        txt_login_fogotpass = findViewById(R.id.txt_login_fogot_password);
        txt_login_notaccount = findViewById(R.id.txt_dont_have_account);

        btn_login_login = findViewById(R.id.btn_login_login);
        circularProgressIndicator = findViewById(R.id.progress_circular);
    }
}