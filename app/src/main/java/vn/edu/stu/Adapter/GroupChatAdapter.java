package vn.edu.stu.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import vn.edu.stu.Model.GroupChat;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.OpenImagenActivity;
import vn.edu.stu.luanvanmxhhippo.R;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.HolderGroupChat> {


    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private ArrayList<GroupChat> groupChatArrayList;

    private FirebaseAuth firebaseAuth;

    public GroupChatAdapter(Context context, ArrayList<GroupChat> groupChatArrayList) {
        this.context = context;
        this.groupChatArrayList = groupChatArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @NotNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.group_chat_right, parent, false);
            return new HolderGroupChat(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.group_chat_left, parent, false);
            return new HolderGroupChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull GroupChatAdapter.HolderGroupChat holder, int position) {

        //get  data
        GroupChat model = groupChatArrayList.get(position);
        String timestamp = model.getGroudchat_timestamp();
        String message = model.getGroudchat_message();
        String sender = model.getGroudchat_sender();
        String messageType = model.getGroudchat_type();
        String messageImage = model.getGroudchat_image();

        //convert time stamp to dd/mm/yyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        if (messageType.equals("text")) {
            //text message, hide image, show textview
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageTv.setText(message);
        } else {
            //image message
            holder.messageIv.setVisibility(View.VISIBLE);
            holder.messageTv.setVisibility(View.GONE);
            try {
                Glide.with(context).load(messageImage).placeholder(R.drawable.placeholder)
                        .into(holder.messageIv);
            } catch (Exception e) {
                holder.messageIv.setImageResource(R.drawable.placeholder);
            }
        }
        //set data

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageType.equals("image")) {
                    Intent intent = new Intent(context, OpenImagenActivity.class);
                    intent.putExtra("image_url_open", model.getGroudchat_image());
                    context.startActivity(intent);
                }
            }
        });

        holder.timeTv.setText(dateTime);

        setUserName(model, holder);

    }

    private void setUserName(GroupChat model, HolderGroupChat holder) {
        //get sender info from uid iin model
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.orderByChild(Constant.USER_ID).equalTo(model.getGroudchat_sender())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String name = "" + ds.child(Constant.USER_USERNAME).getValue();

                            holder.nameTv.setText(name);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

    }

    @Override
    public int getItemViewType(int position) {
        if (groupChatArrayList.get(position).getGroudchat_sender().equals(firebaseAuth.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    @Override
    public int getItemCount() {
        return groupChatArrayList.size();
    }

    class HolderGroupChat extends RecyclerView.ViewHolder {

        private TextView nameTv, messageTv, timeTv;
        private ImageView messageIv;

        public HolderGroupChat(@NonNull @NotNull View itemView) {
            super(itemView);

            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            messageIv = itemView.findViewById(R.id.messageIv);


        }
    }


}
