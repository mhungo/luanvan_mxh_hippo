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

import vn.edu.stu.Model.Messages;
import vn.edu.stu.luanvanmxhhippo.OpenImagenActivity;
import vn.edu.stu.luanvanmxhhippo.R;

public class ImageMessageAdapter extends RecyclerView.Adapter<ImageMessageAdapter.ViewHolder> {

    private Context context;
    private List<Messages> messagesList;

    public ImageMessageAdapter(Context context, List<Messages> messagesList) {
        this.context = context;
        this.messagesList = messagesList;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);

        return new ImageMessageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ImageMessageAdapter.ViewHolder holder, int position) {
        Messages messages = messagesList.get(position);
        try {
            Glide.with(context).load(messages.getMessage_image())
                    .placeholder(R.drawable.placeholder)
                    .into(holder.image_item);
        } catch (Exception e) {
            holder.image_item.setImageResource(R.drawable.placeholder);
        }

        holder.image_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OpenImagenActivity.class);
                intent.putExtra("image_url_open", messages.getMessage_image());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image_item;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            image_item = itemView.findViewById(R.id.image_item);
        }
    }
}
