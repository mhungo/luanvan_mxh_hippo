package vn.edu.stu.Model;

public class Messages {
    private String message_message,
            message_type,
            message_image,
            message_video,
            message_file,
            message_timestamp;
    private boolean message_seen;
    private String message_from;
    private String message_id;

    public Messages(String message_message, String message_type, String message_image, String message_video, String message_file, String message_timestamp, boolean message_seen, String message_from, String message_id) {
        this.message_message = message_message;
        this.message_type = message_type;
        this.message_image = message_image;
        this.message_video = message_video;
        this.message_file = message_file;
        this.message_timestamp = message_timestamp;
        this.message_seen = message_seen;
        this.message_from = message_from;
        this.message_id = message_id;
    }

    public Messages() {
    }

    public String getMessage_message() {
        return message_message;
    }

    public void setMessage_message(String message_message) {
        this.message_message = message_message;
    }

    public String getMessage_type() {
        return message_type;
    }

    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }

    public String getMessage_image() {
        return message_image;
    }

    public void setMessage_image(String message_image) {
        this.message_image = message_image;
    }

    public String getMessage_video() {
        return message_video;
    }

    public void setMessage_video(String message_video) {
        this.message_video = message_video;
    }

    public String getMessage_file() {
        return message_file;
    }

    public void setMessage_file(String message_file) {
        this.message_file = message_file;
    }

    public String getMessage_timestamp() {
        return message_timestamp;
    }

    public void setMessage_timestamp(String message_timestamp) {
        this.message_timestamp = message_timestamp;
    }

    public boolean isMessage_seen() {
        return message_seen;
    }

    public void setMessage_seen(boolean message_seen) {
        this.message_seen = message_seen;
    }

    public String getMessage_from() {
        return message_from;
    }

    public void setMessage_from(String message_from) {
        this.message_from = message_from;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }
}
