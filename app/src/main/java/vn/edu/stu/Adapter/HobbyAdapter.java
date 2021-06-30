package vn.edu.stu.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import vn.edu.stu.Model.Hobby;
import vn.edu.stu.luanvanmxhhippo.R;

public class HobbyAdapter extends RecyclerView.Adapter<HobbyAdapter.ViewHolder> {

    private Context context;
    private List<Hobby> hobbyList;

    public HobbyAdapter(Context context, List<Hobby> hobbyList) {
        this.context = context;
        this.hobbyList = hobbyList;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.hobby_item, parent, false);
        return new HobbyAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull HobbyAdapter.ViewHolder holder, int position) {

        holder.checkbox.setText(hobbyList.get(position).getTitle());

        //null


    }

    @Override
    public int getItemCount() {
        return hobbyList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CheckBox checkbox;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            checkbox = itemView.findViewById(R.id.checkbox);
        }
    }
}
