package vn.edu.stu.luanvanmxhhippo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import vn.edu.stu.Util.Constant;

public class OptionsActivity extends AppCompatActivity {

    private TextView logout, settings, view_post_saved, view_block_user, view_friend_list,
            view_following_list, view_stories_list;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        addControls();
        addEvents();

    }

    private void addEvents() {
        //click back
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //logout
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(OptionsActivity.this);
                builder.setTitle(getString(R.string.warning)).setMessage(R.string.you_want_logout);
                builder.setCancelable(true);

                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        updateToken();
                        updateTokenUser();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(OptionsActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                        finish();
                    }
                });

                builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();

            }
        });

        view_block_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OptionsActivity.this, FollowersActivity.class);
                intent.putExtra("id", "userblocked");
                intent.putExtra("title", "userblocked");
                startActivity(intent);
            }
        });

        view_post_saved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OptionsActivity.this, FollowersActivity.class);
                intent.putExtra("id", "postsaved");
                intent.putExtra("title", "postsaved");
                startActivity(intent);
            }
        });

        view_friend_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OptionsActivity.this, FollowersActivity.class);
                intent.putExtra("id", "friends");
                intent.putExtra("title", "friends");
                startActivity(intent);
            }
        });

        view_following_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OptionsActivity.this, FollowersActivity.class);
                intent.putExtra("id", FirebaseAuth.getInstance().getUid());
                intent.putExtra("title", "following");
                startActivity(intent);
            }
        });

        view_stories_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OptionsActivity.this, FollowersActivity.class);
                intent.putExtra("id", FirebaseAuth.getInstance().getUid());
                intent.putExtra("title", "stories");
                startActivity(intent);
            }
        });
    }

    private void updateTokenUser() {
        String userid = FirebaseAuth.getInstance().getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(Constant.USER_TOKEN, "");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(userid)
                .updateChildren(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(LoginActivity.this, "Update token successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void updateToken() {
        String userid = FirebaseAuth.getInstance().getUid();

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(Constant.TOKEN_TOKEN, "");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_TOKENS);
        reference.child(userid)
                .setValue(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                        }
                    }
                });

    }

    private void addControls() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.option);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        logout = findViewById(R.id.logout);
        settings = findViewById(R.id.settings);
        view_block_user = findViewById(R.id.view_block_user);
        view_post_saved = findViewById(R.id.view_post_saved);
        view_friend_list = findViewById(R.id.view_friend_list);
        view_following_list = findViewById(R.id.view_following_list);
        view_stories_list = findViewById(R.id.view_stories_list);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
    }

}