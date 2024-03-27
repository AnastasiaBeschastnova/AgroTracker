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
        viewHolder.binding.workId.text=item?.workId.toString()
        viewHolder.binding.worktype.text=item?.workType
        viewHolder.binding.culture.text=item?.culture
        viewHolder.binding.fieldName.text=item?.fieldName
        viewHolder.binding.technic.text=item?.technic
        viewHolder.binding.startTime.text=item?.startTime


        viewHolder.binding.root.setOnClickListener {
            if(item != null)
                onItemClicked.invoke(item)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
