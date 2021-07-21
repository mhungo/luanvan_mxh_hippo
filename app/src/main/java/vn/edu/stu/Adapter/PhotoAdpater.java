package vn.edu.stu.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import vn.edu.stu.Model.Story;
import vn.edu.stu.luanvanmxhhippo.OpenImagenActivity;
import vn.edu.stu.luanvanmxhhippo.R;

public class PhotoAdpater extends RecyclerView.Adapter<PhotoAdpater.PhotoViewHolder> {

    private Context mContext;
    private List<Story> mListPhoto;

    public PhotoAdpater(Context mContext, List<Story> mListPhoto) {
        this.mContext = mContext;
        this.mListPhoto = mListPhoto;
    }


    @NonNull
    @NotNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PhotoAdpater.PhotoViewHolder holder, int position) {
        Story story = mListPhoto.get(position);
        try {
            Glide.with(mContext).load(story.getImageurl()).placeholder(R.drawable.placeholder).into(holder.imgPhoto);
        } catch (Exception e) {
            holder.imgPhoto.setImageResource(R.drawable.placeholder);
        }

        holder.imgPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, OpenImagenActivity.class);
                intent.putExtra("image_url_open", story.getImageurl());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mListPhoto == null) {
            return 0;
        } else {
            return mListPhoto.size();
        }
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgPhoto;

        public PhotoViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            imgPhoto = itemView.findViewById(R.id.img_photo);
        }
    }
}
