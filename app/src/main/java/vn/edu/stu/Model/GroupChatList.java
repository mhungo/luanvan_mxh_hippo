package vn.edu.stu.Model;

public class GroupChatList {
    String groupId, groupTitle, groupDecription, groupIcon, timstamp, createBy;

    public GroupChatList() {
    }

    public GroupChatList(String groupId, String groupTitle, String groupDecription, String groupIcon, String timstamp, String createBy) {
        this.groupId = groupId;
        this.groupTitle = groupTitle;
        this.groupDecription = groupDecription;
        this.groupIcon = groupIcon;
        this.timstamp = timstamp;
        this.createBy = createBy;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupTitle() {
        return groupTitle;
    }

    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

    public String getGroupDecription() {
        return groupDecription;
    }

    public void setGroupDecription(String groupDecription) {
        this.groupDecription = groupDecription;
    }

    public String getGroupIcon() {
        return groupIcon;
    }

    public void setGroupIcon(String groupIcon) {
        this.groupIcon = groupIcon;
    }

    public String getTimstamp() {
        return timstamp;
    }

    public void setTimstamp(String timstamp) {
        this.timstamp = timstamp;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }
}
