package vn.edu.stu.Model;

public class RolePost {
    private String idRolePost;
    private String nameRolePost;
    private String decriptionRolePost;
    private int icon;


    public RolePost(String idRolePost, String nameRolePost, String decriptionRolePost, int icon) {
        this.idRolePost = idRolePost;
        this.nameRolePost = nameRolePost;
        this.decriptionRolePost = decriptionRolePost;
        this.icon = icon;
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

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return nameRolePost;
    }
}
