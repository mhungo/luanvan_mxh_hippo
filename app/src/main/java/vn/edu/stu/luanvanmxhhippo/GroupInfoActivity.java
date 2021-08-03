package vn.edu.stu.luanvanmxhhippo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import vn.edu.stu.Adapter.ParticipantAdapter;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;

public class GroupInfoActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private String groupId = "";
    private String groupRole = "";

    private ActionBar actionBar;

    private FirebaseAuth firebaseAuth;

    private ImageView groupAvata;
    private TextView groupDecription, createdBy, editGroup, addParticipant, leaveGroup, participantTotal;
    private RecyclerView participantsRv;

    private List<String> idUserList;
    private ArrayList<User> userList;
    private ParticipantAdapter participantAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        addControls();
        getDataIntent();
        addEvents();

        loadGroupInfo();
        loadGroupRole();
    }

    private void loadGroupRole() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        reference.child(groupId)
                .child(Constant.COLLECTION_PARTICIPANTS)
                .orderByChild("uid")
                .equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            groupRole = "" + dataSnapshot.child("role").getValue();
                            actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail() + "(" + groupRole + ")");

                            if (groupRole.equals("participant")) {
                                editGroup.setVisibility(View.GONE);
                                addParticipant.setVisibility(View.GONE);
                                leaveGroup.setText(R.string.leave_group);

                            } else if (groupRole.equals("admin")) {
                                editGroup.setVisibility(View.GONE);
                                addParticipant.setVisibility(View.VISIBLE);

                            } else if (groupRole.equals("creator")) {
                                editGroup.setVisibility(View.VISIBLE);
                                addParticipant.setVisibility(View.VISIBLE);
                                leaveGroup.setText(R.string.deletegroup);
                            }
                        }

                        //load id user of group
                        loadIdParticipants();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void loadIdParticipants() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        reference.child(groupId)
                .child(Constant.COLLECTION_PARTICIPANTS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        idUserList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String uid = "" + ds.child("uid").getValue();

                            idUserList.add(uid);
                        }

                        loadUser();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    //load user
    private void loadUser() {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (idUserList.contains(user.getUser_id())) {
                        userList.add(user);
                    }
                }

                participantAdapter = new ParticipantAdapter(GroupInfoActivity.this, userList, groupId, groupRole);
                participantsRv.setAdapter(participantAdapter);
                participantTotal.setText(getString(R.string.participant) + userList.size() + ")");
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void loadGroupInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        reference.orderByChild(Constant.GROUP_ID).equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    //get data
                    String groupId = "" + dataSnapshot.child(Constant.GROUP_ID).getValue();
                    String groupTitle = "" + dataSnapshot.child(Constant.GROUP_TITLE).getValue();
                    String groupIcon = "" + dataSnapshot.child(Constant.GROUP_ICON).getValue();
                    String groupDes = "" + dataSnapshot.child(Constant.GROUP_DECRIPTION).getValue();
                    String createBy = "" + dataSnapshot.child(Constant.GROUP_CREATEBY).getValue();
                    String timestamp = "" + dataSnapshot.child(Constant.GROUP_TIMESTAMP).getValue();

                    //convert time
                    //convert time stamp to dd/mm/yyy hh:mm am/pm
                    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(timestamp));
                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    loadCreatorInfo(dateTime, createBy);

                    actionBar.setTitle(groupTitle);
                    groupDecription.setText(groupDes);

                    try {
                        Glide.with(GroupInfoActivity.this).load(groupIcon)
                                .placeholder(R.drawable.placeholder)
                                .into(groupAvata);
                    } catch (Exception e) {
                        groupAvata.setImageResource(R.drawable.placeholder);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    //load info creator
    private void loadCreatorInfo(String dateTime, String createBy) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.orderByChild(Constant.USER_ID).equalTo(createBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);

                    createdBy.setText(getString(R.string.txt_createBy) + " " + user.getUser_username() + " " + getString(R.string.on) + " " + dateTime);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void addEvents() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        addParticipant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupParticipantAddActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        leaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //participant or admin: leave group
                //creator: delete group

                String title = "";
                String decription = "";
                String positiveButtonTitle = "";

                if (groupRole.equals("creator")) {
                    title = getString(R.string.deleteGroup);
                    decription = getString(R.string.are_you_sure_delete_group);
                    positiveButtonTitle = getString(R.string.delete);
                } else {
                    title = getString(R.string.leave_group);
                    decription = getString(R.string.are_you_sure_leave_group);
                    positiveButtonTitle = getString(R.string.leave);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(title)
                        .setMessage(decription)
                        .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (groupRole.equals("creator")) {
                                    //creator : delete group
                                    deleteGroup();
                                } else {
                                    //participant or admin : leave group
                                    leaveGroup();
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        editGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupEditActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });
    }

    private void leaveGroup() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        reference.child(groupId).child(Constant.COLLECTION_PARTICIPANTS).child(firebaseAuth.getUid())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //group left successfully
                        Toast.makeText(GroupInfoActivity.this, R.string.group_lefft_successfull, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(GroupInfoActivity.this, ChatManagerActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        //failed to leave group
                    }
                });
    }

    private void deleteGroup() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        reference.child(groupId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //group delete successfully...
                        Toast.makeText(GroupInfoActivity.this, R.string.group_delete_successfull, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(GroupInfoActivity.this, ChatManagerActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        //failed delete group
                        Toast.makeText(GroupInfoActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getDataIntent() {
        groupId = getIntent().getStringExtra("groupId");
    }

    private void addControls() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        idUserList = new ArrayList<>();
        userList = new ArrayList<>();

        groupAvata = findViewById(R.id.groupIcon);
        groupDecription = findViewById(R.id.groupDecription);
        createdBy = findViewById(R.id.createdBy);
        editGroup = findViewById(R.id.editGroup);
        addParticipant = findViewById(R.id.addParticipant);
        leaveGroup = findViewById(R.id.leaveGroup);
        participantTotal = findViewById(R.id.participantTotal);
        participantsRv = findViewById(R.id.participantsRv);

        firebaseAuth = FirebaseAuth.getInstance();


    }
}