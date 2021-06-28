package vn.edu.stu.Model;

public class ReplyComment {
    private String replycomment_comment;
    private String replycomment_publisher;
    private String replycomment_replyuserid;
    private String replycomment_commentid;
    private String replycomment_timstamp;
    private String replycomment_image;

    public ReplyComment(String replycomment_comment, String replycomment_publisher, String replycomment_replyuserid, String replycomment_commentid, String replycomment_timstamp, String replycomment_image) {
        this.replycomment_comment = replycomment_comment;
        this.replycomment_publisher = replycomment_publisher;
        this.replycomment_replyuserid = replycomment_replyuserid;
        this.replycomment_commentid = replycomment_commentid;
        this.replycomment_timstamp = replycomment_timstamp;
        this.replycomment_image = replycomment_image;
    }

    public ReplyComment() {
    }

    public String getReplycomment_comment() {
        return replycomment_comment;
    }

    public void setReplycomment_comment(String replycomment_comment) {
        this.replycomment_comment = replycomment_comment;
    }

    public String getReplycomment_publisher() {
        return replycomment_publisher;
    }

    public void setReplycomment_publisher(String replycomment_publisher) {
        this.replycomment_publisher = replycomment_publisher;
    }

    public String getReplycomment_replyuserid() {
        return replycomment_replyuserid;
    }

    public void setReplycomment_replyuserid(String replycomment_replyuserid) {
        this.replycomment_replyuserid = replycomment_replyuserid;
    }

    public String getReplycomment_commentid() {
        return replycomment_commentid;
    }

    public void setReplycomment_commentid(String replycomment_commentid) {
        this.replycomment_commentid = replycomment_commentid;
    }

    public String getReplycomment_timstamp() {
        return replycomment_timstamp;
    }

    public void setReplycomment_timstamp(String replycomment_timstamp) {
        this.replycomment_timstamp = replycomment_timstamp;
    }

    public String getReplycomment_image() {
        return replycomment_image;
    }

    public void setReplycomment_image(String replycomment_image) {
        this.replycomment_image = replycomment_image;
    }
}
