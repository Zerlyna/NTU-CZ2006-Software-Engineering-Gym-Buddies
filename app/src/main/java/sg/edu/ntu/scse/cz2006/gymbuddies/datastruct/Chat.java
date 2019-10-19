package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct;

public class Chat {
    private String lastMessage;
    private long lastUpdate;
    private String[] participant;

    public Chat(){}

    public Chat(String lastMessage, long lastUpdate, String[] participant) {
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

    public String[] getParticipant() {
        return participant;
    }

    public void setParticipant(String[] participant) {
        this.participant = participant;
    }
}
