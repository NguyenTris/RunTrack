package com.example.runtrack.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.R

class FaqAdapter(
    private var faqList: List<FaqItem>
) : RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

    class FaqViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tvFaqCategory)
        val tvQuestion: TextView = view.findViewById(R.id.tvFaqQuestion)
        val tvIndicator: TextView = view.findViewById(R.id.tvExpandIndicator)
        val bodyAnswer: View = view.findViewById(R.id.bodyFaqAnswer)
        val tvAnswer: TextView = view.findViewById(R.id.tvFaqAnswer)
        val headerLayout: View = view.findViewById(R.id.headerFaq)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hud_faq, parent, false)
        return FaqViewHolder(view)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val item = faqList[position]
        holder.tvCategory.text = item.category
        holder.tvQuestion.text = item.question
        holder.tvAnswer.text = item.answer

        if (item.isExpanded) {
            holder.bodyAnswer.visibility = View.VISIBLE
            holder.tvIndicator.text = "▲"
        } else {
            holder.bodyAnswer.visibility = View.GONE
            holder.tvIndicator.text = "▼"
        }

        holder.headerLayout.setOnClickListener {
            item.isExpanded = !item.isExpanded
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = faqList.size

    fun updateList(newList: List<FaqItem>) {
        faqList = newList
        notifyDataSetChanged()
    }
}
