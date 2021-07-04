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

import vn.edu.stu.Adapter.PostAdapter;
import vn.edu.stu.Model.Post;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.R;


public class SearchPostFragment extends Fragment {


    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;

    private SearchView search_bar;

    private CircularProgressIndicator progressBar;

    private FirebaseAuth firebaseAuth;

    private List<String> userListIdBlocked;

    private DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_post, container, false);

        reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
        firebaseAuth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        progressBar = view.findViewById(R.id.progress_bar);
        search_bar = view.findViewById(R.id.search_bar);

        userListIdBlocked = new ArrayList<>();

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        recyclerView.setAdapter(postAdapter);

        readIdBlockUser();
        readPost();
        //backgroundReadUser.start();

        search_bar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    readPost();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    searchPost(newText);
                }
                return false;
            }
        });

        return view;
    }

    private void searchPost(String newText) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        postList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Post post = dataSnapshot.getValue(Post.class);

                            if (!userListIdBlocked.contains(post.getPost_publisher())) {
                                if ((post.getPost_description().contains(newText) && post.getPost_rules().equals(Constant.DEFAULT_POST_ROLE_PUBLIC)) ||
                                        (post.getPost_description().contains(newText) && post.getPost_publisher().equals(firebaseAuth.getUid()) &&
                                                post.getPost_rules().equals(Constant.DEFAULT_POST_ROLE_PRIVATE))) {
                                    postList.add(post);
                                }
                            }
                        }
                        postAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        }, 500);
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

    private void readPost() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Query query = reference.limitToLast(10);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (search_bar.getQuery().toString().equals("")) {
                            postList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Post post = dataSnapshot.getValue(Post.class);
                                if (post.getPost_rules().equals(Constant.DEFAULT_POST_ROLE_PUBLIC) && !userListIdBlocked.contains(post.getPost_publisher()))
                                    postList.add(post);
                            }
                            postAdapter.notifyDataSetChanged();
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