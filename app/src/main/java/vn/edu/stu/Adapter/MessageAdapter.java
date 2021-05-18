package vn.edu.stu.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.edu.stu.Model.Messages;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.GetTimeAgo;
import vn.edu.stu.luanvanmxhhippo.R;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context mContext;
    private List<Messages> messagesList;


    public MessageAdapter(Context mContext, List<Messages> messagesList) {
        this.mContext = mContext;
        this.messagesList = messagesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.message_layout_item, parent, false);

        return new MessageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        //Lay id user hien tai
        final String current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //lay message tai vi tri pos
        final Messages messages = messagesList.get(position);

        //Lay id nguoi gui
        String from_user = messages.getFrom();
        //Lay type message: text or image
        final String message_type = messages.getType();

        //Set thoi gian cua tung message
        GetTimeAgo ago = new GetTimeAgo();
        holder.fromTime.setText(ago.getTimeAgo(messages.getTime(), mContext));
        holder.toTime.setText(ago.getTimeAgo(messages.getTime(), mContext));


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(from_user);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Set hinh user chat
                User user = snapshot.getValue(User.class);
                Glide.with(mContext.getApplicationContext()).load(user.getImageurl()).into(holder.profileImage);

                //Kiem tra neu la current user thi an layout ben trai di
                if (snapshot.getKey().equals(current_user_id)) {
                    holder.rightMsgLayout.setVisibility(LinearLayout.VISIBLE);

                    if (message_type.equals("text")) {
                        holder.rightMsgTextView.setVisibility(View.VISIBLE);
                        holder.rightMsgTextView.setText(messages.getMessage());
                        //An hinh
                        holder.rightImageView.setVisibility(View.GONE);

                    } else if (messages.getType().equals("image")) {
                        holder.rightImageView.setVisibility(View.VISIBLE);
                        Glide.with(mContext.getApplicationContext()).load(messages.getMessage()).into(holder.rightImageView);
                        //An text
                        holder.rightMsgTextView.setVisibility(View.GONE);
                    }
                    holder.leftMsgLayout.setVisibility(LinearLayout.GONE);
                } else {
                    holder.leftMsgLayout.setVisibility(LinearLayout.VISIBLE);

                    if (message_type.equals("text")) {
                        holder.leftMsgTextView.setVisibility(View.VISIBLE);
                        holder.leftMsgTextView.setText(messages.getMessage());
                        //An hinh
                        holder.leftImageView.setVisibility(View.GONE);

                    } else if (message_type.equals("image")) {
                        holder.leftImageView.setVisibility(View.VISIBLE);
                        Glide.with(mContext.getApplicationContext()).load(messages.getMessage()).into(holder.leftImageView);
                        //An text
                        holder.leftMsgTextView.setVisibility(View.GONE);
                    }
                    holder.rightMsgLayout.setVisibility(LinearLayout.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView profileImage;
        public LinearLayout leftMsgLayout;
        public LinearLayout rightMsgLayout;

        public TextView leftMsgTextView;
        public TextView rightMsgTextView;
        public TextView fromTime;
        public TextView toTime;

        public ImageView leftImageView;
        public ImageView rightImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profile_image_chat);
            rightMsgLayout = itemView.findViewById(R.id.chat_right_layout_item);
            leftMsgLayout = itemView.findViewById(R.id.chat_left_layout_item);
            leftMsgTextView = itemView.findViewById(R.id.chat_msg_left_text_view);
            rightMsgTextView = itemView.findViewById(R.id.chat_msg_right_text_view);
            fromTime = itemView.findViewById(R.id.from_time);
            toTime = itemView.findViewById(R.id.to_time);

            leftImageView = itemView.findViewById(R.id.chat_image_left);
            rightImageView = itemView.findViewById(R.id.chat_image_right);
        }
    }


}