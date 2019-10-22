package sg.edu.ntu.scse.cz2006.gymbuddies.listener;

import android.view.View;

/**
 * @author Chia Yu
 * @since 2019-10-22
 */
public interface OnRecyclerViewClickedListener<T> {
    void onViewClicked(View view, T holder, int action);
}
