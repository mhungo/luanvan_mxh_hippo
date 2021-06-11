package vn.edu.stu.Model;

public class Status {
    private String status_status;
    private String status_userid;
    private String status_timestamp;

    public Status(String status_status, String status_userid, String status_timestamp) {
        this.status_status = status_status;
        this.status_userid = status_userid;
        this.status_timestamp = status_timestamp;
    }

    public Status() {
    }

    public String getStatus_status() {
        return status_status;
    }

    public void setStatus_status(String status_status) {
        this.status_status = status_status;
    }

    public String getStatus_userid() {
        return status_userid;
    }

    public void setStatus_userid(String status_userid) {
        this.status_userid = status_userid;
    }

    public String getStatus_timestamp() {
        return status_timestamp;
    }

    public void setStatus_timestamp(String status_timestamp) {
        this.status_timestamp = status_timestamp;
    }
}
