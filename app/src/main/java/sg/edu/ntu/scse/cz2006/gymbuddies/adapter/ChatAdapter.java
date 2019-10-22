package sg.edu.ntu.scse.cz2006.gymbuddies.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.List;

import sg.edu.ntu.scse.cz2006.gymbuddies.R;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.Chat;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User;
import sg.edu.ntu.scse.cz2006.gymbuddies.listener.OnRecyclerViewClickedListener;
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.GetProfilePicFromFirebaseAuth;

/**
 * @author Chia Yu
 * @since 2019-10-19
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private String TAG = "gb.adapter.chatlist";
    public static final int ACTION_INVALID = -1;
    public static final int ACTION_CLICK_ON_ITEM_BODY = 1;
    public static final int ACTION_CLICK_ON_FAV_ITEM = 2;
    public static final int ACTION_CLICK_ON_ITEM_PIC = 3;
    private SimpleDateFormat sdf;
    private List<Chat> chats;
    private List<String> favUserIds;
    private OnRecyclerViewClickedListener<ChatViewHolder> listener;

    public ChatAdapter(List<Chat> chats, List<String> favUserIds) {
        this.chats = chats;
        this.favUserIds = favUserIds;
        this.sdf = new SimpleDateFormat("dd/MM/yy");
    }

    public void setOnRecyclerViewClickedListener(OnRecyclerViewClickedListener<ChatViewHolder> listener) {
        this.listener = listener;
    }

    public void setFavUserIds(List<String> favUserIds) {
        this.favUserIds = favUserIds;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return this.chats.size();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chat, parent, false);

        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat curChat = chats.get(position);
        holder.bind(curChat);
    }


    public class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvName, tvLastMsg, tvUpdateTime;
        ImageView imgPic;
        CheckBox cbFav;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_bd_name);
            tvLastMsg = itemView.findViewById(R.id.tv_last_msg);
            tvUpdateTime = itemView.findViewById(R.id.tv_last_msg_date);
            imgPic = itemView.findViewById(R.id.img_bd_pic);
            cbFav = itemView.findViewById(R.id.cb_bd_fav);

            itemView.setOnClickListener(this);
            imgPic.setOnClickListener(this);
            cbFav.setOnClickListener(this);
        }

        public void bind(Chat chat) {
            imgPic.setImageResource(R.mipmap.ic_launcher);
            tvName.setText("");
            // update fav
            cbFav.setOnClickListener(null);
            cbFav.setChecked(false);
            if (chat.getOtherUser() != null) {
                Log.d(TAG, "attempt update user");
                tvName.setText(chat.getOtherUser().getName());
                updateProfilePic(chat.getOtherUser());
                if (favUserIds != null && favUserIds.contains(chat.getOtherUser().getUid())) {
                    cbFav.setChecked(true);
                }
            }
            cbFav.setOnClickListener(this);
            tvLastMsg.setText(chat.getLastMessage());
            tvUpdateTime.setText(sdf.format(chat.getLastUpdate()));
        }

        private void updateProfilePic(User user) {
            if (user.getProfilePicUri().equals(imgPic.getTag())) {
                return;
            }

            // cache image if needed
            imgPic.setImageResource(R.mipmap.ic_launcher);
            if (user.getProfilePicUri() != null) {
                Activity activity = (Activity) itemView.getContext();
                new GetProfilePicFromFirebaseAuth(activity, new GetProfilePicFromFirebaseAuth.Callback() {
                    @Override
                    public void onComplete(@Nullable Bitmap bitmap) {
                        if (bitmap != null) {
                            RoundedBitmapDrawable roundBitmap = RoundedBitmapDrawableFactory.create(activity.getResources(), bitmap);
                            roundBitmap.setCircular(true);
                            imgPic.setImageDrawable(roundBitmap);
                            imgPic.setTag(user.getProfilePicUri());
                        }
                    }
                }).execute(Uri.parse(user.getProfilePicUri()));
            }
        }

        @Override
        public void onClick(View v) {
            int action = ACTION_INVALID;
            if (v == super.itemView) {
                action = ACTION_CLICK_ON_ITEM_BODY;
            } else if (v == cbFav) {
                action = ACTION_CLICK_ON_FAV_ITEM;
            } else if (v == imgPic) {
                action = ACTION_CLICK_ON_ITEM_PIC;
            }

            if (action != ACTION_INVALID) {
                if (listener != null) {
                    listener.onViewClicked(v, this, action);
                }
            }
        }
    }


}
