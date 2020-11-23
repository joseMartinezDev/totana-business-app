package com.josetotana.totanabusiness.categories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.josetotana.totanabusiness.R
import com.josetotana.totanabusiness.utils.TextFilter


class MyCategoryRecyclerViewAdapter(
    private val values: List<CategoryModel>
) : RecyclerView.Adapter<MyCategoryRecyclerViewAdapter.MyViewHolder>(), Filterable {

    private var mClickListener: ItemClickListener? = null

    var categoryFilterList = mutableListOf<CategoryModel>()

    init {
        categoryFilterList.addAll(values)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = categoryFilterList[position]
        holder.contentView.text = item.name
    }

    override fun getItemCount(): Int = categoryFilterList.size

    fun getItem(id: Int): CategoryModel? {
        return categoryFilterList[id]
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val contentView: TextView = view.findViewById(R.id.content)

        init {
            view.setOnClickListener(this)
        }

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }

        override fun onClick(v: View) {
            mClickListener?.onItemClick(v, adapterPosition)
        }

    }

    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = TextFilter.getNormalizeText(constraint.toString())
                categoryFilterList.clear()
                if (charSearch.isEmpty()) {
                    categoryFilterList.addAll(values)
                } else {
                    val resultList = mutableListOf<CategoryModel>()
                    for (row in values) {
                        if (TextFilter.getNormalizeText(row.name).contains(charSearch)) {
                            resultList.add(row)
                        }
                    }
                    categoryFilterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = categoryFilterList
                return filterResults
            }

//            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                categoryFilterList = results?.values as MutableList<CategoryModel>
                notifyDataSetChanged()
            }

        }
    }

}