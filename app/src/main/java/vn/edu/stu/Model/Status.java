package vn.edu.stu.Model;

public class Status {
    private String status;
    private String status_userid;
    private String timeStamp;

    public Status(String status, String status_userid, String timeStamp) {
        this.status = status;
        this.status_userid = status_userid;
        this.timeStamp = timeStamp;
    }

    public Status() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus_userid() {
        return status_userid;
    }

    public void setStatus_userid(String status_userid) {
        this.status_userid = status_userid;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
