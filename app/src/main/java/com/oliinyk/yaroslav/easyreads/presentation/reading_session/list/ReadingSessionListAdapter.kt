package com.oliinyk.yaroslav.easyreads.presentation.reading_session.list

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.oliinyk.yaroslav.easyreads.R
import com.oliinyk.yaroslav.easyreads.databinding.ListItemReadingSessionBinding
import com.oliinyk.yaroslav.easyreads.domain.model.ReadingSession
import com.oliinyk.yaroslav.easyreads.domain.util.DiffUtilCallbackHelper

class ReadingSessionViewHolder(
    private val binding: ListItemReadingSessionBinding,
    private val context: Context,
    private val onClickedEdit: (ReadingSession) -> Unit,
    private val onClickedRemove: (ReadingSession) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(readingSession: ReadingSession) {
        binding.apply {
            labelReadDate.text = context.getString(
                R.string.reading_session_list_item__label__date_text,
                DateFormat.format(
                    context.getString(R.string.date_and_time_format),
                    readingSession.startedDate
                ).toString()
            )
            labelReadFromToPage.text = context.getString(
                R.string.reading_session_list_item__label__read_end_page_text,
                readingSession.startPage,
                readingSession.endPage
            )
            labelReadTime.text = context.getString(
                R.string.reading_session_list_item__label__read_time_text,
                readingSession.readHours,
                readingSession.readMinutes
            )
            labelReadPagesHour.text = context.getString(
                R.string.reading_session_list_item__label__read_pages_hour_text,
                readingSession.readPagesHour
            )
            labelReadPages.text = context.getString(
                R.string.reading_session_list_item__label__read_pages_text,
                readingSession.readPages
            )

            buttonEdit.setOnClickListener {
                onClickedEdit(readingSession)
            }
            buttonRemove.setOnClickListener {
                onClickedRemove(readingSession)
            }
        }
    }
}

class ReadingSessionListAdapter(
    private var readingSessions: List<ReadingSession> = emptyList(),
    private val onClickedEdit: (ReadingSession) -> Unit,
    private val onClickedRemove: (ReadingSession) -> Unit
) : RecyclerView.Adapter<ReadingSessionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadingSessionViewHolder {
        val binding = ListItemReadingSessionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReadingSessionViewHolder(binding, parent.context, onClickedEdit, onClickedRemove)
    }

    override fun getItemCount(): Int {
        return readingSessions.size
    }

    override fun onBindViewHolder(holder: ReadingSessionViewHolder, position: Int) {
        holder.bind(readingSessions[position])
    }

    fun updateData(newReadingSessions: List<ReadingSession>) {
        val diffUtilCallback = DiffUtilCallbackHelper(this.readingSessions, newReadingSessions)
        val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
        this.readingSessions = newReadingSessions
        diffResult.dispatchUpdatesTo(this)
    }
}