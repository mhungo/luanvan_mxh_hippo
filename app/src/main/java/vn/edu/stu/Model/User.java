package vn.edu.stu.Model;

public class User {
    private String id;
    private String email;
    private String username;
    private String fullname;
    private String imageurl;
    private String birthday;
    private String gender;
    private String bio;
    private String token;

    public User(String id, String email, String username, String fullname, String imageurl, String birthday, String gender, String bio, String status, String token) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.fullname = fullname;
        this.imageurl = imageurl;
        this.birthday = birthday;
        this.gender = gender;
        this.bio = bio;
        this.token = token;
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
