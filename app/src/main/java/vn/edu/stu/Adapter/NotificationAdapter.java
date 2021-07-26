package vn.edu.stu.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.List;

import vn.edu.stu.Model.Action;
import vn.edu.stu.Model.Post;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.Util.GetTimeAgo;
import vn.edu.stu.luanvanmxhhippo.InfoProfileFriendActivity;
import vn.edu.stu.luanvanmxhhippo.PostDetailActivity;
import vn.edu.stu.luanvanmxhhippo.R;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context mcontext;
    private List<Action> mNotifications;

    public NotificationAdapter(Context mcontext, List<Action> mNotifications) {
        this.mcontext = mcontext;
        this.mNotifications = mNotifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.notification_item, parent, false);

        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Action notification = mNotifications.get(position);

        //Get thong tin user tung thong bao
        holder.text.setText(notification.getAction_text());
        holder.timenotification.setText(GetTimeAgo.getTimeAgo(Long.parseLong(notification.getAction_timestamp()), mcontext));
        getUserInfo(holder.image_profile, holder.username, notification.getAction_userid());

        //Neu la thong bao post hien thi anh
        if (notification.isAction_ispost()) {
            holder.post_image.setVisibility(View.VISIBLE);
            getPostImage(holder.post_image, notification.getAction_postid());
        } else {
            holder.post_image.setVisibility(View.GONE);
        }

        //Click vao thong bao
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (notification.isAction_ispost()) {
                    SharedPreferences.Editor editor = mcontext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("postid", notification.getAction_postid());
                    editor.apply();

                    Intent intent = new Intent(mcontext, PostDetailActivity.class);
                    mcontext.startActivity(intent);

                    /*((FragmentActivity) mcontext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new PostDetailFragment()).commit();*/

                } else {
                    /*Intent intent = new Intent(mcontext, PostDetailActivity.class);
                    intent.putExtra("profileid", notification.getUserid());
                    mcontext.startActivity(intent);*/

                    SharedPreferences.Editor editor = mcontext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileid", notification.getAction_userid());
                    editor.apply();

                    Intent intent = new Intent(mcontext, InfoProfileFriendActivity.class);
                    mcontext.startActivity(intent);

                    /*((FragmentActivity) mcontext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new ProfileFragment()).commit();*/
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mNotifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image_profile, post_image;
        public TextView username, text, timenotification;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            username = itemView.findViewById(R.id.username);
            text = itemView.findViewById(R.id.comment);
            timenotification = itemView.findViewById(R.id.timenotification);

        }
    }

    //Get thong tin user
    private void getUserInfo(final ImageView imageView, final TextView username, String publisherid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS)
                .child(publisherid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    String text_user = user.getUser_username();
                    username.setText(text_user);
                    try {
                        Glide.with(mcontext).load(user.getUser_imageurl())
                                .placeholder(R.drawable.placeholder)
                                .into(imageView);
                    } catch (Exception e) {
                        imageView.setImageResource(R.drawable.placeholder);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Get Hinh da post
    private void getPostImage(final ImageView imageView, final String postid) {
        List<String> urlImage = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
        databaseReference.child(postid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        Post post = snapshot.getValue(Post.class);
                        if (post != null) {
                            if (post.getPost_type().equals(Constant.DEFAULT_POST_TYPE_IMAGE)) {

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
                                reference.child(postid)
                                        .child(Constant.POST_IMAGE)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                    urlImage.add(dataSnapshot.child("image").getValue().toString());
                                                }
                                                try {
                                                    Glide.with(mcontext).load(urlImage.get(0))
                                                            .placeholder(R.drawable.placeholder)
                                                            .into(imageView);
                                                } catch (Exception e) {
                                                    imageView.setImageResource(R.drawable.placeholder);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            } else if (post.getPost_type().equals(Constant.DEFAULT_POST_TYPE_VIDEO)) {
                                imageView.setImageResource(R.drawable.iconimagevideo);
                            } else {
                                imageView.setImageResource(R.drawable.icontext);
                            }
                        } else {
                            //post is null
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }


}