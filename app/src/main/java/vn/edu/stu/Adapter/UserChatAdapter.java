package vn.edu.stu.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.edu.stu.Model.Messages;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.MessageActivity;
import vn.edu.stu.luanvanmxhhippo.R;

public class UserChatAdapter extends RecyclerView.Adapter<UserChatAdapter.ViewHolder> {

    private Context mContext;
    private List<User> userList;
    private boolean isSerach;

    private String mLastMessage;
    private String mUserChatId;

    public UserChatAdapter(Context mContext, List<User> userList, boolean isSerach) {
        this.mContext = mContext;
        this.userList = userList;
        this.isSerach = isSerach;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_chat_item, parent, false);
        return new UserChatAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final User user = userList.get(position);
        //Kiem tra an hien textLastMessage
        if (isSerach == true) {
            //if la fragment serach thi an cai lastMessage di
            holder.textViewLastMessage.setVisibility(View.GONE);
        } else {
            holder.textViewLastMessage.setVisibility(View.VISIBLE);
        }

        //gan id user chat
        mUserChatId = user.getId();

        //Load ten va hinh dai dien
        holder.textViewUsername.setText(user.getUsername());
        Glide.with(mContext).load(user.getImageurl()).into(holder.profileImage);

        //Load lastMessage
        lastMessage(holder.textViewLastMessage);

        //Mo man hinh chat
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("user_id", user.getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView profileImage;
        public TextView textViewUsername, textViewLastMessage;
        public CircleImageView userStatusOn, userStatusOff;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profile_image);
            textViewUsername = itemView.findViewById(R.id.username);
            textViewLastMessage = itemView.findViewById(R.id.last_msg);
            userStatusOn = itemView.findViewById(R.id.user_status_on);
            userStatusOff = itemView.findViewById(R.id.user_status_off);
        }
    }

    //Ham lay last message
    private void lastMessage(TextView lastMsg) {
        mLastMessage = Constant.DEFAULT;
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Messages")
                .child(firebaseUser.getUid()).child(mUserChatId);
        Query query = reference.limitToLast(1);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages messages = snapshot.getValue(Messages.class);
                //Toast.makeText(mContext, messages.getMessage(), Toast.LENGTH_SHORT).show();

                if (messages.getFrom().equals(firebaseUser.getUid())) {
                    mLastMessage = mContext.getString(R.string.txt_You) + messages.getMessage();
                    lastMsg.setText(mLastMessage);
                } else {
                    mLastMessage = messages.getMessage();
                    if (messages.isSeen()) {
                        lastMsg.setTextColor(Color.GRAY);
                        lastMsg.setTypeface(null, Typeface.NORMAL);
                    } else {
                        lastMsg.setTextColor(Color.BLACK);
                        lastMsg.setTypeface(null, Typeface.BOLD);
                    }
                }
                if (!mLastMessage.equals(Constant.DEFAULT)) {
                    if (messages.getType().equals("text")) {
                        lastMsg.setText(mLastMessage);
                    } else if (messages.getType().equals("image")) {
                        lastMsg.setText(R.string.txt_NhanDuocAnh);
                    }
                }
                mLastMessage = Constant.DEFAULT;
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}