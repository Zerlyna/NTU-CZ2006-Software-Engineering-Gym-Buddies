package sg.edu.ntu.scse.cz2006.gymbuddies.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    private String TAG = "GB.Adapter.BuddyResult";
    private List<User> listBuddies;

    // TODO: add custom click listener to allow more precise click action on different component of single view
    public BuddyResultAdapter(List<User> listBuddies) {
        this.listBuddies = listBuddies;
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
        return new ViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get item
        User item = listBuddies.get(position);

        holder.tvName.setText(item.getName());
        holder.tvLiveRegion.setText(item.getPrefLocation());
        if (item.getGender().equals("Male")){
            holder.imgGender.setImageResource(R.drawable.ic_human_male);
        } else{
            holder.imgGender.setImageResource(R.drawable.ic_human_female);
        }

        // TODO: cache image if needed
        if (item.getProfilePicUri() != null){
            Activity activity = (Activity) holder.itemView.getContext();
            new GetProfilePicFromFirebaseAuth(activity, new GetProfilePicFromFirebaseAuth.Callback() {
                @Override
                public void onComplete(@Nullable Bitmap bitmap) {
                    Log.d(TAG, "Profile Pic("+position+") -> "+bitmap);
                    if (bitmap != null){
                        RoundedBitmapDrawable roundBitmap = RoundedBitmapDrawableFactory.create(activity.getResources(), bitmap);
                        roundBitmap.setCircular(true);
                        holder.imgPic.setImageDrawable(roundBitmap);
                    }
                }
            }).execute(  Uri.parse(item.getProfilePicUri()) );
        }
    }



    public class ViewHolder extends RecyclerView.ViewHolder  {
        TextView tvName, tvLiveRegion;
        ImageView imgPic, imgGender;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_bd_name);
            tvLiveRegion = itemView.findViewById(R.id.tv_bd_region);
            imgPic = itemView.findViewById(R.id.img_bd_pic);
            imgGender = itemView.findViewById(R.id.img_bd_gender);
        }
    }
}
