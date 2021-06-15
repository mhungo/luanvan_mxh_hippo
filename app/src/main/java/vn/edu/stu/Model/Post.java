package vn.edu.stu.Model;

public class Post {
    private String post_id;
    private String post_video;
    private String post_description;
    private String post_publisher;
    private String post_status;
    private String post_rules;
    private String post_type;
    private String post_timestamp;
    private String post_category;

    public Post(String post_id, String post_video, String post_description, String post_publisher, String post_status, String post_rules, String post_type, String post_timestamp, String post_category) {
        this.post_id = post_id;
        this.post_video = post_video;
        this.post_description = post_description;
        this.post_publisher = post_publisher;
        this.post_status = post_status;
        this.post_rules = post_rules;
        this.post_type = post_type;
        this.post_timestamp = post_timestamp;
        this.post_category = post_category;
    }

    public Post() {
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getPost_video() {
        return post_video;
    }

    public void setPost_video(String post_video) {
        this.post_video = post_video;
    }

    public String getPost_description() {
        return post_description;
    }

    public void setPost_description(String post_description) {
        this.post_description = post_description;
    }

    public String getPost_publisher() {
        return post_publisher;
    }

    public void setPost_publisher(String post_publisher) {
        this.post_publisher = post_publisher;
    }

    public String getPost_status() {
        return post_status;
    }

    public void setPost_status(String post_status) {
        this.post_status = post_status;
    }

    public String getPost_rules() {
        return post_rules;
    }

    public void setPost_rules(String post_rules) {
        this.post_rules = post_rules;
    }

    public String getPost_type() {
        return post_type;
    }

    public void setPost_type(String post_type) {
        this.post_type = post_type;
    }

    public String getPost_timestamp() {
        return post_timestamp;
    }

    public void setPost_timestamp(String post_timestamp) {
        this.post_timestamp = post_timestamp;
    }

    public String getPost_category() {
        return post_category;
    }

    public void setPost_category(String post_category) {
        this.post_category = post_category;
    }
}
