package com.oliinyk.yaroslav.easyreads.presentation.note.list

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.oliinyk.yaroslav.easyreads.R
import com.oliinyk.yaroslav.easyreads.databinding.ListItemNoteBinding
import com.oliinyk.yaroslav.easyreads.domain.model.Note
import com.oliinyk.yaroslav.easyreads.domain.util.DiffUtilCallbackHelper

class NoteHolder(
    private val binding: ListItemNoteBinding,
    private val context: Context,
    private val onClickedEdit: (Note) -> Unit,
    private val onClickedRemove: (Note) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(note: Note) {
        binding.apply {
            labelNoteText.text = note.text

            labelAddedDate.text = DateFormat.format(
                context.getString(R.string.date_and_time_format),
                note.addedDate
            ).toString()

            labelPage.visibility = View.GONE
            note.page?.let {
                labelPage.visibility = View.VISIBLE

                labelPage.text = context.getString(R.string.note_list_item__label__page_text, it)
            }

            buttonEdit.setOnClickListener {
                onClickedEdit(note)
            }
            buttonRemove.setOnClickListener {
                onClickedRemove(note)
            }
        }
    }
}

class NoteListAdapter(
    private var notes: List<Note> = emptyList(),
    private val onClickedEdit: (Note) -> Unit,
    private val onClickedRemove: (Note) -> Unit
) : RecyclerView.Adapter<NoteHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
        val binding = ListItemNoteBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteHolder(binding, parent.context, onClickedEdit, onClickedRemove)
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun onBindViewHolder(holder: NoteHolder, position: Int) {
        holder.bind(notes[position])
    }

    fun updateData(newNotes: List<Note>) {
        val diffUtilCallback = DiffUtilCallbackHelper(this.notes, newNotes)
        val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
        this.notes = newNotes
        diffResult.dispatchUpdatesTo(this)
    }
}