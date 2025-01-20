package com.oliinyk.yaroslav.easyreads.presentation.note.add_edit

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.oliinyk.yaroslav.easyreads.R
import com.oliinyk.yaroslav.easyreads.databinding.DialogFragmentNoteAddEditBinding

class NoteAddEditDialogFragment : DialogFragment() {

    private var _binding: DialogFragmentNoteAddEditBinding? = null
    private val binding: DialogFragmentNoteAddEditBinding
        get() = checkNotNull(_binding) {
            getString(R.string.msg_error__cannot_access_binding)
        }

    private val args: NoteAddEditDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val noteOld = args.note

        _binding = DialogFragmentNoteAddEditBinding.inflate(
            requireActivity().layoutInflater
        ).apply {
            noteText.setText(noteOld.text)
            noteOld.page?.let {
                notePage.setText(it.toString())
            }
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.AppDialogTheme)
            .setView(binding.root)
            .create()

        binding.apply {
            noteButtonSave.setOnClickListener {

                val pageText = binding.notePage.text?.toString()
                val noteUpdated = if (
                    pageText != null &&
                    !TextUtils.isEmpty(pageText) &&
                    TextUtils.isDigitsOnly(pageText)
                ) {
                    noteOld.copy(
                        text = binding.noteText.text?.toString() ?: "",
                        page = pageText.toInt()
                    )
                } else {
                    noteOld.copy(
                        text = binding.noteText.text?.toString() ?: "",
                        page = null
                    )
                }

                setFragmentResult(
                    REQUEST_KEY_NOTE,
                    bundleOf(BUNDLE_KEY_NOTE to noteUpdated)
                )

                dialog.cancel()
            }
            noteButtonCancel.setOnClickListener {
                dialog.cancel()
            }
        }

        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val REQUEST_KEY_NOTE = "REQUEST_KEY_NOTE"
        const val BUNDLE_KEY_NOTE = "BUNDLE_KEY_NOTE"
    }
}