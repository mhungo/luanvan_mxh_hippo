package vn.edu.stu.luanvanmxhhippo;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import vn.edu.stu.Adapter.ParticipantAdapter;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;

public class GroupParticipantAddActivity extends AppCompatActivity {

    //init view

    private Toolbar toolbar;

    private RecyclerView usersRv;

    private FirebaseAuth firebaseAuth;

    private String groupId;

    private String myGroupRole = "";

    private ArrayList<User> userList;
    private ParticipantAdapter participantAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_participant_add);

        addControls();
        getDataIntent();
        addEvents();
        loadGroupInfo();
    }

    private void addEvents() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadAllUsers() {
        //init list
        userList = new ArrayList<>();
        //load users from database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User modelUser = ds.getValue(User.class);

                    //get all user accept currently signed in
                    if (!firebaseAuth.getUid().equals(modelUser.getId())) {
                        //not my uid
                        userList.add(modelUser);
                    }
                }
                //set up adpater
                participantAdapter = new ParticipantAdapter(
                        GroupParticipantAddActivity.this, userList, "" + groupId, "" + myGroupRole);

                //set apdater to recylerview
                usersRv.setAdapter(participantAdapter);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void getDataIntent() {
        groupId = getIntent().getStringExtra("groupId");
    }

    private void loadGroupInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String groupId = "" + ds.child("groupId").getValue();
                    String groupDecription = "" + ds.child("groupDecription").getValue();
                    String groupIcon = "" + ds.child("groupIcon").getValue();
                    String createBy = "" + ds.child("createBy").getValue();
                    String timestamp = "" + ds.child("timestamp").getValue();

                    reference.child(groupId).child("Participants").child(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        myGroupRole = "" + snapshot.child("role").getValue();

                                        loadAllUsers();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void addControls() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        usersRv = findViewById(R.id.usersRv);
        firebaseAuth = FirebaseAuth.getInstance();

    }
}