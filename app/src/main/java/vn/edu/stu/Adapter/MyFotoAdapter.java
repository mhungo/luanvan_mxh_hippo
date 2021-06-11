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

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
        List<SlideModel> sliderList = new ArrayList<>();
        sliderList.clear();

        if (post.getPost_type().equals(Constant.DEFAULT_POST_TYPE_IMAGE)) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS)
                    .child(post.getPost_id()).child(Constant.POST_IMAGE);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        sliderList.add(new SlideModel(dataSnapshot.child("image").getValue().toString(), ScaleTypes.CENTER_INSIDE));
                    }
                    holder.post_image.setImageList(sliderList, ScaleTypes.CENTER_INSIDE);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        } else if (post.getPost_type().equals(Constant.DEFAULT_POST_TYPE_VIDEO)) {
            sliderList.add(new SlideModel(Constant.VARIABLE_ICONVIDEO, ScaleTypes.CENTER_INSIDE));
            holder.post_image.setImageList(sliderList, ScaleTypes.CENTER_INSIDE);
        } else {
            sliderList.add(new SlideModel(Constant.VARIABLE_ICONTEXT, ScaleTypes.CENTER_INSIDE));
            holder.post_image.setImageList(sliderList, ScaleTypes.CENTER_INSIDE);
        }

        holder.post_image.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemSelected(int i) {
                /*SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("postid", post.getPostid());
                editor.apply();

                Intent intent = new Intent(context, PostDetailActivity.class);
                context.startActivity(intent);*/
            }
        });

        holder.filterImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("postid", post.getPost_id());
                editor.apply();

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

        public ImageSlider post_image;
        public ImageView filterImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            post_image = itemView.findViewById(R.id.post_images);
            filterImage = itemView.findViewById(R.id.filterImage);

        }
    }
}