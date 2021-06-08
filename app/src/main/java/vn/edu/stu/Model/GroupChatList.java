package vn.edu.stu.Model;

public class GroupChatList implements Comparable<GroupChatList> {
    private String groupId, groupTitle, groupDecription, groupIcon, createBy, lastMessageTimestamp, timstamp;

    public GroupChatList(String groupId, String groupTitle, String groupDecription, String groupIcon, String createBy, String lastMessageTimestamp, String timstamp) {
        this.groupId = groupId;
        this.groupTitle = groupTitle;
        this.groupDecription = groupDecription;
        this.groupIcon = groupIcon;
        this.createBy = createBy;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.timstamp = timstamp;
    }

    public GroupChatList() {
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

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(String lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getTimstamp() {
        return timstamp;
    }

    public void setTimstamp(String timstamp) {
        this.timstamp = timstamp;
    }

    @Override
    public int compareTo(GroupChatList o) {
        return (int) (Long.parseLong(lastMessageTimestamp) - Long.parseLong(o.lastMessageTimestamp));
    }
}
