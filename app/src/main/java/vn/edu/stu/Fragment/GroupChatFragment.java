package vn.edu.stu.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import vn.edu.stu.Adapter.GroupChatListAdapter;
import vn.edu.stu.Model.GroupChatList;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.GroupCreateActivity;
import vn.edu.stu.luanvanmxhhippo.R;


public class GroupChatFragment extends Fragment {

    private RecyclerView groupRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<String> timeStampGroupChat;
    private ArrayList<GroupChatList> groupChatLists;
    private GroupChatListAdapter groupChatListAdapter;

    private FloatingActionButton btnCreateGroup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_chat, container, false);


        groupRv = view.findViewById(R.id.groupRv);
        groupRv.setHasFixedSize(true);
        groupRv.setLayoutManager(new LinearLayoutManager(getContext()));
        btnCreateGroup = view.findViewById(R.id.btnCreateGroup);

        firebaseAuth = FirebaseAuth.getInstance();

        //handle click
        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), GroupCreateActivity.class);
                startActivity(intent);
            }
        });

        loadGroupChatList();

        return view;
    }

    private void loadGroupChatList() {
        groupChatLists = new ArrayList<>();
        timeStampGroupChat = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                groupChatLists.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    //if current user exists in participants of group then show group
                    if (dataSnapshot.child(Constant.COLLECTION_PARTICIPANTS).child(firebaseAuth.getUid()).exists()) {
                        GroupChatList model = dataSnapshot.getValue(GroupChatList.class);
                        groupChatLists.add(model);
                    }
                }

                //Collections.sort(groupChatLists);
                groupChatListAdapter = new GroupChatListAdapter(getContext(), groupChatLists);
                groupRv.setAdapter(groupChatListAdapter);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }


    private void searchGroupChatList(String query) {
        groupChatLists = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                groupChatLists.size();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    //if current user exists in participants of group then show group
                    if (!dataSnapshot.child(Constant.COLLECTION_PARTICIPANTS).child(firebaseAuth.getUid()).exists()) {

                        //search groupTitle chat
                        if (dataSnapshot.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())) {
                            GroupChatList model = dataSnapshot.getValue(GroupChatList.class);
                            groupChatLists.add(model);
                        }

                    }
                }
                groupChatListAdapter = new GroupChatListAdapter(getContext(), groupChatLists);
                groupRv.setAdapter(groupChatListAdapter);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }


}