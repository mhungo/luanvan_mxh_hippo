package vn.edu.stu.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.edu.stu.Model.Messages;
import vn.edu.stu.Model.Status;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.MessageActivity;
import vn.edu.stu.luanvanmxhhippo.R;

public class UserChatAdapter extends RecyclerView.Adapter<UserChatAdapter.ViewHolder> {

    private Context mContext;
    private List<User> userList;
    private boolean isSerach;

    private ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

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
        viewBinderHelper.bind(holder.SwipeRevealLayoutChatList, user.getUser_id());

        //Kiem tra an hien textLastMessage
        if (isSerach == true) {
            //if la fragment serach thi an cai lastMessage di
            holder.textViewLastMessage.setVisibility(View.GONE);
            holder.SwipeRevealLayoutChatList.setLockDrag(true);
        } else {
            holder.textViewLastMessage.setVisibility(View.VISIBLE);
        }

        //gan id user chat
        mUserChatId = user.getUser_id();

        //Load ten va hinh dai dien
        holder.textViewUsername.setText(user.getUser_username());
        try {
            Glide.with(mContext).load(user.getUser_imageurl())
                    .placeholder(R.drawable.placeholder)
                    .into(holder.profileImage);
        } catch (Exception e) {
            holder.profileImage.setImageResource(R.drawable.placeholder);
        }

        //Load lastMessage
        lastMessage(holder.textViewLastMessage);

        //check online/offline
        checkStatusOnOff(user, holder);

        //Mo man hinh chat
        holder.layout_chatlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkIsBlock(user, holder);
            }
        });

        holder.layout_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.custom_dialog_unfriend_layout);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.BOTTOM);
                dialog.setCancelable(true);

                MaterialButton btn_confirm_dialog, btn_cancel_dialog;
                TextView textviewtitile;
                btn_confirm_dialog = dialog.findViewById(R.id.btn_confirm_dialog);
                btn_cancel_dialog = dialog.findViewById(R.id.btn_cancel_dialog);
                textviewtitile = dialog.findViewById(R.id.textviewtitile);
                textviewtitile.setText(mContext.getString(R.string.are_you_remove) + " ?");

                //confirm unfollow
                btn_confirm_dialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteChatList(holder, user);
                        dialog.dismiss();
                    }
                });

                //cancel
                btn_cancel_dialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                //show dialog
                dialog.show();
            }
        });
    }

    private void deleteChatList(ViewHolder holder, User user) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_CHATLIST);
        reference.child(firebaseUser.getUid())
                .child(user.getUser_id())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_MESSAGES);
                            ref.child(firebaseUser.getUid())
                                    .child(user.getUser_id())
                                    .removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull @NotNull Exception e) {

                                        }
                                    });
                        } else {
                            //fail
                        }
                    }
                });
    }

    private void checkIsBlock(User user, ViewHolder holder) {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference1.child(user.getUser_id())
                .child(Constant.COLLECTION_BLOCKUSER)
                .orderByChild(Constant.BLOCK_USER_ID)
                .equalTo(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (dataSnapshot.exists()) {
                                Snackbar.make(holder.itemView, R.string.you_block_not_sent_message, BaseTransientBottomBar.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        Intent intent = new Intent(mContext, MessageActivity.class);
                        intent.putExtra("user_id", user.getUser_id());
                        mContext.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void checkStatusOnOff(User user, ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_STATUS)
                .child(user.getUser_id());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Status status = snapshot.getValue(Status.class);
                String sta_on_off = status.getStatus_status();
                String timstamp = status.getStatus_timestamp() + "";

                if (sta_on_off.equals("true")) {
                    holder.userStatusOn.setVisibility(View.VISIBLE);
                } else {
                    holder.userStatusOn.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public SwipeRevealLayout SwipeRevealLayoutChatList;
        public RelativeLayout layout_chatlist;
        public TextView tvDeleteChat;
        public LinearLayout layout_delete;
        public ImageView profileImage;
        public TextView textViewUsername, textViewLastMessage;
        public CircleImageView userStatusOn, userStatusOff;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            SwipeRevealLayoutChatList = itemView.findViewById(R.id.SwipeRevealLayoutChatList);
            layout_chatlist = itemView.findViewById(R.id.layout_chatlist);
            tvDeleteChat = itemView.findViewById(R.id.tvDeleteChat);
            layout_delete = itemView.findViewById(R.id.layout_delete);

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
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_MESSAGES)
                .child(firebaseUser.getUid()).child(mUserChatId);
        Query query = reference.limitToLast(1);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages messages = snapshot.getValue(Messages.class);
                //Toast.makeText(mContext, messages.getMessage(), Toast.LENGTH_SHORT).show();

                if (messages.getMessage_from().equals(firebaseUser.getUid())) {
                    mLastMessage = mContext.getString(R.string.txt_You) + messages.getMessage_message();
                    lastMsg.setText(mLastMessage);
                } else {
                    mLastMessage = messages.getMessage_message();
                    if (messages.isMessage_seen()) {
                        lastMsg.setTextColor(Color.GRAY);
                        lastMsg.setTypeface(null, Typeface.NORMAL);
                    } else {
                        lastMsg.setTextColor(Color.BLACK);
                        lastMsg.setTypeface(null, Typeface.BOLD);
                    }
                }
                if (!mLastMessage.equals(Constant.DEFAULT)) {
                    if (messages.getMessage_type().equals("text")) {
                        lastMsg.setText(mLastMessage);
                    } else if (messages.getMessage_type().equals("image")) {
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