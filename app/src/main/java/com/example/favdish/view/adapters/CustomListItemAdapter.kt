package com.example.favdish.view.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.favdish.databinding.ItemCustomListBinding
import com.example.favdish.view.activities.AddUpdateDishActivity
import com.example.favdish.view.fragments.AllDishesFragment

class CustomListItemAdapter(
    private val activity: Activity,
    private val fragment: Fragment?,
    private val listitems: List<String>,
    private val selection: String)
    : RecyclerView.Adapter<CustomListItemAdapter.ViewHolder>() {

    class ViewHolder(view: ItemCustomListBinding): RecyclerView.ViewHolder(view.root) {
        val tvText = view.tvText
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemCustomListBinding = ItemCustomListBinding.inflate(LayoutInflater.from(
            activity), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listitems[position]
        holder.tvText.text = item

        /*

        As this adapter is used in activity and a fragment, we should check this in our listeners
        to correspond correctly
         */
        holder.itemView.setOnClickListener { //?
            if (activity is AddUpdateDishActivity){
                activity.selectedListItem(item, selection)
            }
            if (fragment is AllDishesFragment){
                fragment.filterSelection(item)
            }
        }
    }

    override fun getItemCount(): Int {
        return listitems.size
    }
}


/*
* Activity in which we are
*Data to work with(items for views)
*What the user selected(as we have one recycler view for 3 fields)
*
*As a param ViewHolder will take a binding object, with which we'll be able to put an item from
* our list of data into view, and then we'll pass it altogether to RecyclerView
*
*
* */