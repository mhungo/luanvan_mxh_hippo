package vn.edu.stu.luanvanmxhhippo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class FogotPasswordActivity extends AppCompatActivity {

    private TextInputLayout text_fogot_inputlayout_email;
    private MaterialButton btnSend;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    //regex
    private String EMAIL_PATTERN = "^(.+)@(.+)$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fogot_password);

        addControls();
        addEvents();
    }

    private void addEvents() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = text_fogot_inputlayout_email.getEditText().getText().toString();
                if ((email.length() >= 12 && email.length() <= 40 && Pattern.matches(EMAIL_PATTERN, email))) {
                    sendEmailFogot(email);
                } else {
                    Toast.makeText(FogotPasswordActivity.this, "Invalid email ", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void sendEmailFogot(String email) {
        //progress
        progressDialog.setTitle("Sending email...");
        progressDialog.show();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            text_fogot_inputlayout_email.getEditText().setText("");
                            Toast.makeText(FogotPasswordActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(FogotPasswordActivity.this, "Failed...", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(FogotPasswordActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void addControls() {
        text_fogot_inputlayout_email = findViewById(R.id.text_fogot_inputlayout_email);
        btnSend = findViewById(R.id.btn_fogot_login);

        progressDialog = new ProgressDialog(FogotPasswordActivity.this);
    }
}