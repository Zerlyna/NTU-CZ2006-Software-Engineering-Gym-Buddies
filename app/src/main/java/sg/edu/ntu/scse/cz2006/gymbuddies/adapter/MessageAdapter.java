package sg.edu.ntu.scse.cz2006.gymbuddies.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import sg.edu.ntu.scse.cz2006.gymbuddies.R;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.ChatMessage;

/**
 * @author Chia Yu
 * @since 2019-10-19
 */
public class MessageAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MSG_SENT = 1;
    private static final int VIEW_TYPE_MSG_RECEIVED = 2;
    private SimpleDateFormat sdf;
    private String sender;
    private ArrayList<ChatMessage> messages;

    public MessageAdapter( ArrayList<ChatMessage> messages, String sender){
        this.messages = messages;
        this.sender = sender;
        this.sdf = new SimpleDateFormat("HH:mm");
    }

    @Override
    public int getItemCount() {
        return this.messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        if (msg.getSender().equals(sender)){
            return VIEW_TYPE_MSG_SENT;
        }else {
            return VIEW_TYPE_MSG_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MSG_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_msg_send, parent, false);
            return new MessageSentViewHolder(view);
        } else if (viewType == VIEW_TYPE_MSG_RECEIVED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_msg_receive, parent, false);
            return new MessageReceivedViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MSG_SENT:
                ((MessageSentViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MSG_RECEIVED:
                ((MessageReceivedViewHolder) holder).bind(message);
        }

    }



    public class MessageSentViewHolder extends RecyclerView.ViewHolder  {
        TextView tvContent, tvTime;

        public MessageSentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_msg_body);
            tvTime = itemView.findViewById(R.id.tv_msg_time);
        }

        public void bind(ChatMessage msg){
            tvContent.setText( msg.getMessage() );
            tvTime.setText( sdf.format(msg.getTimestamp()) );
        }
    }

    public class MessageReceivedViewHolder extends RecyclerView.ViewHolder  {
        TextView tvContent, tvTime;

        public MessageReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_msg_body);
            tvTime = itemView.findViewById(R.id.tv_msg_time);
        }

        public void bind(ChatMessage msg){
            tvContent.setText( msg.getMessage() );
            tvTime.setText( sdf.format(msg.getTimestamp()) );
        }
    }
}
