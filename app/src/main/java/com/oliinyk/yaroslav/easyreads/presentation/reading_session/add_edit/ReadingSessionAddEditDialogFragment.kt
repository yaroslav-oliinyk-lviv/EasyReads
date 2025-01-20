package com.oliinyk.yaroslav.easyreads.presentation.reading_session.add_edit

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.oliinyk.yaroslav.easyreads.R
import com.oliinyk.yaroslav.easyreads.databinding.DialogFragmentReadingSessionAddEditBinding
import com.oliinyk.yaroslav.easyreads.domain.model.ReadingSessionRecordStatusType
import com.oliinyk.yaroslav.easyreads.domain.util.AppConstants.MILLISECONDS_IN_ONE_SECOND
import com.oliinyk.yaroslav.easyreads.domain.util.AppConstants.SECONDS_IN_ONE_HOUR
import com.oliinyk.yaroslav.easyreads.domain.util.AppConstants.SECONDS_IN_ONE_MINUTE

class ReadingSessionAddEditDialogFragment : DialogFragment() {

    private var _binding: DialogFragmentReadingSessionAddEditBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            getString(R.string.msg_error__cannot_access_binding)
        }

    private val args: ReadingSessionAddEditDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var readingSession = args.readingSession

        _binding = DialogFragmentReadingSessionAddEditBinding.inflate(
            requireActivity().layoutInflater
        ).apply {
            labelStartPage.text = readingSession.startPage.toString()
            if (readingSession.endPage != 0) {
                editEndPage.setText(readingSession.endPage.toString())
            }

            spinnerReadHour.setSelection(readingSession.readHours)
            spinnerReadMinutes.setSelection(readingSession.readMinutes)
            spinnerReadSeconds.setSelection(readingSession.readSeconds)
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.AppDialogTheme)
            .setView(binding.root)
            .create()

        binding.apply {
            buttonSave.setOnClickListener {
                editEndPage.text?.let { endPageEditable ->
                    val endPageText = endPageEditable.toString()
                    if (endPageText.isNotEmpty() && endPageText.isDigitsOnly()) {
                        val endPageInt = endPageText.toInt()
                        readingSession = readingSession.copy(
                            endPage = endPageInt,
                            readPages = endPageInt - readingSession.startPage
                        )
                    }
                }

                if (readingSession.endPage <= readingSession.startPage) {
                    editEndPage.error = getString(R.string.reading_session_add_edit_dialog__error__message_text)
                    return@setOnClickListener
                }

                val readHours = spinnerReadHour.selectedItemPosition * SECONDS_IN_ONE_HOUR
                val readMinutes = spinnerReadMinutes.selectedItemPosition * SECONDS_IN_ONE_MINUTE
                val readSeconds = spinnerReadSeconds.selectedItemPosition
                readingSession = readingSession.copy(
                    readTimeInMilliseconds = (readHours + readMinutes + readSeconds).toLong() * MILLISECONDS_IN_ONE_SECOND
                )
                if (readingSession.recordStatus != ReadingSessionRecordStatusType.FINISHED) {
                    readingSession = readingSession.copy(
                        recordStatus = ReadingSessionRecordStatusType.FINISHED
                    )
                }

                setFragmentResult(
                    REQUEST_KEY_READING_SESSION,
                    bundleOf(BUNDLE_KEY_READING_SESSION to readingSession)
                )

                dialog.cancel()
            }
            buttonCancel.setOnClickListener {
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
        const val REQUEST_KEY_READING_SESSION = "REQUEST_KEY_READING_SESSION"
        const val BUNDLE_KEY_READING_SESSION = "BUNDLE_KEY_READING_SESSION"
    }
}