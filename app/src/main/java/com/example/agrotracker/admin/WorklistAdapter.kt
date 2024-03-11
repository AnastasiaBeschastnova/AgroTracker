package com.example.agrotracker.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.agrotracker.databinding.ItemWorklistBinding
import java.text.SimpleDateFormat
import java.util.Date


class WorklistAdapter(private val dataSet: Array<WorklistItemModel>, val onItemClicked: (item: WorklistItemModel)->Unit) :
    RecyclerView.Adapter<WorklistAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemWorklistBinding) : RecyclerView.ViewHolder(binding.root)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = ItemWorklistBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = dataSet.getOrNull(position)
        viewHolder.binding.field.text=item?.fieldName
        viewHolder.binding.worktype.text=item?.workType
        viewHolder.binding.culture.text=item?.culture
        viewHolder.binding.fuel.text=item?.fuel
        viewHolder.binding.technic.text=item?.technic
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
        val date: String = simpleDateFormat.format(item?.startTime)
        //println(date)
        //viewHolder.binding.worktime.text=item?.startTime.toString()
        viewHolder.binding.worktime.text=date

        viewHolder.binding.root.setOnClickListener {
            if(item != null)
                onItemClicked.invoke(item)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
