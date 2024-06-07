package com.example.agrotracker.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.agrotracker.databinding.ItemWorklistBinding

class WorklistAdapter(
    private val dataSet: Array<WorklistItemModel>,
    val onItemClicked: (item: WorklistItemModel) -> Unit
) :
    RecyclerView.Adapter<WorklistAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemWorklistBinding) : RecyclerView.ViewHolder(binding.root)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view =
            ItemWorklistBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = dataSet.getOrNull(position)
        viewHolder.binding.workId.text = item?.workId.toString()
        viewHolder.binding.worktype.text = item?.workType
        if (item?.endTime.toString()=="null") {
            viewHolder.binding.endTime.text = "в процессе"
        } else {
            val end_time=convertTime(item?.endTime)
            viewHolder.binding.endTime.text = end_time[0]+" "+end_time[1]//+" (UTC+"+end_time[2]+")"

        }
        viewHolder.binding.fieldName.text = item?.fieldName
        viewHolder.binding.technic.text = item?.technic
        val start_time= convertTime(item?.startTime)

        viewHolder.binding.startTime.text = start_time[0]+" "+start_time[1]//+" (UTC"+start_time[2]+")"
        viewHolder.binding.root.setOnClickListener {
            if (item != null)
                onItemClicked.invoke(item)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}

private fun convertTime(cTime:String?): List<String> {
    //разбиение строки с датой и временем на дату, время и часовой пояс
    val start_time_split=cTime?.split("\"")
    val start_time_value=start_time_split?.get(1)?.split("T")
    val start_date=start_time_value?.get(0)//yyyy-MM-dd
    val start_time_tz=start_time_value?.get(1)//hh:mm:ss.SSSSSS+03:00
    val start_time_tz_split=start_time_tz?.split("+")
    val start_time=start_time_tz_split?.get(0)//hh:mm:ss.SSSSSS
    val time_zone=start_time_tz_split?.get(1)//03:00
    val convertedTime= listOf(start_date.toString(),start_time.toString(),time_zone.toString())
    return convertedTime
}

