package vn.edu.stu.Util;

import android.content.Context;

import java.util.ArrayList;

import vn.edu.stu.Model.RoleGroupPost;
import vn.edu.stu.luanvanmxhhippo.R;

public class DataRoleGroupPost {

    public static ArrayList<RoleGroupPost> roleGroupPostArrayList = new ArrayList<>();

    public DataRoleGroupPost() {
    }

    public static ArrayList<RoleGroupPost> getRoleGroupPostArrayList(Context context) {
        String publics = context.getString(R.string.publics);
        String privates = context.getString(R.string.privates);

        roleGroupPostArrayList.clear();
        roleGroupPostArrayList.add(new RoleGroupPost("public", publics, "", R.drawable.ic_role_public));
        roleGroupPostArrayList.add(new RoleGroupPost("private", privates, "", R.drawable.ic_role_private));
        return roleGroupPostArrayList;
    }

    public static void setRoleGroupPostArrayList(ArrayList<RoleGroupPost> roleGroupPostArrayList) {
        DataRoleGroupPost.roleGroupPostArrayList = roleGroupPostArrayList;
    }
}
