package vn.edu.stu.Model;

public class GroupChatList {
    private String groudchatlist_groupid,
            groudchatlist_grouptitle,
            groudchatlist_groupdecription,
            groudchatlist_groupicon,
            groudchatlist_createby,
            groudchatlist_lastmessagetimestamp,
            groudchatlist_timestamp;

    public GroupChatList(String groudchatlist_groupid, String groudchatlist_grouptitle, String groudchatlist_groupdecription, String groudchatlist_groupicon, String groudchatlist_createby, String groudchatlist_lastmessagetimestamp, String groudchatlist_timestamp) {
        this.groudchatlist_groupid = groudchatlist_groupid;
        this.groudchatlist_grouptitle = groudchatlist_grouptitle;
        this.groudchatlist_groupdecription = groudchatlist_groupdecription;
        this.groudchatlist_groupicon = groudchatlist_groupicon;
        this.groudchatlist_createby = groudchatlist_createby;
        this.groudchatlist_lastmessagetimestamp = groudchatlist_lastmessagetimestamp;
        this.groudchatlist_timestamp = groudchatlist_timestamp;
    }

    public GroupChatList() {
    }

    public String getGroudchatlist_groupid() {
        return groudchatlist_groupid;
    }

    public void setGroudchatlist_groupid(String groudchatlist_groupid) {
        this.groudchatlist_groupid = groudchatlist_groupid;
    }

    public String getGroudchatlist_grouptitle() {
        return groudchatlist_grouptitle;
    }

    public void setGroudchatlist_grouptitle(String groudchatlist_grouptitle) {
        this.groudchatlist_grouptitle = groudchatlist_grouptitle;
    }

    public String getGroudchatlist_groupdecription() {
        return groudchatlist_groupdecription;
    }

    public void setGroudchatlist_groupdecription(String groudchatlist_groupdecription) {
        this.groudchatlist_groupdecription = groudchatlist_groupdecription;
    }

    public String getGroudchatlist_groupicon() {
        return groudchatlist_groupicon;
    }

    public void setGroudchatlist_groupicon(String groudchatlist_groupicon) {
        this.groudchatlist_groupicon = groudchatlist_groupicon;
    }

    public String getGroudchatlist_createby() {
        return groudchatlist_createby;
    }

    public void setGroudchatlist_createby(String groudchatlist_createby) {
        this.groudchatlist_createby = groudchatlist_createby;
    }

    public String getGroudchatlist_lastmessagetimestamp() {
        return groudchatlist_lastmessagetimestamp;
    }

    public void setGroudchatlist_lastmessagetimestamp(String groudchatlist_lastmessagetimestamp) {
        this.groudchatlist_lastmessagetimestamp = groudchatlist_lastmessagetimestamp;
    }

    public String getGroudchatlist_timestamp() {
        return groudchatlist_timestamp;
    }

    public void setGroudchatlist_timestamp(String groudchatlist_timestamp) {
        this.groudchatlist_timestamp = groudchatlist_timestamp;
    }

    @Override
    public String toString() {
        return groudchatlist_grouptitle;
    }

}
