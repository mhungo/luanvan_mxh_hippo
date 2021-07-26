package vn.edu.stu.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.progressindicator.CircularProgressIndicator;
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
import vn.edu.stu.Adapter.GroupPostItemAdapter;
import vn.edu.stu.Model.GroupPost;
import vn.edu.stu.Model.GroupPostPosts;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.GroupPostCreateActivity;
import vn.edu.stu.luanvanmxhhippo.R;


public class GroupPostFragment extends Fragment {

    private Toolbar toolbar;
    private ImageView btn_add_group_post, ic_setting_group;
    private RecyclerView recycler_view_post_group_post, recycler_view_all_group_post;

    private FirebaseUser firebaseUser;

    private SwipeRefreshLayout mRefreshLayout;

    private List<GroupPost> groupPosts;
    private List<GroupPostPosts> groupPostPosts;
    private GroupPostAdapter groupPostAdapter;
    private GroupPostItemAdapter groupPostItemAdapter;

    private CircularProgressIndicator progress_circular;


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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadGroupPost();
                loadPostPosts();
            }
        }, 500);


        return view;
    }

    private void loadPostPosts() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                groupPostPosts.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child(Constant.COLLECTION_PARTICIPANTS).child(firebaseUser.getUid()).exists()) {
                        reference.child(dataSnapshot.getKey())
                                .child(Constant.COLLECTION_POSTS)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                        for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                            GroupPostPosts post = dataSnapshot1.getValue(GroupPostPosts.class);
                                            if (post.getPost_status().equals(Constant.DEFAULT_STATUS_ENABLE)) {
                                                groupPostPosts.add(post);
                                            }
                                        }
                                        groupPostItemAdapter = new GroupPostItemAdapter(getContext(), groupPostPosts, "");
                                        recycler_view_post_group_post.setAdapter(groupPostItemAdapter);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                });
                    }
                }
                progress_circular.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void loadGroupPost() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                groupPosts.clear();
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

    private void reloadData() {
        loadGroupPost();
        loadPostPosts();

        mRefreshLayout.setRefreshing(false);
    }

    private void addEvents(View view) {

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadData();
            }
        });

        btn_add_group_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), GroupPostCreateActivity.class));
            }
        });
    }

    private void addControls(View view) {

        progress_circular = view.findViewById(R.id.progress_circular);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mRefreshLayout = view.findViewById(R.id.mRefreshLayout);

        btn_add_group_post = view.findViewById(R.id.btn_add_group_post);
        ic_setting_group = view.findViewById(R.id.ic_setting_group);
        recycler_view_post_group_post = view.findViewById(R.id.recycler_view_post_group_post);
        recycler_view_post_group_post.setHasFixedSize(true);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getContext());
        linearLayout.setReverseLayout(true);
        linearLayout.setStackFromEnd(true);
        recycler_view_post_group_post.setLayoutManager(linearLayout);
        groupPostPosts = new ArrayList<>();


        recycler_view_all_group_post = view.findViewById(R.id.recycler_view_all_group_post);
        recycler_view_all_group_post.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recycler_view_all_group_post.setLayoutManager(linearLayoutManager);
        groupPosts = new ArrayList<>();

    }
}