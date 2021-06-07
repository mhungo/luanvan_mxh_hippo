package vn.edu.stu.Model;

import java.util.ArrayList;

public class RolePost {
    private String idRolePost;
    private String nameRolePost;
    private String decriptionRolePost;

    public RolePost(String idRolePost, String nameRolePost, String decriptionRolePost) {
        this.idRolePost = idRolePost;
        this.nameRolePost = nameRolePost;
        this.decriptionRolePost = decriptionRolePost;
    }

    public RolePost() {
    }

    public String getIdRolePost() {
        return idRolePost;
    }

    public void setIdRolePost(String idRolePost) {
        this.idRolePost = idRolePost;
    }

    public String getNameRolePost() {
        return nameRolePost;
    }

    public void setNameRolePost(String nameRolePost) {
        this.nameRolePost = nameRolePost;
    }

    public String getDecriptionRolePost() {
        return decriptionRolePost;
    }

    public void setDecriptionRolePost(String decriptionRolePost) {
        this.decriptionRolePost = decriptionRolePost;
    }

    public static ArrayList<RolePost> rolePostArrayList = new ArrayList<>();

    public void setRolePostArrayList() {
        rolePostArrayList.add(new RolePost("1", "All friends", ""));
        rolePostArrayList.add(new RolePost("2", "Only me", ""));
        rolePostArrayList.add(new RolePost("3", ">Only people in the group", ""));
    }

}
