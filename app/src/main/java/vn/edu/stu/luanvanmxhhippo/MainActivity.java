package vn.edu.stu.luanvanmxhhippo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import vn.edu.stu.Fragment.ActionFragment;
import vn.edu.stu.Fragment.GroupPostFragment;
import vn.edu.stu.Fragment.HomeFragment;
import vn.edu.stu.Fragment.InfoProfileFragment;
import vn.edu.stu.Fragment.SearchFragment;
import vn.edu.stu.Util.Constant;


public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Fragment selectedFragment = null;

    private boolean doubleBackToExitPressedOnce = false;

    private String user_token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        Bundle intent = getIntent().getExtras();
        /*if (intent != null) {
            String publisher = intent.getString("publisherid");

            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("profileid", publisher);
            editor.apply();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ProfileFragment()).commit();

        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }*/
        if (intent != null) {
            String fragmt = intent.getString("fragment_type");
            if (fragmt.equals("action")) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ActionFragment()).commit();
            }
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }

        //Toast token

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.nav_home:
                            selectedFragment = new HomeFragment();
                            break;

                        case R.id.nav_search:
                            selectedFragment = new SearchFragment();

                            break;
                        case R.id.nav_group:
                            selectedFragment = new GroupPostFragment();

                            break;
                        case R.id.nav_heart:
                            selectedFragment = new ActionFragment();

                            break;
                        case R.id.nav_profile:

                            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                            editor.putString("profileid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            editor.apply();
                            selectedFragment = new InfoProfileFragment();
                            break;
                    }
                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                selectedFragment).commit();
                    }
                    return true;
                }
            };

    @Override
    protected void onStart() {
        super.onStart();
        statusOnline();
    }

    @Override
    protected void onStop() {
        super.onStop();
        statusOffline();
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

    private void statusOnline() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_STATUS)
                .child(FirebaseAuth.getInstance().getUid());
        reference.child(Constant.STATUS_STATUS).setValue("true");
    }

    private void statusOffline() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_STATUS)
                .child(FirebaseAuth.getInstance().getUid());
        HashMap<String, Object> hashMapOff = new HashMap<>();
        hashMapOff.put(Constant.STATUS_STATUS, "false");
        hashMapOff.put(Constant.STATUS_TIMESTAMP, System.currentTimeMillis() + "");
        reference.updateChildren(hashMapOff)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {

                    }
                });
    }

}