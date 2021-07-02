package vn.edu.stu.Util;

import java.util.ArrayList;

import vn.edu.stu.Model.RoleGroupPost;
import vn.edu.stu.luanvanmxhhippo.R;

public class DataRoleGroupPost {

    public static ArrayList<RoleGroupPost> roleGroupPostArrayList = new ArrayList<>();

    public DataRoleGroupPost() {
    }

    public static ArrayList<RoleGroupPost> getRoleGroupPostArrayList() {
        roleGroupPostArrayList.clear();
        roleGroupPostArrayList.add(new RoleGroupPost("public", "Public", "", R.drawable.ic_role_public));
        roleGroupPostArrayList.add(new RoleGroupPost("private", "Private", "", R.drawable.ic_role_private));
        return roleGroupPostArrayList;
    }

    public static void setRoleGroupPostArrayList(ArrayList<RoleGroupPost> roleGroupPostArrayList) {
        DataRoleGroupPost.roleGroupPostArrayList = roleGroupPostArrayList;
    }
}
