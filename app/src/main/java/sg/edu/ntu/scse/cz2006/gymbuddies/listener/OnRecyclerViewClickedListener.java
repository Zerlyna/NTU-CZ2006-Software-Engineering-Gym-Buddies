package sg.edu.ntu.scse.cz2006.gymbuddies.listener;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @author Chia Yu
 * @since 2019-10-22
 */
public interface OnRecyclerViewClickedListener<T extends RecyclerView.ViewHolder> {
    void onViewClicked(View view, T holder, int action);
}
