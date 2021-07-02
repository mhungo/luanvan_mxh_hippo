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

import vn.edu.stu.Model.RoleGroupPost;
import vn.edu.stu.luanvanmxhhippo.R;

public class RoleGroupPostAdapter extends ArrayAdapter<RoleGroupPost> {

    private ArrayList<RoleGroupPost> roleGroupPosts;
    private Activity activity;
    private int resource;


    public RoleGroupPostAdapter(@NonNull Activity context, int resource, @NonNull ArrayList<RoleGroupPost> objects) {
        super(context, resource, objects);

        this.activity = context;
        this.resource = resource;
        this.roleGroupPosts = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = this.activity.getLayoutInflater();
        View view = layoutInflater.inflate(this.resource, parent, false);

        ImageView icon_role = view.findViewById(R.id.icon_role);
        TextView text_role_item = view.findViewById(R.id.text_role_item);
        TextView text_decription_role_item = view.findViewById(R.id.text_decription_role_item);

        RoleGroupPost roleGroupPost = roleGroupPosts.get(position);

        try {
            icon_role.setImageResource(roleGroupPost.getIcon());
        } catch (Exception e) {
            icon_role.setImageResource(R.drawable.placeholder);
        }

        text_role_item.setText(roleGroupPost.getNameRoleGroupPost());
        text_decription_role_item.setText(roleGroupPost.getDecriptionRoleGroupPost());

        return view;


    }
}
