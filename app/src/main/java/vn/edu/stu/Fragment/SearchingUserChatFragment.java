package vn.edu.stu.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import vn.edu.stu.Adapter.UserChatAdapter;
import vn.edu.stu.Model.User;
import vn.edu.stu.luanvanmxhhippo.R;


public class SearchingUserChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserChatAdapter userChatAdapter;
    private List<User> userList;

    private EditText editTextSearch;

    private FirebaseUser mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_searching_user_chat, container, false);

        mAuth = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = view.findViewById(R.id.recycler_view);
        editTextSearch = view.findViewById(R.id.search_user);

        userList = new ArrayList<>();
        userChatAdapter = new UserChatAdapter(getContext(), userList, true);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(userChatAdapter);

        readUser();
        //backgroundReadUser.start();

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUser(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }

    private void searchUser(String s) {
        final List<String> followList = new ArrayList<>();
        DatabaseReference referenceFollow = FirebaseDatabase.getInstance().getReference("Follow").child(mAuth.getUid())
                .child("following");
        referenceFollow.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    followList.add(dataSnapshot.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("username")
                .startAt(s)
                .endAt(s + "\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (mAuth != null) {
                        for (String i : followList) {
                            if (user.getId().equals(i))
                                userList.add(user);
                        }
                    }
                }
                userChatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readUser() {
        final List<String> followList = new ArrayList<>();
        DatabaseReference referenceFollow = FirebaseDatabase.getInstance().getReference("Follow").child(mAuth.getUid())
                .child("following");
        referenceFollow.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    followList.add(dataSnapshot.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (editTextSearch.getText().toString().equals("")) {
                    userList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (mAuth != null) {
                            for (String i : followList) {
                                if (user.getId().equals(i))
                                    userList.add(user);
                            }
                        }
                    }
                    userChatAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}