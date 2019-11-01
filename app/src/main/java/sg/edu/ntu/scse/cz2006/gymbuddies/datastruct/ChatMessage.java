package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct;


/**
 *  Created by Chia Yu on 22/10/2019.
 *  for sg.edu.ntu.scse.cz2006.gymbuddies.datastruct in Gym Buddies!
 *
 *  Entity class to hold chat message between user.
 *
 * @author Chia Yu
 * @since 2019-10-22
 */
public class ChatMessage {
    private String message;
    private String sender;
    private long timestamp;

    public ChatMessage(){}

    public ChatMessage(String message, String sender, long timestamp) {
        this.message = message;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public ChatMessage(String message, String sender) {
        this.message = message;
        this.sender = sender;
        this.timestamp = System.currentTimeMillis();
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
