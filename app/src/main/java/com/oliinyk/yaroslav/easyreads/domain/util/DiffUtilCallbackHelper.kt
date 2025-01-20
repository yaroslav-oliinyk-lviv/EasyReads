package com.oliinyk.yaroslav.easyreads.domain.util

import androidx.recyclerview.widget.DiffUtil
import com.oliinyk.yaroslav.easyreads.domain.model.BaseModel

class DiffUtilCallbackHelper(
    private val oldList: List<BaseModel>,
    private val newList: List<BaseModel>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id.compareTo(newList[newItemPosition].id) == 0
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}