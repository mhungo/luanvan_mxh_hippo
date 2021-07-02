package vn.edu.stu.Adapter;

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
import vn.edu.stu.Model.Item;
import vn.edu.stu.Model.Post;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.R;

public class RecylerviewHomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Item> items;

    public RecylerviewHomeAdapter(List<Item> items) {
        this.items = items;
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
        } else {
            return new PostSuggestionViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.user_item,
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
        } else {
            User user = (User) items.get(position).getObject();
            ((PostSuggestionViewHolder) holder).getInfoUser(user, (PostSuggestionViewHolder) holder);
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
