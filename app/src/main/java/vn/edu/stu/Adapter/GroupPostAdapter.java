package vn.edu.stu.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import vn.edu.stu.Model.GroupPost;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.GroupPostActivity;
import vn.edu.stu.luanvanmxhhippo.R;

public class GroupPostAdapter extends RecyclerView.Adapter<GroupPostAdapter.ViewHolder> {

    private List<GroupPost> groupPosts;
    private Context context;

    public GroupPostAdapter(List<GroupPost> groupPosts, Context context) {
        this.groupPosts = groupPosts;
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_post_item, parent, false);
        return new GroupPostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull GroupPostAdapter.ViewHolder holder, int position) {
        GroupPost groupPost = groupPosts.get(position);

        if (groupPost != null) {
            loadInfoItem(groupPost, holder);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(groupPost.getGrouppost_id())) {

                                if (snapshot.child(groupPost.getGrouppost_id())
                                        .child(Constant.COLLECTION_PARTICIPANTS)
                                        .hasChild(FirebaseAuth.getInstance().getUid())) {

                                    Intent intent = new Intent(context, GroupPostActivity.class);
                                    intent.putExtra("group_post_id", groupPost.getGrouppost_id());
                                    context.startActivity(intent);
                                } else {
                                    Snackbar.make(holder.itemView, R.string.you_are_not_member_group, BaseTransientBottomBar.LENGTH_SHORT).show();
                                }

                            } else {
                                Snackbar.make(holder.itemView, R.string.group_not_exist, BaseTransientBottomBar.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });


                }
            });
        } else {
            //null
        }

    }

    private void loadInfoItem(GroupPost groupPost, ViewHolder holder) {
        holder.txt_title_group_post.setText(groupPost.getGrouppost_title());
        try {
            Glide.with(context).load(groupPost.getGrouppost_icon())
                    .placeholder(R.drawable.placeholder)
                    .into(holder.img_group_post);
        } catch (Exception e) {
            holder.img_group_post.setImageResource(R.drawable.placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return groupPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView img_group_post;
        public TextView txt_title_group_post;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            img_group_post = itemView.findViewById(R.id.img_group_post);
            txt_title_group_post = itemView.findViewById(R.id.txt_title_group_post);
        }
    }
}
