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

import org.jetbrains.annotations.NotNull;

import java.util.List;

import vn.edu.stu.Model.GroupPost;
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
                    Intent intent = new Intent(context, GroupPostActivity.class);
                    intent.putExtra("group_post_id", groupPost.getGrouppost_id());
                    context.startActivity(intent);
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
