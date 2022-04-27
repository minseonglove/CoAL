package com.minseonglove.coal.ui.coin_select

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.minseonglove.coal.R
import com.minseonglove.coal.databinding.RecyclerCoinListBinding

class CoinSelectAdapter(
    private val itemClick: (String) -> Unit
) : ListAdapter<String, CoinSelectAdapter.ViewHolder>(diffUtil) {

    class ViewHolder(val binding: RecyclerCoinListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String){
            binding.textviewCoinList.text = item
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.recycler_coin_list, parent, false)
        return ViewHolder(RecyclerCoinListBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            bind(getItem(position))
            binding.constraintlayoutCoinList.setOnClickListener {
                itemClick(getItem(position))
            }
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem

        }

    }

}
