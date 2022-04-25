package com.minseonglove.coal.ui.coin_select

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.minseonglove.coal.R
import com.minseonglove.coal.databinding.RecyclerCoinListBinding

class CoinSelectAdapter(private var coinList: List<String>)
    : RecyclerView.Adapter<CoinSelectAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: RecyclerCoinListBinding):
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): CoinSelectAdapter.ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.recycler_coin_list, parent, false)
        return ViewHolder(RecyclerCoinListBinding.bind(view))
    }

    override fun onBindViewHolder(holder: CoinSelectAdapter.ViewHolder, position: Int) {
        holder.binding.textviewCoinList.text = coinList[position]
    }

    override fun getItemCount(): Int = coinList.size

    /*
    fun updateItems(items: List<String>) {
        coinList = items
        notifyDataSetChanged()
    }
     */
}