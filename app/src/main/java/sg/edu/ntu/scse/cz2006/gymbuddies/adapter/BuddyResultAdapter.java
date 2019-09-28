package sg.edu.ntu.scse.cz2006.gymbuddies.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import sg.edu.ntu.scse.cz2006.gymbuddies.R;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User;
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.GetProfilePicFromFirebaseAuth;

public class BuddyResultAdapter extends RecyclerView.Adapter<BuddyResultAdapter.ViewHolder> {
    public static final int ACTION_CLICK_ON_ITEM_BODY   = 1;
    public static final int ACTION_CLICK_ON_FAV_ITEM    = 2;
    public static final int ACTION_CLICK_ON_ITEM_PIC = 3;
    public interface OnBuddyClickedListener {
        void onBuddyItemClicked(ViewHolder holder, int action, int position);
        void onBuddyItemCheckChanged(ViewHolder holder, int action, int position, boolean checked);
    }
    private String TAG = "GB.Adapter.BuddyResult";
    private List<User> listBuddies;
    private OnBuddyClickedListener listener;

    public BuddyResultAdapter(List<User> listBuddies) {
        this.listBuddies = listBuddies;
    }

    public void setOnBuddyClickedListener(OnBuddyClickedListener listener){
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return this.listBuddies.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.row_buddy, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setPosition(position);
    }



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        View itemView;
        TextView tvName;
        ImageView imgViewPic, imgViewGender;
        LinearLayout llPrefDays;
        CheckBox cbFav;
        private int position;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView=itemView;
            tvName = itemView.findViewById(R.id.tv_bd_name);
            imgViewPic = itemView.findViewById(R.id.img_bd_pic);
            imgViewGender = itemView.findViewById(R.id.img_bd_gender);
            llPrefDays = itemView.findViewById(R.id.ll_pref_days);
            cbFav = itemView.findViewById(R.id.cb_bd_fav);

            // set up click listener
            itemView.setOnClickListener(this);
            imgViewPic.setOnClickListener(this);
            cbFav.setOnCheckedChangeListener(this);

            // programmingly change profdays
            CheckBox cbDay;
            final float scale = itemView.getContext().getResources().getDisplayMetrics().density;
            int pixels = (int) (36 * scale + 0.5f);
            llPrefDays.getLayoutParams().height = pixels;
            for (int i = 0; i < llPrefDays.getChildCount(); i++) {
                cbDay = (CheckBox) llPrefDays.getChildAt(i);
                cbDay.setText( cbDay.getText().subSequence(0, 1));
                cbDay.setEnabled(false);
                cbDay.setClickable(false);
            }
        }

        public void setPosition(int position){
            this.position = position;
            updateAs(this.position);
        }

        private void updateAs(int position) {
            User curUser = listBuddies.get(position);
            tvName.setText(curUser.getName());
            if (curUser.getGender().equals("Male")) {
                imgViewGender.setImageResource(R.drawable.ic_human_male);
            } else {
                imgViewGender.setImageResource(R.drawable.ic_human_female);
            }
            // TODO: update fav button! (pending on firebase update)

            updatePrefDays(curUser);
            updateProfilePic(curUser);
        }


        private  void updatePrefDays(User user){
            ((CheckBox) llPrefDays.getChildAt(0)).setChecked(user.getPrefDay().getMonday());
            ((CheckBox) llPrefDays.getChildAt(1)).setChecked(user.getPrefDay().getTuesday());
            ((CheckBox) llPrefDays.getChildAt(2)).setChecked(user.getPrefDay().getWednesday());
            ((CheckBox) llPrefDays.getChildAt(3)).setChecked(user.getPrefDay().getThursday());
            ((CheckBox) llPrefDays.getChildAt(4)).setChecked(user.getPrefDay().getFriday());
            ((CheckBox) llPrefDays.getChildAt(5)).setChecked(user.getPrefDay().getSaturday());
            ((CheckBox) llPrefDays.getChildAt(6)).setChecked(user.getPrefDay().getSunday());
        }

        private void updateProfilePic(User user){
            // cache image if needed
            if (user.getProfilePicUri() != null) {
                Activity activity = (Activity) itemView.getContext();
                new GetProfilePicFromFirebaseAuth(activity, new GetProfilePicFromFirebaseAuth.Callback() {
                    @Override
                    public void onComplete(@Nullable Bitmap bitmap) {
                        if (bitmap != null) {
                            RoundedBitmapDrawable roundBitmap = RoundedBitmapDrawableFactory.create(activity.getResources(), bitmap);
                            roundBitmap.setCircular(true);
                            imgViewPic.setImageDrawable(roundBitmap);
                        }
                    }
                }).execute(Uri.parse(user.getProfilePicUri()));
            }
        }

        @Override
        public void onClick(View view) {
            if (view == itemView) {
                if (listener != null) {
                    listener.onBuddyItemClicked(this, ACTION_CLICK_ON_ITEM_BODY, position);
                }
            } else if (view == cbFav){
                if (listener != null) {
                    listener.onBuddyItemClicked(this, ACTION_CLICK_ON_FAV_ITEM, position);
                }
            } else if (view == imgViewPic){
                if (listener != null) {
                    listener.onBuddyItemClicked(this, ACTION_CLICK_ON_ITEM_PIC, position);
                }
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
            if (compoundButton == cbFav){
                if (listener != null){
                    listener.onBuddyItemCheckChanged(this, ACTION_CLICK_ON_FAV_ITEM, position, checked);
                }
            }

        }
    }
}
