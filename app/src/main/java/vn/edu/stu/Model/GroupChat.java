package vn.edu.stu.Model;

public class GroupChat {
    private String groudchat_message,
            groudchat_timestamp,
            groudchat_type,
            groudchat_sender,
            groudchat_image,
            groudchat_video,
            groudchat_file;

    public GroupChat(String groudchat_message, String groudchat_timestamp, String groudchat_type, String groudchat_sender, String groudchat_image, String groudchat_video, String groudchat_file) {
        this.groudchat_message = groudchat_message;
        this.groudchat_timestamp = groudchat_timestamp;
        this.groudchat_type = groudchat_type;
        this.groudchat_sender = groudchat_sender;
        this.groudchat_image = groudchat_image;
        this.groudchat_video = groudchat_video;
        this.groudchat_file = groudchat_file;
    }

    public GroupChat() {
    }

    public String getGroudchat_message() {
        return groudchat_message;
    }

    public void setGroudchat_message(String groudchat_message) {
        this.groudchat_message = groudchat_message;
    }

    public String getGroudchat_timestamp() {
        return groudchat_timestamp;
    }

    public void setGroudchat_timestamp(String groudchat_timestamp) {
        this.groudchat_timestamp = groudchat_timestamp;
    }

    public String getGroudchat_type() {
        return groudchat_type;
    }

    public void setGroudchat_type(String groudchat_type) {
        this.groudchat_type = groudchat_type;
    }

    public String getGroudchat_sender() {
        return groudchat_sender;
    }

    public void setGroudchat_sender(String groudchat_sender) {
        this.groudchat_sender = groudchat_sender;
    }

    public String getGroudchat_image() {
        return groudchat_image;
    }

    public void setGroudchat_image(String groudchat_image) {
        this.groudchat_image = groudchat_image;
    }

    public String getGroudchat_video() {
        return groudchat_video;
    }

    public void setGroudchat_video(String groudchat_video) {
        this.groudchat_video = groudchat_video;
    }

    public String getGroudchat_file() {
        return groudchat_file;
    }

    public void setGroudchat_file(String groudchat_file) {
        this.groudchat_file = groudchat_file;
    }
}
