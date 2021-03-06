package vn.edu.stu.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import vn.edu.stu.Adapter.ChatListAdapter;
import vn.edu.stu.Adapter.UserChatAdapter;
import vn.edu.stu.Model.ChatList;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.MainActivity;
import vn.edu.stu.luanvanmxhhippo.R;


public class ChatFragment extends Fragment {

    private ImageView imageViewBack;

    private RecyclerView recyclerView;
    private UserChatAdapter userChatAdapter;
    private List<User> userList;

    private List<ChatList> chatLists;
    private ChatListAdapter chatListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        addControls(view);
        addEvents();

        readChatList();

        return view;
    }

    private void addEvents() {
        //Nut icon back
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                getContext().startActivity(intent);
            }
        });
    }

    private void addControls(View view) {
        imageViewBack = view.findViewById(R.id.back);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);

        chatLists = new ArrayList<>();

        chatListAdapter = new ChatListAdapter(getContext(), chatLists);
        recyclerView.setAdapter(chatListAdapter);

    }

    private void readChatList() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_CHATLIST);
        Query query = reference.child(FirebaseAuth.getInstance().getUid())
                .orderByChild(Constant.CHATLIST_LASTMESSAGE_TIMESTAMP);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatLists.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatList chatList = dataSnapshot.getValue(ChatList.class);
                    chatLists.add(chatList);
                }
                //backgroundReadUser.start();
                //readUser();
                chatListAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readUser() {
        userList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        Query query = reference.orderByChild(Constant.USER_ID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    for (ChatList chatList : chatLists) {
                        if (user.getUser_id().equals(chatList.getChatlist_id())) {
                            userList.add(user);
                        }
                    }
                }
                userChatAdapter = new UserChatAdapter(getContext(), userList, false);
                recyclerView.setAdapter(userChatAdapter);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}