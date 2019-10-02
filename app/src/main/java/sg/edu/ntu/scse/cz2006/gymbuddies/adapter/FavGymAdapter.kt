package sg.edu.ntu.scse.cz2006.gymbuddies.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.recyclerview.widget.RecyclerView
import sg.edu.ntu.scse.cz2006.gymbuddies.R
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.GymList

/**
 * Created by Kenneth on 17/9/2019.
 * for sg.edu.ntu.scse.cz2006.gymbuddies.adapter in Gym Buddies!
 */
class FavGymAdapter(gyms: List<GymList.GymShell>) : RecyclerView.Adapter<FavGymAdapter.FavViewHolder>() {

    private var gymList: List<GymList.GymShell> = ArrayList()
    private var onClickListener: View.OnClickListener? = null

    init {
        this.gymList = gyms
    }

    fun setOnClickListener(listener: View.OnClickListener) {
        onClickListener = listener
    }

    override fun getItemCount(): Int {
        return gymList.size
    }

    override fun onBindViewHolder(holder: FavViewHolder, position: Int) {
        val s = gymList[position]
        holder.title.text = s.properties.Name
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclew_item_gym_detail, parent, false)
        return FavViewHolder(itemView, onClickListener)
    }

    inner class FavViewHolder(v: View, listener: View.OnClickListener?) : RecyclerView.ViewHolder(v), View.OnClickListener {
        var title: TextView = v.findViewById(R.id.fav_list_title)
        var rating: AppCompatRatingBar = v.findViewById(R.id.fav_list_rating)
        var ratingCount: TextView = v.findViewById(R.id.fav_list_rating_count)
        var ratingAvg: TextView = v.findViewById(R.id.fav_list_rating_avg)
        var favCount: TextView = v.findViewById(R.id.fav_list_favourites)

        init {
            v.setOnClickListener(listener ?: this)
            v.tag = this
        }

        override fun onClick(p0: View?) {
            p0?.let { Toast.makeText(it.context, title.text, Toast.LENGTH_SHORT).show() }
        }

    }

}