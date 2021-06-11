package vn.edu.stu.Model;

public class Action {
    private String action_userid;
    private String action_text;
    private String action_postid;
    private String action_timestamp;
    private boolean action_ispost;

    public Action(String action_userid, String action_text, String action_postid, String action_timestamp, boolean action_ispost) {
        this.action_userid = action_userid;
        this.action_text = action_text;
        this.action_postid = action_postid;
        this.action_timestamp = action_timestamp;
        this.action_ispost = action_ispost;
    }

    public Action() {
    }

    public String getAction_userid() {
        return action_userid;
    }

    public void setAction_userid(String action_userid) {
        this.action_userid = action_userid;
    }

    public String getAction_text() {
        return action_text;
    }

    public void setAction_text(String action_text) {
        this.action_text = action_text;
    }

    public String getAction_postid() {
        return action_postid;
    }

    public void setAction_postid(String action_postid) {
        this.action_postid = action_postid;
    }

    public String getAction_timestamp() {
        return action_timestamp;
    }

    public void setAction_timestamp(String action_timestamp) {
        this.action_timestamp = action_timestamp;
    }

    public boolean isAction_ispost() {
        return action_ispost;
    }

    public void setAction_ispost(boolean action_ispost) {
        this.action_ispost = action_ispost;
    }
}
