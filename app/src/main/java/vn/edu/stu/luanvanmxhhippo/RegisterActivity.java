package vn.edu.stu.luanvanmxhhippo;

import android.app.ProgressDialog;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.regex.Pattern;

import vn.edu.stu.Util.Constant;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private String current_user_id;
    private String user_token;

    private TextInputLayout txt_register_username, txt_register_fullname, txt_register_enterpassword,
            txt_register_email, txt_register_password;

    private TextView txt_register_already_account;

    private MaterialButton btn_register_create;

    private ProgressDialog progressDialog;


    private String EMAIL_PATTERN = "^(.+)@(.+)$";
    private String USERNAME_PATTERN = "^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){3,18}[a-zA-Z0-9]$";
    private String PASS_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$";

    //Regex Pass: ^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$
    //Regex username: ^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){3,18}[a-zA-Z0-9]$
    //Regex email: ^(.+)@(.+)$

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        addControls();
        getToken();
        addEvents();

        Toast.makeText(this, user_token, Toast.LENGTH_SHORT).show();
    }

    private void addEvents() {
        //click button create
        btn_register_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                if (checkValidate() && checkEqualPassword()) {
                    Toast.makeText(RegisterActivity.this, "OK", Toast.LENGTH_SHORT).show();

                    String username = txt_register_username.getEditText().getText().toString();
                    String fullname = txt_register_fullname.getEditText().getText().toString();
                    String email = txt_register_email.getEditText().getText().toString();
                    String pass = txt_register_password.getEditText().getText().toString();

                    //Show Dialog
                    progressDialog.show();

                    //call method register
                    register_account(username, fullname, email, pass);

                } else {
                    Toast.makeText(RegisterActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //click text already account
        txt_register_already_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void register_account(String register_username, String register_fullname, String register_email, String register_password) {
        firebaseAuth.createUserWithEmailAndPassword(register_email, register_password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                        /*//Sent verification link
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(RegisterActivity.this, "Verification Email has been Sent", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("DDD", "onFailure: Email not sent" + e.getMessage());
                            }
                        });*/

                            current_user_id = FirebaseAuth.getInstance().getUid();
                            //Add to database status user
                            HashMap<String, String> status_User = new HashMap<>();
                            status_User.put(Constant.STATUS_USERID, current_user_id);
                            status_User.put(Constant.STATUS, Constant.DEFAULT_STATUS_OFFLINE);
                            status_User.put(Constant.STATUS_TIMESTAMP, System.currentTimeMillis() + "");

                            DatabaseReference referenceStatus = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_STATUS)
                                    .child(current_user_id);
                            referenceStatus.setValue(status_User);

                            //Add to database token user
                            HashMap<String, String> upload_token_user = new HashMap<>();
                            upload_token_user.put(Constant.TOKEN, user_token);
                            DatabaseReference referenceToken = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_TOKENS)
                                    .child(current_user_id);
                            referenceToken.setValue(upload_token_user);

                            //Add to database user info
                            HashMap<String, String> new_user = new HashMap<>();
                            new_user.put(Constant.ID, current_user_id);
                            new_user.put(Constant.EMAIL, register_email);
                            new_user.put(Constant.USERNAME, register_username);
                            new_user.put(Constant.FULLNAME, register_fullname);
                            new_user.put(Constant.GENDER, Constant.GENDER_DEFAULT);
                            new_user.put(Constant.BIRTHDAY, Constant.BIRTHDAY_DEFAULT);
                            new_user.put(Constant.IMAGEURL, Constant.IMAGE_PROFILE);
                            new_user.put(Constant.TOKEN, user_token);
                            new_user.put(Constant.BIO, Constant.BIO_DEFAULT);

                            DatabaseReference referenceUser = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS)
                                    .child(current_user_id);

                            referenceUser.setValue(new_user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        Toast.makeText(RegisterActivity.this, "Created Successfull", Toast.LENGTH_SHORT).show();
                                        cleanEdittext();

                                        //Go to login screen
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(RegisterActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull @NotNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(RegisterActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            //Stop dialog
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Unable to register account or email, password already exists, try again later", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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

    //Hide keyboard
    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    //CleanText
    private void cleanEdittext() {
        txt_register_username.getEditText().getText().clear();
        txt_register_fullname.getEditText().getText().clear();
        txt_register_email.getEditText().getText().clear();
        txt_register_password.getEditText().getText().clear();
        txt_register_enterpassword.getEditText().getText().clear();
    }

    //check null or empty
    private boolean checkValidate() {
        boolean kq;
        String username = txt_register_username.getEditText().getText().toString();
        String fullname = txt_register_fullname.getEditText().getText().toString();
        String email = txt_register_email.getEditText().getText().toString();
        String pass = txt_register_password.getEditText().getText().toString();
        String enterpass = txt_register_enterpassword.getEditText().getText().toString();

        if ((username.length() >= 6 && username.length() <= 20 && Pattern.matches(USERNAME_PATTERN, username))) {
            txt_register_username.setErrorEnabled(false);
        } else {
            txt_register_username.setError("Error");
        }
        if ((fullname.length() >= 6 && fullname.length() <= 30)) {
            txt_register_fullname.setErrorEnabled(false);
        } else {
            txt_register_fullname.setError("Error");
        }
        if ((email.length() >= 12 && email.length() <= 40 && Pattern.matches(EMAIL_PATTERN, email))) {
            txt_register_email.setErrorEnabled(false);
        } else {
            txt_register_email.setError("Error");
        }
        if ((pass.length() >= 8 && pass.length() <= 20 && Pattern.matches(PASS_PATTERN, pass))) {
            txt_register_password.setErrorEnabled(false);
        } else {
            txt_register_password.setError("Error");
        }
        if ((enterpass.length() >= 8 && enterpass.length() <= 20 && Pattern.matches(PASS_PATTERN, enterpass))) {
            txt_register_enterpassword.setErrorEnabled(false);
        } else {
            txt_register_enterpassword.setError("Error");
        }

        if ((username.length() >= 6 && username.length() <= 20 && Pattern.matches(USERNAME_PATTERN, username))
                && (fullname.length() >= 6 && fullname.length() <= 30)
                && (email.length() >= 12 && email.length() <= 40 && Pattern.matches(EMAIL_PATTERN, email))
                && (pass.length() >= 8 && pass.length() <= 20 && Pattern.matches(PASS_PATTERN, pass))
                && (enterpass.length() >= 8 && enterpass.length() <= 20 && Pattern.matches(PASS_PATTERN, enterpass))) {
            kq = true;
        } else {
            kq = false;
        }
        return kq;
    }

    //check equal pass and enterpass
    private boolean checkEqualPassword() {
        String pass = txt_register_password.getEditText().getText().toString();
        String enterpass = txt_register_enterpassword.getEditText().getText().toString();
        if (pass.equals(enterpass)) {
            txt_register_enterpassword.setErrorEnabled(false);
            return true;
        } else {
            txt_register_enterpassword.setError("Password is not the same ");
            return false;
        }
    }

    private void addControls() {
        txt_register_username = findViewById(R.id.text_register_inputlayout_username);
        txt_register_fullname = findViewById(R.id.text_register_inputlayout_fullname);
        txt_register_email = findViewById(R.id.text_register_inputlayout_email);
        txt_register_password = findViewById(R.id.text_register_inputlayout_password);
        txt_register_enterpassword = findViewById(R.id.text_register_inputlayout_enterpassword);

        txt_register_already_account = findViewById(R.id.text_register_already_account);

        btn_register_create = findViewById(R.id.btn_register_create);

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setTitle("Create an account");
        progressDialog.setMessage("Please wait ...");

    }

}