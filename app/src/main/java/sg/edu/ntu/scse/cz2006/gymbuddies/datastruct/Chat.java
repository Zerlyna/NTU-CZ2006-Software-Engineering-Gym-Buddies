package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct;

import com.google.firebase.firestore.Exclude;

import java.util.HashMap;


/**
 * @author Chia Yu
 * @since 2019-10-22
 */
public class Chat {
    private String lastMessage="";
    private long lastUpdate=0;
    private HashMap<String, Boolean> participant = new HashMap<>();

    String chatId;
    User otherUser;

    public Chat(){}

    public Chat(String lastMessage, long lastUpdate, HashMap<String, Boolean> participant) {
        this.lastMessage = lastMessage;
        this.lastUpdate = lastUpdate;
        this.participant = participant;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public HashMap<String, Boolean> getParticipant() {
        return participant;
    }

    public void setParticipant(HashMap<String, Boolean> participant) {
        this.participant = participant;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    @Exclude
    public User getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(User otherUser) {
        this.otherUser = otherUser;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "lastMessage='" + lastMessage + '\'' +
                ", lastUpdate=" + lastUpdate +
                ", participant=" + participant +
                ", chatId='" + chatId + '\'' +
                ", otherUser=" + otherUser +
                '}';
    }
}
