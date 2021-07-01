package vn.edu.stu.Model;

public class DataTypeCall {
    private String type;
    private String meetingType;
    private String name;
    private String email;
    private String imageURL;
    private String invitertoken;
    private String meetingRoom;

    public DataTypeCall(String type, String meetingType, String name, String email, String imageURL, String invitertoken, String meetingRoom) {
        this.type = type;
        this.meetingType = meetingType;
        this.name = name;
        this.email = email;
        this.imageURL = imageURL;
        this.invitertoken = invitertoken;
        this.meetingRoom = meetingRoom;
    }

    public DataTypeCall() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMeetingType() {
        return meetingType;
    }

    public void setMeetingType(String meetingType) {
        this.meetingType = meetingType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getInvitertoken() {
        return invitertoken;
    }

    public void setInvitertoken(String invitertoken) {
        this.invitertoken = invitertoken;
    }

    public String getMeetingRoom() {
        return meetingRoom;
    }

    public void setMeetingRoom(String meetingRoom) {
        this.meetingRoom = meetingRoom;
    }
}
