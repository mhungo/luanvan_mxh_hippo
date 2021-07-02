package vn.edu.stu.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import vn.edu.stu.Adapter.GroupPostAdapter;
import vn.edu.stu.Model.GroupPost;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.GroupPostCreateActivity;
import vn.edu.stu.luanvanmxhhippo.R;


public class GroupPostFragment extends Fragment {

    private Toolbar toolbar;
    private ImageView btn_add_group_post, ic_setting_group;
    private RecyclerView recycler_view_post_group_post, recycler_view_all_group_post;

    private FirebaseUser firebaseUser;

    private List<GroupPost> groupPosts;
    private GroupPostAdapter groupPostAdapter;


    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_post, container, false);

        addControls(view);
        addEvents(view);

        loadGroupPost();

        return view;
    }

    private void loadGroupPost() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child(Constant.COLLECTION_PARTICIPANTS).child(firebaseUser.getUid()).exists()) {
                        GroupPost groupPost = dataSnapshot.getValue(GroupPost.class);
                        groupPosts.add(groupPost);
                    }
                }
                groupPostAdapter = new GroupPostAdapter(groupPosts, getContext());
                recycler_view_all_group_post.setAdapter(groupPostAdapter);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void addEvents(View view) {

        btn_add_group_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), GroupPostCreateActivity.class));
            }
        });
    }

    private void addControls(View view) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        btn_add_group_post = view.findViewById(R.id.btn_add_group_post);
        ic_setting_group = view.findViewById(R.id.ic_setting_group);
        recycler_view_post_group_post = view.findViewById(R.id.recycler_view_post_group_post);

        recycler_view_all_group_post = view.findViewById(R.id.recycler_view_all_group_post);
        recycler_view_all_group_post.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recycler_view_all_group_post.setLayoutManager(linearLayoutManager);
        groupPosts = new ArrayList<>();

    }
}