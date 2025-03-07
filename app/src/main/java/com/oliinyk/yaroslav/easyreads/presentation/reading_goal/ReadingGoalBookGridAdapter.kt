package com.oliinyk.yaroslav.easyreads.presentation.reading_goal

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.oliinyk.yaroslav.easyreads.R
import com.oliinyk.yaroslav.easyreads.databinding.ListItemReadingGoalBookBinding
import com.oliinyk.yaroslav.easyreads.databinding.ListItemReadingGoalBookGridBinding
import com.oliinyk.yaroslav.easyreads.domain.model.Book
import com.oliinyk.yaroslav.easyreads.domain.model.BookShelveType
import com.oliinyk.yaroslav.easyreads.domain.util.DiffUtilCallbackHelper
import com.oliinyk.yaroslav.easyreads.domain.util.updateBookCoverImage

class ReadingGoalGridBookHolder(
    private val binding: ListItemReadingGoalBookGridBinding,
    private val context: Context,
    val onClickedBook: (Book) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(book: Book, holderSize: ReadingGoalGridHolderSize) {
        binding.apply {

            if (holderSize == ReadingGoalGridHolderSize.SMALL) {
                val scale = context.resources.displayMetrics.density
                val widthInDp = 74 * scale
                val heightInDp = 111 * scale
                coverImage.layoutParams = coverImage.layoutParams.apply {
                    width = widthInDp.toInt()
                    height = heightInDp.toInt()
                }

            } else if (holderSize == ReadingGoalGridHolderSize.LARGE) {
                val scale = context.resources.displayMetrics.density
                val widthInDp = 120 * scale
                val heightInDp = 180 * scale
                coverImage.layoutParams = coverImage.layoutParams.apply {
                    width = widthInDp.toInt()
                    height = heightInDp.toInt()
                }
            }

            updateBookCoverImage(context, coverImage, book.coverImageFileName)

            root.setOnClickListener {
                onClickedBook(book)
            }
        }
    }

    enum class ReadingGoalGridHolderSize {
        SMALL, DEFAULT, LARGE
    }
}

data class ReadingGoalBookGridAdapter(
    private var books: List<Book> = emptyList(),
    val holderSize: ReadingGoalGridBookHolder.ReadingGoalGridHolderSize,
    private val onClickedBook: (Book) -> Unit
) : RecyclerView.Adapter<ReadingGoalGridBookHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadingGoalGridBookHolder {
        val binding = ListItemReadingGoalBookGridBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ReadingGoalGridBookHolder(binding, parent.context, onClickedBook)
    }

    override fun getItemCount(): Int {
        return books.size
    }

    override fun onBindViewHolder(holder: ReadingGoalGridBookHolder, position: Int) {
        holder.bind(books[position], holderSize)
    }

    fun updateData(newBooks: List<Book>) {
        val diffUtilCallback = DiffUtilCallbackHelper(this.books, newBooks)
        val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
        this.books = newBooks
        diffResult.dispatchUpdatesTo(this)
    }
}