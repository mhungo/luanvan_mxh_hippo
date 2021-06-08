package vn.edu.stu.Model;

public class GroupChatList implements Comparable<GroupChatList> {
    private String groupId, groupTitle, groupDecription, groupIcon, createBy;
    private long timstamp;


    public GroupChatList() {
    }

    public GroupChatList(String groupId, String groupTitle, String groupDecription, String groupIcon, String createBy, long timstamp) {
        this.groupId = groupId;
        this.groupTitle = groupTitle;
        this.groupDecription = groupDecription;
        this.groupIcon = groupIcon;
        this.createBy = createBy;
        this.timstamp = timstamp;
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

    public long getTimstamp() {
        return timstamp;
    }

    public void setTimstamp(long timstamp) {
        this.timstamp = timstamp;
    }

    @Override
    public int compareTo(GroupChatList o) {
        return Long.compare(this.timstamp, o.timstamp);
    }
}
