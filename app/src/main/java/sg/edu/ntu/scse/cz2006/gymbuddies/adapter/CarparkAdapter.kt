package sg.edu.ntu.scse.cz2006.gymbuddies.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import sg.edu.ntu.scse.cz2006.gymbuddies.R
import sg.edu.ntu.scse.cz2006.gymbuddies.data.CarPark

/**
 * Recycler Adapter for Carpark Results
 * For sg.edu.ntu.scse.cz2006.gymbuddies.adapter in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-10-21
 * @property carparkList List<Pair<CarPark, Float>> The list of all carparks near gym
 * @property onClickListener OnClickListener? Set this to override the default onClick listener defined in [CarparkAdapter.CarparkViewHolder.onClick]
 * @constructor Creates a adapter for the Gym Favourites List RecyclerView
 */
class CarparkAdapter(carpark: List<Pair<CarPark, Float>>) : RecyclerView.Adapter<CarparkAdapter.CarparkViewHolder>() {

    private var carparkList: List<Pair<CarPark, Float>> = ArrayList()
    private var onClickListener: View.OnClickListener? = null

    init {
        this.carparkList = carpark
    }

    /**
     * Overrides the default on click [listener]
     * @param listener OnClickListener Custom onclick listener. Set to null to revert back to default defined in [CarparkAdapter.CarparkViewHolder.onClick]
     */
    fun setOnClickListener(listener: View.OnClickListener) {
        onClickListener = listener
    }

    /**
     * Gets the number of gyms stored in this adapter
     * @return Int Number of gyms in this adapter
     */
    override fun getItemCount(): Int {
        return carparkList.size
    }

    /**
     * Gets the gym list stored in this adapter
     * @return List<FavGymObject> Gym List object
     */
    fun getList(): List<Pair<CarPark, Float>> { return carparkList }

    /**
     * Updates the gym list stored in this adapter
     * @param newList List<FavGymObject> New Gym List object
     */
    fun updateList(newList: List<Pair<CarPark, Float>>) { this.carparkList = newList }

    /**
     * Internal function to bind the view [holder] at the current RecyclerView [position] to a specific data
     * @param holder FavViewHolder Holder to store the data
     * @param position Int Positing in the RecyclerView
     */
    override fun onBindViewHolder(holder: CarparkViewHolder, position: Int) {
        val s = carparkList[position]
        holder.title.text = s.first.address
        holder.description.text = "${s.first.id} | ${s.second} m away"
        holder.cpObj = s.first
        holder.distance = s.second
    }

    /**
     * Creates the required View Holder to store the data in
     * @param parent ViewGroup The parent view (ignored)
     * @param viewType Int The type of view to create (ignored)
     * @return FavViewHolder The view holder that is created to store the data in
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarparkViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_carpark, parent, false)
        return CarparkViewHolder(itemView, onClickListener)
    }

    /**
     * Inner class to store the carpark in
     *
     * @author Kenneth Soh
     * @since 2019-09-17
     * @property title TextView Carpark Address
     * @property description TextView Carpark ID/Distance
     * @property distance Float Distance from gym
     * @property cpObj CarPark? Carpark Object
     * @constructor Creates a view holder to hold the view required for RecyclerView
     */
    inner class CarparkViewHolder(v: View, listener: View.OnClickListener?) : RecyclerView.ViewHolder(v), View.OnClickListener {
        var title: TextView = v.findViewById(R.id.cp_title)
        var description: TextView = v.findViewById(R.id.cp_desc)
        var cpObj: CarPark? = null
        var distance: Float = 0f

        init {
            v.setOnClickListener(listener ?: this)
            v.tag = this
        }

        /**
         * Default onclick handler
         * @param p0 View View context object
         */
        override fun onClick(p0: View?) {
            p0?.let { Toast.makeText(it.context, title.text, Toast.LENGTH_SHORT).show() }
        }

    }

}