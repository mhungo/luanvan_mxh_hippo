package vn.edu.stu.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import vn.edu.stu.Model.ImagePhoto;
import vn.edu.stu.Model.Post;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.PostDetailActivity;
import vn.edu.stu.luanvanmxhhippo.R;

public class MyFotoAdapter extends RecyclerView.Adapter<MyFotoAdapter.ViewHolder> {

    private Context context;
    private List<Post> mPosts;

    public MyFotoAdapter(Context context, List<Post> mPosts) {
        this.context = context;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fotos_item, parent, false);

        return new MyFotoAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Post post = mPosts.get(position);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS)
                .child(post.getPublisher()).child(post.getPostid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                ImagePhoto imagePhoto = snapshot.getValue(ImagePhoto.class);
                if (imagePhoto != null) {
                    Glide.with(context).load(imagePhoto.getImage()).into(holder.post_image);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


        holder.post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("postid", post.getPostid());
                editor.apply();

                /*((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new PostDetailFragment()).commit();*/

                Intent intent = new Intent(context, PostDetailActivity.class);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView post_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            post_image = itemView.findViewById(R.id.post_image);

        }
    }
}