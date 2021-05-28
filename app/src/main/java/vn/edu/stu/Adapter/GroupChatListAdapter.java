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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import vn.edu.stu.Model.GroupChatList;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.GroupChatActivity;
import vn.edu.stu.luanvanmxhhippo.R;

public class GroupChatListAdapter extends RecyclerView.Adapter<GroupChatListAdapter.HolderGroupChatList> {

    private Context context;
    private ArrayList<GroupChatList> groupChatLists;

    public GroupChatListAdapter(Context context, ArrayList<GroupChatList> groupChatLists) {
        this.context = context;
        this.groupChatLists = groupChatLists;
    }

    @NonNull
    @NotNull
    @Override
    public HolderGroupChatList onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_item, parent, false);

        return new HolderGroupChatList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull GroupChatListAdapter.HolderGroupChatList holder, int position) {
        //get data
        GroupChatList model = groupChatLists.get(position);
        String groupId = model.getGroupId();
        String groupIcon = model.getGroupIcon();
        String groupTitle = model.getGroupTitle();

        holder.nameTv.setText("");
        holder.timeTv.setText("");
        holder.messageTv.setText("");

        //load last message and time
        loadLastMessage(model, holder);

        //set data
        holder.groupTitleTv.setText(groupTitle);
        try {
            Glide.with(context).load(groupIcon).placeholder(R.drawable.placeholder)
                    .into(holder.groupIcon);

        } catch (Exception e) {
            holder.groupIcon.setImageResource(R.drawable.placeholder);
        }

        //handle group click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupId", groupId);
                context.startActivity(intent);
            }
        });

    }

    private void loadLastMessage(GroupChatList model, HolderGroupChatList holder) {
        //get last message from group
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        ref.child((model.getGroupId())).child("Messages").limitToLast(1) // get last item 1
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {

                            //get data
                            String message = "" + ds.child("message").getValue();
                            String timestamp = "" + ds.child("timestamp").getValue();
                            String sender = "" + ds.child("sender").getValue();
                            String messageType = "" + ds.child("type").getValue();

                            //convert time
                            //convert time stamp to dd/mm/yyy hh:mm am/pm
                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                            if (messageType.equals("image")) {
                                holder.messageTv.setText("Sent photo");
                            } else {
                                holder.messageTv.setText(message);
                            }

                            holder.timeTv.setText(dateTime);

                            //get info of sender or last message
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
                            ref.orderByChild(Constant.ID).equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                String name = "" + ds.child(Constant.USERNAME).getValue();
                                                holder.nameTv.setText(name);

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupChatLists.size();
    }

    //view holder
    public class HolderGroupChatList extends RecyclerView.ViewHolder {

        //view ui
        private ImageView groupIcon;
        private TextView groupTitleTv, nameTv, messageTv, timeTv;

        public HolderGroupChatList(@NonNull @NotNull View itemView) {
            super(itemView);

            groupIcon = itemView.findViewById(R.id.image_group);
            groupTitleTv = itemView.findViewById(R.id.text_title_group);
            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);


        }
    }

}
