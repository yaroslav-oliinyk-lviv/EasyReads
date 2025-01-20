package com.oliinyk.yaroslav.easyreads.presentation.reading_session.record

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.oliinyk.yaroslav.easyreads.R
import com.oliinyk.yaroslav.easyreads.databinding.FragmentReadingSessionRecordBinding
import com.oliinyk.yaroslav.easyreads.domain.model.Note
import com.oliinyk.yaroslav.easyreads.domain.model.ReadingSession
import com.oliinyk.yaroslav.easyreads.domain.model.ReadingSessionRecordStatusType
import com.oliinyk.yaroslav.easyreads.domain.service.ReadTimeCounterService
import com.oliinyk.yaroslav.easyreads.domain.util.ToastHelper
import com.oliinyk.yaroslav.easyreads.domain.util.updateBookCoverImage
import com.oliinyk.yaroslav.easyreads.presentation.note.add_edit.NoteAddEditDialogFragment
import com.oliinyk.yaroslav.easyreads.presentation.reading_session.add_edit.ReadingSessionAddEditDialogFragment.Companion.BUNDLE_KEY_READING_SESSION
import com.oliinyk.yaroslav.easyreads.presentation.reading_session.add_edit.ReadingSessionAddEditDialogFragment.Companion.REQUEST_KEY_READING_SESSION
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReadingSessionRecordFragment : Fragment() {

    private var _binding: FragmentReadingSessionRecordBinding? = null
    private val binding: FragmentReadingSessionRecordBinding
        get() = checkNotNull(_binding) {
            getString(R.string.msg_error__cannot_access_binding)
        }

    private val args: ReadingSessionRecordFragmentArgs by navArgs()
    private val viewModel: ReadingSessionRecordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_in_from_bottom)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().startService(
                Intent(requireContext(), ReadTimeCounterService::class.java).apply {
                    action = ReadTimeCounterService.Actions.STOP.toString()
                }
            )
            viewModel.removeUnfinishedReadingSession()
            findNavController().popBackStack()
        }

        viewModel.loadLastUnfinishedByBookId(args.book)

        requireActivity().startService(
            Intent(requireContext(), ReadTimeCounterService::class.java).apply {
                action = ReadTimeCounterService.Actions.START.toString()
                putExtra("bookId", args.book.id.toString())
                putExtra("bookTitle", args.book.title)
                putExtra("pageCurrent", args.book.pageCurrent)
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReadingSessionRecordBinding
            .inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = getString(R.string.reading_session_record__toolbar__title_text)

        setOnClickListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateUi.collect { stateUi ->
                    updateUi(stateUi)
                }
            }
        }

        setFragmentResultListener(
            REQUEST_KEY_READING_SESSION
        ) { _, bundle ->
            val readingSessionUpdated =
                bundle.getParcelable(BUNDLE_KEY_READING_SESSION) as ReadingSession?
            readingSessionUpdated?.let { readingSession ->
                requireActivity().startService(
                    Intent(requireContext(), ReadTimeCounterService::class.java).apply {
                        action = ReadTimeCounterService.Actions.STOP.toString()
                    }
                )
                viewModel.save(readingSession)
            }
            viewLifecycleOwner.lifecycleScope.launch {
                delay(250)
                findNavController().popBackStack()
            }
        }
        setFragmentResultListener(NoteAddEditDialogFragment.REQUEST_KEY_NOTE) { _, bundle ->
            val note = bundle.getParcelable(NoteAddEditDialogFragment.BUNDLE_KEY_NOTE) as Note?
            note?.let {
                viewModel.addNote(it)

                ToastHelper.show(
                    requireContext().applicationContext,
                    getString(R.string.book_details__toast__message_new_note_added_text)
                )
            }
        }
    }

    private fun setOnClickListeners() {
        binding.apply {
            buttonStartPauseRecording.setOnClickListener {
                viewModel.resumeOrPause { resumeOrPauseAction ->
                    requireActivity().startService(
                        Intent(requireContext(), ReadTimeCounterService::class.java).apply {
                            action = resumeOrPauseAction.toString()
                        }
                    )
                }
            }
            buttonFinishRecording.setOnClickListener {
                requireActivity().startService(
                    Intent(requireContext(), ReadTimeCounterService::class.java).apply {
                        action = ReadTimeCounterService.Actions.PAUSE.toString()
                    }
                )
                viewModel.currentReadingSession?.let { readingSession ->
                    findNavController().navigate(
                        ReadingSessionRecordFragmentDirections.showReadingSessionAddEdit(
                            readingSession
                        )
                    )
                }
            }

            buttonShowNotes.setOnClickListener {
                findNavController().navigate(
                    ReadingSessionRecordFragmentDirections.showNotes(
                        viewModel.currentBook.id
                    )
                )
            }
            buttonAddNote.setOnClickListener {
                findNavController().navigate(
                    ReadingSessionRecordFragmentDirections.showEditNoteDialog(
                        Note(bookId = viewModel.currentBook.id)
                    )
                )
            }
        }
    }

    private fun updateUi(stateUi: ReadingSessionRecordStateUi) {
        updateBookInfo(stateUi)
        stateUi.readingSession?.let { readingSession ->
            updateReadTimeCounter(readingSession)
            updateStartStopButton(readingSession)
        }
    }

    private fun updateBookInfo(stateUi: ReadingSessionRecordStateUi) {
        binding.apply {
            stateUi.book?.let { book ->
                if (labelTitle.text != book.title) {
                    labelTitle.text = book.title
                    labelAuthor.text = book.author
                    labelReadPages.text = getString(
                        R.string.reading_session_record__label__read_pages_text,
                        stateUi.book.pageCurrent,
                        stateUi.book.pageAmount
                    )
                    updateBookCoverImage(
                        requireContext(),
                        coverImage,
                        book.coverImageFileName
                    )
                }
            }
        }
    }

    private fun updateReadTimeCounter(readingSession: ReadingSession) {
        binding.apply {
            labelReadTime.text = getString(
                R.string.reading_session_record__label__read_time_text,
                readingSession.readHours,
                readingSession.readMinutes,
                readingSession.readSeconds
            )
        }
    }

    private fun updateStartStopButton(readingSession: ReadingSession) {
        binding.apply {
            when (readingSession.recordStatus) {
                ReadingSessionRecordStatusType.STARTED -> {
                    if (labelRecordStatus.text != getString(
                            R.string.reading_session_record__label__record_status_started_text
                        )
                    ) {
                        buttonStartPauseRecording.setImageResource(R.drawable.ic_button_record_pause)
                        labelRecordStatus.text = getString(
                            R.string.reading_session_record__label__record_status_started_text
                        )
                    }
                }

                else -> {
                    buttonStartPauseRecording.setImageResource(R.drawable.ic_button_record_start)
                    labelRecordStatus.text = getString(
                        R.string.reading_session_record__label__record_status_paused_text
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}