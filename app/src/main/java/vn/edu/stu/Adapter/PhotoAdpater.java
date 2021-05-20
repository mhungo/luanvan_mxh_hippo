package vn.edu.stu.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import vn.edu.stu.luanvanmxhhippo.R;

public class PhotoAdpater extends RecyclerView.Adapter<PhotoAdpater.PhotoViewHolder> {

    private Context mContext;
    private List<Uri> mListPhoto;

    public PhotoAdpater(Context mContext) {
        this.mContext = mContext;
    }

    public void setData(List<Uri> list) {
        this.mListPhoto = list;
        notifyDataSetChanged();
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
        Uri uri = mListPhoto.get(position);
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
