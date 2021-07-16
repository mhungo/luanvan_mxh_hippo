package vn.edu.stu.Util;

import android.content.Context;

import java.util.ArrayList;

import vn.edu.stu.Model.RolePost;
import vn.edu.stu.luanvanmxhhippo.R;

public class DataRolePost {

    public static ArrayList<RolePost> rolePostArrayList = new ArrayList<>();

    public DataRolePost() {

    }

    public static ArrayList<RolePost> getRolePostArrayList(Context context) {
        String allfriend = context.getString(R.string.all_friend);
        String onlyme = context.getString(R.string.onlyme);
        String onlypeopleofgroup = context.getString(R.string.onlypeopleofgroup);

        rolePostArrayList.clear();
        rolePostArrayList.add(new RolePost("public", allfriend, "", R.drawable.ic_role_public));
        rolePostArrayList.add(new RolePost("private", onlyme, "", R.drawable.ic_role_private));
        rolePostArrayList.add(new RolePost("onlyfriend", onlypeopleofgroup, "", R.drawable.ic_role_friend));
        return rolePostArrayList;
    }

    public static void setRolePostArrayList(ArrayList<RolePost> rolePostArrayList) {
        DataRolePost.rolePostArrayList = rolePostArrayList;
    }
}
