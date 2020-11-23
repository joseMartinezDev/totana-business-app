

package com.josetotana.totanabusiness.categoryItems

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.josetotana.totanabusiness.R
import com.josetotana.totanabusiness.utils.TextFilter

class CategoryItemsRecyclerViewAdapter (private val values: List<CategoryItemModel>
) : RecyclerView.Adapter<CategoryItemsRecyclerViewAdapter.MyViewHolder>(), Filterable {

    private var mClickListener: CategoryItemClickListener? = null

    var itemsFilterList = mutableListOf<CategoryItemModel>()

    init {
        itemsFilterList.addAll(values)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_category_item, parent, false)
        return MyViewHolder(view)
    }
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val item = itemsFilterList[position]
            holder.nameView.text = item.name
        }

    fun getItem(id: Int): CategoryItemModel? {
        return itemsFilterList[id]
    }


    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val nameView: TextView = view.findViewById(R.id.itemName)

        init {
            view.setOnClickListener(this)
        }

        override fun toString(): String {
            return super.toString() + " '" + nameView.text + "'"
        }

        override fun onClick(v: View) {
            mClickListener?.onCategoryItemClick(v, adapterPosition)
        }

    }

    fun setClickListener(itemClickListener: CategoryItemClickListener?) {
        mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface CategoryItemClickListener {
        fun onCategoryItemClick(view: View, position: Int)
    }

    override fun getItemCount(): Int = itemsFilterList.size
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = TextFilter.getNormalizeText(constraint.toString())
                itemsFilterList.clear()
                if (charSearch.isEmpty()) {
                    itemsFilterList.addAll(values)
                } else {
                    val resultList = mutableListOf<CategoryItemModel>()
                    for (row in values) {
                        if (TextFilter.getNormalizeText(row.name).contains(charSearch)) {
                            resultList.add(row)
                        }
                    }
                    itemsFilterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = itemsFilterList
                return filterResults
            }

            //            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                itemsFilterList = results?.values as MutableList<CategoryItemModel>
                notifyDataSetChanged()
            }

        }
    }


}