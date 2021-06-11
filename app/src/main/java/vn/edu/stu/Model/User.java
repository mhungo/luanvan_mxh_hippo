package vn.edu.stu.Model;

public class User {
    private String user_id;
    private String user_email;
    private String user_username;
    private String user_fullname;
    private String user_imageurl;
    private String user_birthday;
    private String user_gender;
    private String user_enable;
    private String user_bio;
    private String user_token;
    private String user_imgbackgroundurl;
    private String user_timestamp;

    public User(String user_id, String user_email, String user_username, String user_fullname, String user_imageurl, String user_birthday, String user_gender, String user_enable, String user_bio, String user_token, String user_imgbackgroundurl, String user_timestamp) {
        this.user_id = user_id;
        this.user_email = user_email;
        this.user_username = user_username;
        this.user_fullname = user_fullname;
        this.user_imageurl = user_imageurl;
        this.user_birthday = user_birthday;
        this.user_gender = user_gender;
        this.user_enable = user_enable;
        this.user_bio = user_bio;
        this.user_token = user_token;
        this.user_imgbackgroundurl = user_imgbackgroundurl;
        this.user_timestamp = user_timestamp;
    }

    public User() {
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getUser_username() {
        return user_username;
    }

    public void setUser_username(String user_username) {
        this.user_username = user_username;
    }

    public String getUser_fullname() {
        return user_fullname;
    }

    public void setUser_fullname(String user_fullname) {
        this.user_fullname = user_fullname;
    }

    public String getUser_imageurl() {
        return user_imageurl;
    }

    public void setUser_imageurl(String user_imageurl) {
        this.user_imageurl = user_imageurl;
    }

    public String getUser_birthday() {
        return user_birthday;
    }

    public void setUser_birthday(String user_birthday) {
        this.user_birthday = user_birthday;
    }

    public String getUser_gender() {
        return user_gender;
    }

    public void setUser_gender(String user_gender) {
        this.user_gender = user_gender;
    }

    public String getUser_enable() {
        return user_enable;
    }

    public void setUser_enable(String user_enable) {
        this.user_enable = user_enable;
    }

    public String getUser_bio() {
        return user_bio;
    }

    public void setUser_bio(String user_bio) {
        this.user_bio = user_bio;
    }

    public String getUser_token() {
        return user_token;
    }

    public void setUser_token(String user_token) {
        this.user_token = user_token;
    }

    public String getUser_imgbackgroundurl() {
        return user_imgbackgroundurl;
    }

    public void setUser_imgbackgroundurl(String user_imgbackgroundurl) {
        this.user_imgbackgroundurl = user_imgbackgroundurl;
    }

    public String getUser_timestamp() {
        return user_timestamp;
    }

    public void setUser_timestamp(String user_timestamp) {
        this.user_timestamp = user_timestamp;
    }
}
