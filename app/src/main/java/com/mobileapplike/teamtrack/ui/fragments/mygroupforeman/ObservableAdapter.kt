package com.mobileapplike.teamtrack.ui.fragments.mygroupforeman


import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mobileapplike.teamtrack.Person

import com.mobileapplike.teamtrack.R
import com.mobileapplike.teamtrack.utils.TrackingUtility.inflate

import kotlinx.android.extensions.LayoutContainer


class ObservableAdapter : RecyclerView.Adapter<ObservableAdapter.ObservableViewHolder>() {

    private val data: MutableList<Person> = mutableListOf()
    private var onItemSelectedListener: (Person) -> Unit = {}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObservableAdapter.ObservableViewHolder {
        return ObservableViewHolder(inflate(parent, R.layout.list_item))
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ObservableAdapter.ObservableViewHolder, position: Int) {
        holder.bindModel(data[position])
    }

    inner class ObservableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), LayoutContainer {
        override val containerView: View?
            get() = itemView

        init {
            //cv_observable_root.setOnClickListener {
            //    onItemSelectedListener(data[adapterPosition])
            //}
        }

        fun bindModel(model: Person) {
            containerView!!.findViewById<TextView>(R.id.display_name).text = model.nickName
            containerView!!.findViewById<TextView>(R.id.phone_number).text = model.id
            //if (model.visibility.equals("true")){
                containerView!!.findViewById<ImageView>(R.id.imageView3).setImageResource(R.drawable.ic_location_on_black_24dp)
            //} else {
            //    containerView!!.findViewById<ImageView>(R.id.imageView3).setImageResource(R.drawable.ic_location_off_black_24dp)
            //}

        }
    }

    fun removeItem(position: Int) {
        data.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, data.size)
    }

    fun setData(newData: List<Person>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return data[oldItemPosition].id == newData[newItemPosition].id
            }

            override fun getOldListSize(): Int {
                return data.size
            }

            override fun getNewListSize(): Int {
                return newData.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return data[oldItemPosition] == newData[newItemPosition]
            }
        }
        DiffUtil.calculateDiff(diffCallback, true).dispatchUpdatesTo(this)
        data.apply {
            clear()
            addAll(newData)
        }
    }

}