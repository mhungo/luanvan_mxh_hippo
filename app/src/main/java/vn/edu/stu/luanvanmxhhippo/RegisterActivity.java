package vn.edu.stu.luanvanmxhhippo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private String current_user_id;
    private String user_token;

    private TextInputLayout txt_register_username, txt_register_fullname, txt_register_enterpassword,
            txt_register_email, txt_register_password;

    private TextView txt_register_already_account;

    private MaterialButton btn_register_create, btn_register_continue;
    private ProgressDialog progressDialog;

    private List<String> stringListUsername;


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
        checkUsername();
        addEvents();

    }

    private void addEvents() {
        //click button create
        btn_register_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                if (checkValidate() && checkEqualPassword()) {
                    //Toast.makeText(RegisterActivity.this, "OK", Toast.LENGTH_SHORT).show();

                    String username = txt_register_username.getEditText().getText().toString().trim();
                    String fullname = txt_register_fullname.getEditText().getText().toString().trim();
                    String email = txt_register_email.getEditText().getText().toString().trim();
                    String pass = txt_register_password.getEditText().getText().toString().trim();

                    //Show Dialog
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    if (stringListUsername.contains(username)) {
                        txt_register_username.setError("username exist");
                        Snackbar.make(btn_register_create, "username exist", Snackbar.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    } else {
                        //call method register
                        register_account(username, fullname, email, pass);
                    }


                } else {
                    Toast.makeText(RegisterActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_register_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = txt_register_email.getEditText().getText().toString().trim();
                String pass = txt_register_password.getEditText().getText().toString().trim();

                firebaseAuth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                                    } else {
                                        Snackbar.make(btn_register_continue, "Please check email verify", Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
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

    private void uploadUserToDb() {
        String register_username = txt_register_username.getEditText().getText().toString().trim();
        String register_fullname = txt_register_fullname.getEditText().getText().toString().trim();
        String register_email = txt_register_email.getEditText().getText().toString().trim();
        String pass = txt_register_password.getEditText().getText().toString().trim();

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
        new_user.put(Constant.USER_EMAIL, register_email);
        new_user.put(Constant.USER_USERNAME, register_username);
        new_user.put(Constant.USER_FULLNAME, register_fullname);
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
    }

    private void checkUsername() {
        stringListUsername.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    stringListUsername.add(user.getUser_username());
                }
                Log.i("BBB", "checkUsername: " + stringListUsername);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }


    private void register_account(String register_username, String register_fullname, String register_email, String register_password) {
        firebaseAuth.createUserWithEmailAndPassword(register_email, register_password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            uploadUserToDb();

                            /*//Sent verification link
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            firebaseUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    btn_register_create.setVisibility(View.GONE);
                                    btn_register_continue.setVisibility(View.VISIBLE);
                                    progressDialog.dismiss();
                                    FirebaseAuth.getInstance().signOut();
                                    Toast.makeText(RegisterActivity.this, "Email sent, please check mail", Toast.LENGTH_SHORT).show();
                                    *//*
                                    Intent intent = new Intent(RegisterActivity.this, VerificationEmailActivity.class);
                                    startActivity(intent);*//*

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Log.d("DDD", "onFailure: Email not sent" + e.getMessage());
                                }
                            });*/


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
        btn_register_continue = findViewById(R.id.btn_register_continue);

        firebaseAuth = FirebaseAuth.getInstance();
        stringListUsername = new ArrayList<>();

        progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setTitle("Create an account");
        progressDialog.setMessage("Please wait ...");

    }

}