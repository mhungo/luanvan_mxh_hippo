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
import java.util.List;

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
    private List<String> stringListIdFriend;
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

    private void loadIdFriendList() {
        //init list

        userList = new ArrayList<>();
        stringListIdFriend = new ArrayList<>();

        //load Id friend
        DatabaseReference referenceFriend = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDS);
        referenceFriend.child(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        stringListIdFriend.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            stringListIdFriend.add(dataSnapshot.getKey());
                        }
                        loadFriend();

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

    }

    private void loadFriend() {
        //load users from database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User modelUser = ds.getValue(User.class);

                    //get all user accept currently signed in
                    if (!firebaseAuth.getUid().equals(modelUser.getUser_id())) {
                        //not my uid
                        for (String id : stringListIdFriend) {
                            if (modelUser.getUser_id().equals(id)) {
                                userList.add(modelUser);
                            }
                        }
                    } else {
                        //this current user
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
        ref.orderByChild(Constant.GROUP_ID)
                .equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {

                            String groupId = "" + ds.child(Constant.GROUP_ID).getValue();
                            String groupTitle = "" + ds.child(Constant.GROUP_TITLE).getValue();
                            String groupIcon = "" + ds.child(Constant.GROUP_ICON).getValue();
                            String groupDecription = "" + ds.child(Constant.GROUP_DECRIPTION).getValue();
                            String createBy = "" + ds.child(Constant.GROUP_CREATEBY).getValue();
                            String timestamp = "" + ds.child(Constant.GROUP_TIMESTAMP).getValue();

                            reference.child(groupId).child("Participants").child(firebaseAuth.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                myGroupRole = "" + snapshot.child("role").getValue();

                                                loadIdFriendList();
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