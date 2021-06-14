package vn.edu.stu.Util;

import java.util.ArrayList;

import vn.edu.stu.Model.RolePost;
import vn.edu.stu.luanvanmxhhippo.R;

public class DataRolePost {

    public static ArrayList<RolePost> rolePostArrayList = new ArrayList<>();

    public DataRolePost() {

    }

    public static ArrayList<RolePost> getRolePostArrayList() {
        rolePostArrayList.clear();
        rolePostArrayList.add(new RolePost("public", "All friends", "", R.drawable.ic_role_public));
        rolePostArrayList.add(new RolePost("private", "Only me", "", R.drawable.ic_role_private));
        rolePostArrayList.add(new RolePost("onlyfriend", "Only people in the group", "", R.drawable.ic_role_friend));
        return rolePostArrayList;
    }

    public static void setRolePostArrayList(ArrayList<RolePost> rolePostArrayList) {
        DataRolePost.rolePostArrayList = rolePostArrayList;
    }
}