package vn.edu.stu.Model;

public class RoleGroupPost {
    private String idRoleGroupPost;
    private String nameRoleGroupPost;
    private String decriptionRoleGroupPost;
    private int icon;


    public RoleGroupPost(String idRoleGroupPost, String nameRoleGroupPost, String decriptionRoleGroupPost, int icon) {
        this.idRoleGroupPost = idRoleGroupPost;
        this.nameRoleGroupPost = nameRoleGroupPost;
        this.decriptionRoleGroupPost = decriptionRoleGroupPost;
        this.icon = icon;
    }

    public RoleGroupPost() {
    }

    public String getIdRoleGroupPost() {
        return idRoleGroupPost;
    }

    public void setIdRoleGroupPost(String idRoleGroupPost) {
        this.idRoleGroupPost = idRoleGroupPost;
    }

    public String getNameRoleGroupPost() {
        return nameRoleGroupPost;
    }

    public void setNameRoleGroupPost(String nameRoleGroupPost) {
        this.nameRoleGroupPost = nameRoleGroupPost;
    }

    public String getDecriptionRoleGroupPost() {
        return decriptionRoleGroupPost;
    }

    public void setDecriptionRoleGroupPost(String decriptionRoleGroupPost) {
        this.decriptionRoleGroupPost = decriptionRoleGroupPost;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return getNameRoleGroupPost();
    }
}
