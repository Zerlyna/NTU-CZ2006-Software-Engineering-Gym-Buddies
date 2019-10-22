package sg.edu.ntu.scse.cz2006.gymbuddies.util;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import sg.edu.ntu.scse.cz2006.gymbuddies.R;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User;

/**
 * @author Chia Yu
 * @since 2019-10-22
 */
public class DialogHelper {

    public static Dialog displayBuddyProfile(Context context, User user, Drawable drawable){
        // inflate dialog layout
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.dialog_bd_profile, null);

        ImageView imgPic = view.findViewById(R.id.profile_pic);
        TextView tvName = view.findViewById(R.id.tv_bd_name);
        TextView tvLocation = view.findViewById(R.id.tv_pref_location);
        TextView tvTime = view.findViewById(R.id.tv_pref_time);
        LinearLayout llPrefDays = view.findViewById(R.id.ll_pref_days);

        imgPic.setImageDrawable(drawable);
        tvName.setText(user.getName());
        tvLocation.setText(user.getPrefLocation());
        tvTime.setText(user.getPrefTime());

        Drawable drawableLeft;
        if (user.getGender().equals("Male")) {
            drawableLeft = context.getResources().getDrawable(R.drawable.ic_human_male);
        } else {
            drawableLeft = context.getResources().getDrawable(R.drawable.ic_human_female);
        }
        tvName.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);

        for (int i =0; i<llPrefDays.getChildCount(); i++){
            CheckBox cb = (CheckBox) llPrefDays.getChildAt(i);
            cb.setEnabled(false);
            cb.setText(cb.getText().subSequence(0,1));
        }
        ((CheckBox) llPrefDays.getChildAt(0)).setChecked(user.getPrefDay().getMonday());
        ((CheckBox) llPrefDays.getChildAt(1)).setChecked(user.getPrefDay().getTuesday());
        ((CheckBox) llPrefDays.getChildAt(2)).setChecked(user.getPrefDay().getWednesday());
        ((CheckBox) llPrefDays.getChildAt(3)).setChecked(user.getPrefDay().getThursday());
        ((CheckBox) llPrefDays.getChildAt(4)).setChecked(user.getPrefDay().getFriday());
        ((CheckBox) llPrefDays.getChildAt(5)).setChecked(user.getPrefDay().getSaturday());
        ((CheckBox) llPrefDays.getChildAt(6)).setChecked(user.getPrefDay().getSunday());


        // build & display dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        return builder.setTitle("Profile")
                .setView(view)
                .setPositiveButton("Cancel",null)
                .show();
    }
}
