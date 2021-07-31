package vn.edu.stu.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import vn.edu.stu.Adapter.RecylerviewHomeAdapter;
import vn.edu.stu.Adapter.UserAdapter;
import vn.edu.stu.Model.GroupPost;
import vn.edu.stu.Model.Item;
import vn.edu.stu.Model.Post;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.R;


public class SearchUserFragment extends Fragment {


    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsersList;

    private List<String> userListIdBlocked;

    private SearchView search_bar;

    private CircularProgressIndicator progressBar;

    private DatabaseReference reference;
    private List<Item> items;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_user, container, false);


        reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        progressBar = view.findViewById(R.id.progress_bar);
        search_bar = view.findViewById(R.id.search_bar);
        userListIdBlocked = new ArrayList<>();

        items = new ArrayList<>();

        mUsersList = new ArrayList<>();
        /*userAdapter = new UserAdapter(getContext(), mUsersList, true);
        recyclerView.setAdapter(userAdapter);*/

        readIdBlockUser();
        readUser();

        //backgroundReadUser.start();

        search_bar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    //readUser();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    searchUsers(newText);
                    //searchGroupPost(newText);
                }
                return false;
            }
        });

        return view;
    }

    private void searchGroupPost(String newText) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    GroupPost groupPost = dataSnapshot.getValue(GroupPost.class);
                    if (groupPost.getGrouppost_title().toLowerCase().contains(newText.toLowerCase())
                            && groupPost.getGrouppost_role().equals(Constant.DEFAULT_POST_ROLE_PUBLIC)) {
                        items.add(new Item(2, groupPost));
                    }
                }
                progressBar.setVisibility(View.GONE);
                RecylerviewHomeAdapter adapter = new RecylerviewHomeAdapter(items, getContext());
                recyclerView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void readPost() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
                Query query = reference1.limitToLast(10);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (search_bar.getQuery().toString().equals("")) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Post post = dataSnapshot.getValue(Post.class);
                                if (post.getPost_rules().equals(Constant.DEFAULT_POST_ROLE_PUBLIC) && !userListIdBlocked.contains(post.getPost_publisher()))
                                    items.add(new Item(0, post));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

            }
        }, 1000);
    }

    private void searchUsers(String s) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                items.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);

                    if (user.getUser_username().toLowerCase().contains(s.toLowerCase())
                            || user.getUser_fullname().toLowerCase().contains(s.toLowerCase())
                            || user.getUser_email().toLowerCase().contains(s.toLowerCase())) {
                        if (userListIdBlocked.contains(user.getUser_id())) {

                        } else {
                            items.add(new Item(1, user));
                        }
                    }
                }
                searchGroupPost(s);
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
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    GroupPost groupPost = dataSnapshot.getValue(GroupPost.class);
                    if (groupPost.getGrouppost_role().equals(Constant.DEFAULT_POST_ROLE_PUBLIC)) {
                        items.add(new Item(2, groupPost));
                    }

                }
                progressBar.setVisibility(View.GONE);
                RecylerviewHomeAdapter adapter = new RecylerviewHomeAdapter(items, getContext());
                recyclerView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    //load id user blocked
    private void readIdBlockUser() {

        userListIdBlocked = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(FirebaseAuth.getInstance().getUid())
                .child(Constant.COLLECTION_BLOCKUSER)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        userListIdBlocked.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            userListIdBlocked.add(dataSnapshot.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

    }

    private void readUser() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Query query = reference.limitToLast(10);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        items.clear();
                        if (search_bar.getQuery().toString().equals("")) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                User user = dataSnapshot.getValue(User.class);
                                if (userListIdBlocked.contains(user.getUser_id())) {

                                } else {
                                    items.add(new Item(1, user));
                                    mUsersList.add(user);
                                }
                            }
                            loadGroupPost();
                            //readPost();
                            progressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

            }
        }, 1000);

    }
}