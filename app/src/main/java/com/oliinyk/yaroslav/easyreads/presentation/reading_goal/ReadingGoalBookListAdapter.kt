package com.oliinyk.yaroslav.easyreads.presentation.reading_goal

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.oliinyk.yaroslav.easyreads.R
import com.oliinyk.yaroslav.easyreads.databinding.ListItemReadingGoalBookBinding
import com.oliinyk.yaroslav.easyreads.domain.model.Book
import com.oliinyk.yaroslav.easyreads.domain.model.BookShelveType
import com.oliinyk.yaroslav.easyreads.domain.util.DiffUtilCallbackHelper
import com.oliinyk.yaroslav.easyreads.domain.util.updateBookCoverImage

class ReadingGoalBookHolder(
    private val binding: ListItemReadingGoalBookBinding,
    private val context: Context,
    val onClickedBook: (Book) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(book: Book, holderSize: ReadingGoalHolderSize) {
        binding.apply {

            if (holderSize == ReadingGoalHolderSize.SMALL) {
                val scale = context.resources.displayMetrics.density
                val widthInDp = 74 * scale
                val heightInDp = 111 * scale
                coverImage.layoutParams = coverImage.layoutParams.apply {
                    width = widthInDp.toInt()
                    height = heightInDp.toInt()
                }

                title.textSize = 13.toFloat()
                title.maxLines = 2

            } else if (holderSize == ReadingGoalHolderSize.LARGE) {
                val scale = context.resources.displayMetrics.density
                val widthInDp = 120 * scale
                val heightInDp = 180 * scale
                coverImage.layoutParams = coverImage.layoutParams.apply {
                    width = widthInDp.toInt()
                    height = heightInDp.toInt()
                }

                title.maxLines = 4
                author.textSize = 15.toFloat()
                author.maxLines = 2
            }

            title.text = book.title
            author.text = book.author
            shelve.text = context.getString(
                R.string.book_details__label__shelve_text,
                when (book.shelve) {
                    BookShelveType.WANT_TO_READ -> {
                        context.getString(R.string.book_details__label__shelve_want_to_read_text)
                    }

                    BookShelveType.READING -> {
                        context.getString(R.string.book_details__label__shelve_reading_text)
                    }

                    BookShelveType.FINISHED -> {
                        context.getString(R.string.book_details__label__shelve_finished_text)
                    }
                }
            )
            pages.text = context.getString(
                R.string.book_details__label__book_pages_text,
                book.pageCurrent,
                book.pageAmount
            )
            progress.max = 100
            val percentage = if (book.pageAmount != 0) {
                book.pageCurrent * 100 / book.pageAmount
            } else {
                0
            }
            progress.progress = percentage
            progressPercentage.text =
                context.getString(R.string.book_reading_progress_percentage_text, percentage)
            updateBookCoverImage(context, coverImage, book.coverImageFileName)

            root.setOnClickListener {
                onClickedBook(book)
            }
        }
    }

    enum class ReadingGoalHolderSize {
        SMALL, DEFAULT, LARGE
    }
}

data class ReadingGoalBookListAdapter(
    private var books: List<Book> = emptyList(),
    val holderSize: ReadingGoalBookHolder.ReadingGoalHolderSize,
    private val onClickedBook: (Book) -> Unit
) : RecyclerView.Adapter<ReadingGoalBookHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadingGoalBookHolder {
        val binding = ListItemReadingGoalBookBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ReadingGoalBookHolder(binding, parent.context, onClickedBook)
    }

    override fun getItemCount(): Int {
        return books.size
    }

    override fun onBindViewHolder(holder: ReadingGoalBookHolder, position: Int) {
        holder.bind(books[position], holderSize)
    }

    fun updateData(newBooks: List<Book>) {
        val diffUtilCallback = DiffUtilCallbackHelper(this.books, newBooks)
        val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
        this.books = newBooks
        diffResult.dispatchUpdatesTo(this)
    }
}