package vn.edu.stu.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import vn.edu.stu.Model.RolePost;
import vn.edu.stu.luanvanmxhhippo.R;

public class RolePostAdapter extends ArrayAdapter<RolePost> {
    private Activity context;
    private ArrayList<RolePost> rolePosts;
    private int resource;

    public RolePostAdapter(@NonNull Activity context, int resource, @NonNull ArrayList<RolePost> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.rolePosts = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = this.context.getLayoutInflater();
        View view = layoutInflater.inflate(this.resource, null);

        ImageView icon_role = view.findViewById(R.id.icon_role);
        TextView text_role_item = view.findViewById(R.id.text_role_item);
        TextView text_decription_role_item = view.findViewById(R.id.text_decription_role_item);

        RolePost rolePost = rolePosts.get(position);

        try {
            icon_role.setImageResource(rolePost.getIcon());
        } catch (Exception e) {
            icon_role.setImageResource(R.drawable.placeholder);
        }

        text_role_item.setText(rolePost.getNameRolePost());
        text_decription_role_item.setText(rolePost.getDecriptionRolePost());

        return view;
    }
}
