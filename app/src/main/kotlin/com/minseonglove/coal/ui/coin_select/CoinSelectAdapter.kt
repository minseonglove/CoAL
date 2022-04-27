package com.minseonglove.coal.ui.coin_select

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.minseonglove.coal.R
import com.minseonglove.coal.databinding.RecyclerCoinListBinding

class CoinSelectAdapter(
    private var coinList: List<String>,
    private val itemClick: (String) -> Unit
) : RecyclerView.Adapter<CoinSelectAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: RecyclerCoinListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): CoinSelectAdapter.ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.recycler_coin_list, parent, false)
        return ViewHolder(RecyclerCoinListBinding.bind(view))
    }

    override fun onBindViewHolder(holder: CoinSelectAdapter.ViewHolder, position: Int) {
        with(holder.binding) {
            textviewCoinList.text = coinList[position]
            constraintlayoutCoinList.setOnClickListener {
                itemClick(coinList[position])
            }
        }
    }

    override fun getItemCount() = coinList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(items: List<String>) {
        coinList = items
        notifyDataSetChanged()
    }
}
