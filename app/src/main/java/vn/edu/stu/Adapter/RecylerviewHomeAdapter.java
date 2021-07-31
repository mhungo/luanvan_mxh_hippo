package vn.edu.stu.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.denzcoskun.imageslider.ImageSlider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.edu.stu.Model.GroupPost;
import vn.edu.stu.Model.Item;
import vn.edu.stu.Model.Post;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.GroupPostActivity;
import vn.edu.stu.luanvanmxhhippo.InfoProfileFriendActivity;
import vn.edu.stu.luanvanmxhhippo.R;

public class RecylerviewHomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Item> items;
    private Context context;

    public RecylerviewHomeAdapter(List<Item> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new PostViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.post_item,
                            parent,
                            false
                    )
            );
        } else if (viewType == 1) {
            return new PostSuggestionViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.user_item,
                            parent,
                            false
                    )
            );

        } else {
            return new GroupPostViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.group_post_search_item,
                            parent,
                            false
                    )
            );

        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == 0) {
            Post post = (Post) items.get(position).getObject();

            ((PostViewHolder) holder).publisherInfo(((PostViewHolder) holder).image_profile,
                    ((PostViewHolder) holder).username,
                    ((PostViewHolder) holder).publisher,
                    post.getPost_publisher());
        } else if (getItemViewType(position) == 1) {
            User user = (User) items.get(position).getObject();
            ((PostSuggestionViewHolder) holder).getInfoUser(user, (PostSuggestionViewHolder) holder);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileid", user.getUser_id());
                    editor.apply();

                    Intent intent = new Intent(context, InfoProfileFriendActivity.class);
                    context.startActivity(intent);

                    /*((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new ProfileFragment()).commit();*/
                }
            });
        } else {
            GroupPost groupPost = (GroupPost) items.get(position).getObject();
            ((GroupPostViewHolder) holder).loadInfoGroup(groupPost, (GroupPostViewHolder) holder);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, GroupPostActivity.class);
                    intent.putExtra("group_post_id", groupPost.getGrouppost_id());
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    static class PostSuggestionViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public TextView fullname;
        public CircleImageView image_profile;
        public Button btn_follow;

        public PostSuggestionViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            fullname = itemView.findViewById(R.id.fullname);
            image_profile = itemView.findViewById(R.id.image_profile);
            btn_follow = itemView.findViewById(R.id.btn_follow);
        }

        private void getInfoUser(User user, PostSuggestionViewHolder holder) {
            holder.username.setText(user.getUser_username());
            holder.fullname.setText(user.getUser_fullname());

            //Load anh
            try {
                Glide.with(itemView.getContext()).load(user.getUser_imageurl())
                        .placeholder(R.drawable.placeholder)
                        .into(holder.image_profile);
            } catch (Exception e) {
                holder.image_profile.setImageResource(R.drawable.placeholder);
            }
        }
    }

    static class GroupPostViewHolder extends RecyclerView.ViewHolder {

        public ImageView img_group_post;
        public TextView txt_title_group_post, txt_decription_group_post;

        public GroupPostViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            img_group_post = itemView.findViewById(R.id.img_group_post);
            txt_title_group_post = itemView.findViewById(R.id.txt_title_group_post);
            txt_decription_group_post = itemView.findViewById(R.id.txt_decription_group_post);
        }

        private void loadInfoGroup(GroupPost groupPost, GroupPostViewHolder holder) {
            holder.txt_title_group_post.setText(groupPost.getGrouppost_title());
            holder.txt_decription_group_post.setText(groupPost.getGrouppost_decription());
            try {
                Glide.with(itemView.getContext()).load(groupPost.getGrouppost_icon())
                        .placeholder(R.drawable.placeholder)
                        .into(holder.img_group_post);
            } catch (Exception e) {
                holder.img_group_post.setImageResource(R.drawable.placeholder);
            }
        }
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {

        public ImageView image_profile, like, comment, save, chat, more, share, filterImage, iconrole;
        public TextView username, likes, publisher, description, comments, time;
        public ImageSlider post_image;
        private VideoView post_video;

        public PostViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            post_video = itemView.findViewById(R.id.post_video);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            save = itemView.findViewById(R.id.save);
            chat = itemView.findViewById(R.id.chat);
            username = itemView.findViewById(R.id.username);
            likes = itemView.findViewById(R.id.likes);
            share = itemView.findViewById(R.id.share);
            publisher = itemView.findViewById(R.id.publisher);
            description = itemView.findViewById(R.id.description);
            comments = itemView.findViewById(R.id.comments);
            more = itemView.findViewById(R.id.more);
            filterImage = itemView.findViewById(R.id.filterImage);

            iconrole = itemView.findViewById(R.id.iconrole);
            time = itemView.findViewById(R.id.time);
        }

        private void publisherInfo(final ImageView image_profile, final TextView username, final TextView publisher, final String userid) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                    .child(Constant.COLLECTION_USERS)
                    .child(userid);

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    try {
                        Glide.with(itemView.getContext()).load(user.getUser_imageurl())
                                .placeholder(R.drawable.placeholder)
                                .into(image_profile);
                    } catch (Exception e) {
                        image_profile.setImageResource(R.drawable.placeholder);
                    }
                    //set username, text username
                    username.setText(user.getUser_username());
                    publisher.setText(user.getUser_username());
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }
    }
}
