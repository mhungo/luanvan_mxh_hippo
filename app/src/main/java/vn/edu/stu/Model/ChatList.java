package vn.edu.stu.Model;

public class ChatList {
    private String chatlist_id;
    private String chatlist_lastmessage_timestamp;

    public ChatList(String chatlist_id, String chatlist_lastmessage_timestamp) {
        this.chatlist_id = chatlist_id;
        this.chatlist_lastmessage_timestamp = chatlist_lastmessage_timestamp;
    }

    public ChatList() {
    }

    public String getChatlist_id() {
        return chatlist_id;
    }

    public void setChatlist_id(String chatlist_id) {
        this.chatlist_id = chatlist_id;
    }

    public String getChatlist_lastmessage_timestamp() {
        return chatlist_lastmessage_timestamp;
    }

    public void setChatlist_lastmessage_timestamp(String chatlist_lastmessage_timestamp) {
        this.chatlist_lastmessage_timestamp = chatlist_lastmessage_timestamp;
    }

}
