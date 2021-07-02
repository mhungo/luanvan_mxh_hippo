package vn.edu.stu.Model;

public class GroupPost {
    private String grouppost_id;
    private String grouppost_title;
    private String grouppost_decription;
    private String grouppost_icon;
    private String grouppost_createby;
    private String grouppost_timestamp;
    private String grouppost_role;

    public GroupPost(String grouppost_id, String grouppost_title, String grouppost_decription, String grouppost_icon, String grouppost_createby, String grouppost_timestamp, String grouppost_role) {
        this.grouppost_id = grouppost_id;
        this.grouppost_title = grouppost_title;
        this.grouppost_decription = grouppost_decription;
        this.grouppost_icon = grouppost_icon;
        this.grouppost_createby = grouppost_createby;
        this.grouppost_timestamp = grouppost_timestamp;
        this.grouppost_role = grouppost_role;
    }

    public GroupPost() {
    }

    public String getGrouppost_id() {
        return grouppost_id;
    }

    public void setGrouppost_id(String grouppost_id) {
        this.grouppost_id = grouppost_id;
    }

    public String getGrouppost_title() {
        return grouppost_title;
    }

    public void setGrouppost_title(String grouppost_title) {
        this.grouppost_title = grouppost_title;
    }

    public String getGrouppost_decription() {
        return grouppost_decription;
    }

    public void setGrouppost_decription(String grouppost_decription) {
        this.grouppost_decription = grouppost_decription;
    }

    public String getGrouppost_icon() {
        return grouppost_icon;
    }

    public void setGrouppost_icon(String grouppost_icon) {
        this.grouppost_icon = grouppost_icon;
    }

    public String getGrouppost_createby() {
        return grouppost_createby;
    }

    public void setGrouppost_createby(String grouppost_createby) {
        this.grouppost_createby = grouppost_createby;
    }

    public String getGrouppost_timestamp() {
        return grouppost_timestamp;
    }

    public void setGrouppost_timestamp(String grouppost_timestamp) {
        this.grouppost_timestamp = grouppost_timestamp;
    }

    public String getGrouppost_role() {
        return grouppost_role;
    }

    public void setGrouppost_role(String grouppost_role) {
        this.grouppost_role = grouppost_role;
    }
}
