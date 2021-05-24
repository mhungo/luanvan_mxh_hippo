package vn.edu.stu.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import vn.edu.stu.Model.GroupChatList;
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
        GroupChatList groupChatList = groupChatLists.get(position);
        String groupId = groupChatList.getGroupId();
        String groupIcon = groupChatList.getGroupIcon();
        String groupTitle = groupChatList.getGroupTitle();

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
