package vn.edu.stu.Model;

public class Comment {
    private String comment_comment;
    private String comment_publisher;
    private String comment_commentid;
    private String comment_timstamp;
    private String comment_image;

    public Comment(String comment_comment, String comment_publisher, String comment_commentid, String comment_timstamp, String comment_image) {
        this.comment_comment = comment_comment;
        this.comment_publisher = comment_publisher;
        this.comment_commentid = comment_commentid;
        this.comment_timstamp = comment_timstamp;
        this.comment_image = comment_image;
    }

    public Comment() {
    }

    public String getComment_comment() {
        return comment_comment;
    }

    public void setComment_comment(String comment_comment) {
        this.comment_comment = comment_comment;
    }

    public String getComment_publisher() {
        return comment_publisher;
    }

    public void setComment_publisher(String comment_publisher) {
        this.comment_publisher = comment_publisher;
    }

    public String getComment_commentid() {
        return comment_commentid;
    }

    public void setComment_commentid(String comment_commentid) {
        this.comment_commentid = comment_commentid;
    }

    public String getComment_timstamp() {
        return comment_timstamp;
    }

    public void setComment_timstamp(String comment_timstamp) {
        this.comment_timstamp = comment_timstamp;
    }

    public String getComment_image() {
        return comment_image;
    }

    public void setComment_image(String comment_image) {
        this.comment_image = comment_image;
    }
}
