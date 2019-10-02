package sg.edu.ntu.scse.cz2006.gymbuddies.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import sg.edu.ntu.scse.cz2006.gymbuddies.R

/**
 * Created by Kenneth on 17/9/2019.
 * for sg.edu.ntu.scse.cz2006.gymbuddies.adapter in Gym Buddies!
 */
class StringRecyclerAdapter(string: List<String>, private var announce: Boolean) : RecyclerView.Adapter<StringRecyclerAdapter.StringViewHolder>() {
    constructor(string: List<String>) : this(string, true)

    private var stringList: List<String> = ArrayList()
    private var onClickListener: View.OnClickListener? = null

    init {
        stringList = string
    }

    fun setOnClickListener(listener: View.OnClickListener) {
        onClickListener = listener
    }

    override fun getItemCount(): Int {
        return stringList.size
    }

    override fun onBindViewHolder(holder: StringViewHolder, position: Int) {
        val s = stringList[position]
        holder.title.text = s
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StringViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_simple_list_item_1, parent, false)
        return StringViewHolder(itemView, onClickListener)
    }

    inner class StringViewHolder(v: View, listener: View.OnClickListener?) : RecyclerView.ViewHolder(v), View.OnClickListener {
        var title: TextView = v.findViewById(android.R.id.text1)

        init {
            v.setOnClickListener(listener ?: this)
            v.tag = this
        }

        override fun onClick(p0: View?) {
            if (announce) p0?.let { Toast.makeText(it.context, title.text, Toast.LENGTH_SHORT).show() }
        }

    }

}