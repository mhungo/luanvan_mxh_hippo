package vn.edu.stu.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
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

import vn.edu.stu.Adapter.UserChatAdapter;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.R;


public class SearchingUserChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserChatAdapter userChatAdapter;
    private List<User> userList;

    private EditText editTextSearch;

    private SearchView searchView;

    private FirebaseUser mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_searching_user_chat, container, false);

        mAuth = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = view.findViewById(R.id.recycler_view);
        editTextSearch = view.findViewById(R.id.search_user);
        searchView = view.findViewById(R.id.search_bar);

        userList = new ArrayList<>();
        userChatAdapter = new UserChatAdapter(getContext(), userList, true);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(userChatAdapter);

        readUser();
        //backgroundReadUser.start();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals("")) {
                    readUser();
                } else {
                    searchUser(newText);
                }
                return false;
            }
        });


        return view;
    }

    private void searchUser(String s) {
        final List<String> listIdFriend = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        DatabaseReference referenceFriend = FirebaseDatabase.getInstance().getReference((Constant.COLLECTION_FRIENDS));

        referenceFriend.child(mAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        listIdFriend.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            listIdFriend.add(dataSnapshot.getKey());
                        }

                        reference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                userList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    User user = dataSnapshot.getValue(User.class);
                                    for (String i : listIdFriend) {
                                        if (user.getUser_id().equals(i))
                                            if (user.getUser_username().contains(s) || user.getUser_fullname().contains(s)) {
                                                userList.add(user);
                                            }
                                    }
                                }
                                userChatAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
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
                final List<String> listIdFriend = new ArrayList<>();
                DatabaseReference referenceFriend = FirebaseDatabase.getInstance().getReference((Constant.COLLECTION_FRIENDS));
                referenceFriend.child(mAuth.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                listIdFriend.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    listIdFriend.add(dataSnapshot.getKey());
                                }

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
                                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                        userList.clear();
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                            User user = dataSnapshot.getValue(User.class);
                                            if (mAuth != null) {
                                                for (String i : listIdFriend) {
                                                    if (user.getUser_id().equals(i))
                                                        userList.add(user);
                                                }
                                            }

                                            userChatAdapter.notifyDataSetChanged();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
            }
        }, 1000);

    }
}