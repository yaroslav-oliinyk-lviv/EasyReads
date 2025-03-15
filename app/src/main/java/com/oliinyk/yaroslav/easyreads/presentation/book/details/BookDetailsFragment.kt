package com.oliinyk.yaroslav.easyreads.presentation.book.details

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
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
import com.oliinyk.yaroslav.easyreads.databinding.FragmentBookDetailsBinding
import com.oliinyk.yaroslav.easyreads.domain.model.BookShelveType
import com.oliinyk.yaroslav.easyreads.domain.model.Note
import com.oliinyk.yaroslav.easyreads.domain.model.ReadingSession
import com.oliinyk.yaroslav.easyreads.domain.util.AlertDialogHelper
import com.oliinyk.yaroslav.easyreads.domain.util.AppConstants.MILLISECONDS_IN_ONE_SECOND
import com.oliinyk.yaroslav.easyreads.domain.util.AppConstants.MINUTES_IN_ONE_HOUR
import com.oliinyk.yaroslav.easyreads.domain.util.AppConstants.SECONDS_IN_ONE_MINUTE
import com.oliinyk.yaroslav.easyreads.domain.util.ToastHelper
import com.oliinyk.yaroslav.easyreads.domain.util.deleteBookCoverImage
import com.oliinyk.yaroslav.easyreads.domain.util.updateBookCoverImage
import com.oliinyk.yaroslav.easyreads.presentation.note.add_edit.NoteAddEditDialogFragment
import com.oliinyk.yaroslav.easyreads.presentation.reading_session.add_edit.ReadingSessionAddEditDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookDetailsFragment : Fragment() {

    private var _binding: FragmentBookDetailsBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            getString(R.string.msg_error__cannot_access_binding)
        }

    private val args: BookDetailsFragmentArgs by navArgs()
    private val viewModel: BookDetailsViewModel by viewModels()
    private lateinit var _menuProvider: MenuProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _menuProvider = createMenuProvider()

        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_in_from_bottom)

        exitTransition = inflater.inflateTransition(R.transition.fade)
        reenterTransition = inflater.inflateTransition(R.transition.fade)

        viewModel.loadBookById(args.book.id)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateUi.collect { uiState ->
                    updateUi(uiState)
                }
            }
        }

        binding.apply {
            // Notes
            buttonNotesSeeAll.setOnClickListener {
                    findNavController().navigate(
                        BookDetailsFragmentDirections.showNotes(viewModel.getCurrentBook().id)
                    )
            }
            buttonNoteAdd.setOnClickListener {
                findNavController().navigate(
                    BookDetailsFragmentDirections.showAddNoteDialog(Note())
                )
            }
            buttonNoteEdit.setOnClickListener {
                if (viewModel.getNotes().isNotEmpty()) {
                    findNavController().navigate(
                        BookDetailsFragmentDirections
                            .showAddNoteDialog(viewModel.getNotes().first())
                    )
                }
            }
            // Start Reading Session
            buttonStartReadingSession.setOnClickListener {
                findNavController().navigate(
                    BookDetailsFragmentDirections.showReadingSessionRecord(
                        viewModel.getCurrentBook()
                    )
                )
            }
            // ReadingSessions
            buttonReadingSessionsSeeAll.setOnClickListener {
                findNavController().navigate(
                    BookDetailsFragmentDirections.showReadingSessions(
                        viewModel.getCurrentBook()
                    )
                )
            }
            buttonReadingSessionAdd.setOnClickListener {
                findNavController().navigate(
                    BookDetailsFragmentDirections.showReadingSessionRecord(
                        viewModel.getCurrentBook()
                    )
                )
            }
            buttonReadingSessionEdit.setOnClickListener {
                if (viewModel.getReadingSessions().isNotEmpty()) {
                    findNavController().navigate(
                        BookDetailsFragmentDirections.showReadingSessionAddEdit(
                            viewModel.getReadingSessions().first()
                        )
                    )
                }
            }
        }

        setFragmentResultListener(NoteAddEditDialogFragment.REQUEST_KEY_NOTE) { _, bundle ->
            val note = bundle.getParcelable(NoteAddEditDialogFragment.BUNDLE_KEY_NOTE) as Note?
            note?.let {
                if (it.bookId == null) {
                    viewModel.addNote(it)

                    ToastHelper.show(
                        requireContext().applicationContext,
                        getString(R.string.book_details__toast__message_new_note_added_text)
                    )
                } else {
                    viewModel.updateNote(it)

                    ToastHelper.show(
                        requireContext().applicationContext,
                        getString(R.string.book_details__toast__message_note_updated_text)
                    )
                }
            }
        }
        setFragmentResultListener(
            ReadingSessionAddEditDialogFragment.REQUEST_KEY_READING_SESSION
        ) { _, bundle ->
            val readingSessionUpdated = bundle.getParcelable(
                ReadingSessionAddEditDialogFragment.BUNDLE_KEY_READING_SESSION
            ) as ReadingSession?

            readingSessionUpdated?.let { readingSession ->
                if (readingSession.bookId == null) {
                    viewModel.addReadingSession(readingSession)
                } else {
                    viewModel.updateReadingSession(readingSession)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        requireActivity().addMenuProvider(_menuProvider)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().removeMenuProvider(_menuProvider)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUi(stateUi: BookDetailsUiState) {
        requireActivity().title = getString(R.string.book_details__toolbar__title_test)

        binding.apply {
            //Book
            updateBookCoverImage(requireContext(), coverImage, stateUi.book.coverImageFileName)

            title.text = stateUi.book.title
            author.text = stateUi.book.author
            updateBookReadTime(stateUi)
            updateBookReadPagesHour(stateUi)
            pages.text = getString(
                R.string.book_details__label__book_pages_text,
                stateUi.book.pageCurrent,
                stateUi.book.pageAmount
            )
            descriptionText.text = stateUi.book.description

            shelve.text = when(stateUi.book.shelve) {
                BookShelveType.WANT_TO_READ -> {
                    getString(R.string.book_details__label__shelve_want_to_read_text)
                }
                BookShelveType.READING -> {
                    getString(R.string.book_details__label__shelve_reading_text)
                }
                BookShelveType.FINISHED -> {
                    getString(R.string.book_details__label__shelve_finished_text)
                }
            }

            progress.max = 100
            val percentage = if (stateUi.book.pageAmount != 0) {
                stateUi.book.pageCurrent * 100 / stateUi.book.pageAmount
            } else {
                0
            }
            progress.progress = percentage
            progressPercentage.text = getString(R.string.book_reading_progress_percentage_text, percentage)

            //Start Reading
            if (stateUi.book.isFinished) {
                split5.visibility = View.GONE
                buttonStartReadingSession.visibility = View.GONE
                labelStartReadingSession.visibility = View.GONE
                buttonReadingSessionAdd.visibility = View.GONE
            }

            //Notes
            buttonNotesSeeAll.text = getString(
                R.string.book_details__button__see_all_notes_text,
                stateUi.notes.size
            )
            if (stateUi.notes.isNotEmpty()) {
                val note = stateUi.notes.first()
                labelNoteText.text = note.text

                labelNoteAddedDate.visibility = View.VISIBLE
                labelNoteAddedDate.text = DateFormat.format(
                    getString(R.string.date_and_time_format),
                    note.addedDate
                ).toString()

                note.page?.let {
                    labelNotePage.visibility = View.VISIBLE
                    labelNotePage.text = getString(R.string.note_list_item__label__page_text, it)
                }
                buttonNoteEdit.visibility = View.VISIBLE
            } else {
                labelNoteAddedDate.visibility = View.GONE
                labelNotePage.visibility = View.GONE
                buttonNoteEdit.visibility = View.GONE
                labelNoteText.text = getString(R.string.note_list__label__no_notes_text)
            }

            //Reading Sessions
            buttonReadingSessionsSeeAll.text = getString(
                R.string.book_details__button__see_all_reading_sessions_text,
                stateUi.readingSessions.size
            )
            if (stateUi.readingSessions.isNotEmpty()) {
                labelNoReadingSession.visibility = View.GONE
                labelReadingSessionReadTime.visibility = View.VISIBLE
                labelReadingSessionReadPagesHour.visibility = View.VISIBLE
                labelReadingSessionReadPages.visibility = View.VISIBLE
                labelReadingSessionReadDate.visibility = View.VISIBLE
                labelReadingSessionReadFromToPage.visibility = View.VISIBLE
                buttonReadingSessionEdit.visibility = View.VISIBLE

                val readingSession = stateUi.readingSessions.first()
                labelReadingSessionReadTime.text = getString(
                    R.string.reading_session_list_item__label__read_time_text,
                    readingSession.readHours,
                    readingSession.readMinutes
                )
                labelReadingSessionReadPagesHour.text = getString(
                    R.string.book_details__label__book_read_pages_hour_text,
                    readingSession.readPagesHour
                )
                labelReadingSessionReadPages.text = getString(
                    R.string.reading_session_list_item__label__read_pages_text,
                    readingSession.readPages
                )
                labelReadingSessionReadDate.text = getString(
                    R.string.reading_session_list_item__label__date_text,
                    DateFormat.format(
                        getString(R.string.date_and_time_format),
                        readingSession.startedDate
                    ).toString()
                )
                labelReadingSessionReadFromToPage.text = getString(
                    R.string.reading_session_list_item__label__read_end_page_text,
                    readingSession.startPage,
                    readingSession.endPage
                )
            } else {
                labelNoReadingSession.visibility = View.VISIBLE
                labelReadingSessionReadTime.visibility = View.GONE
                labelReadingSessionReadPagesHour.visibility = View.GONE
                labelReadingSessionReadPages.visibility = View.GONE
                labelReadingSessionReadDate.visibility = View.GONE
                labelReadingSessionReadFromToPage.visibility = View.GONE
                buttonReadingSessionEdit.visibility = View.GONE
            }
        }
    }

    private fun FragmentBookDetailsBinding.updateBookReadPagesHour(
        stateUi: BookDetailsUiState
    ) {
        var readPagesHour = 0
        if (stateUi.readingSessions.isNotEmpty()) {
            readPagesHour = stateUi.readingSessions
                .map { it.readPagesHour }
                .reduce { acc, i -> acc + i } / stateUi.readingSessions.size
        }
        bookReadAveragePagesHour.text = getString(
            R.string.book_details__label__book_read_average_pages_hour_text,
            readPagesHour
        )
    }

    private fun FragmentBookDetailsBinding.updateBookReadTime(
        stateUi: BookDetailsUiState
    ) {
        var readHours = 0
        var readMinutes = 0
        if (stateUi.readingSessions.isNotEmpty()) {
            val totalReadTimeInSeconds = stateUi.readingSessions
                .map { (it.readTimeInMilliseconds / MILLISECONDS_IN_ONE_SECOND).toInt() }
                .reduce { acc, i -> acc + i }
            val totalReadMinutes = totalReadTimeInSeconds / SECONDS_IN_ONE_MINUTE
            readHours = totalReadMinutes / MINUTES_IN_ONE_HOUR
            readMinutes = totalReadMinutes % MINUTES_IN_ONE_HOUR
        }
        bookReadTime.text = getString(
            R.string.book_details__label__book_read_time_text,
            readHours,
            readMinutes
        )
    }

    private fun createMenuProvider(): MenuProvider {
        return object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_book_details, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_edit_book -> {
                        findNavController().navigate(
                            BookDetailsFragmentDirections.showEditBook(viewModel.getCurrentBook())
                        )
                        true
                    }

                    R.id.menu_remove_book -> {
                        AlertDialogHelper.showConfirmationDialog(
                            requireContext(),
                            R.string.book_details__dialog__message_book_remove_text
                        ) {
                            deleteBookCoverImage(
                                requireContext(),
                                viewModel.getCurrentBook().coverImageFileName
                            )
                            viewModel.removeCurrentBook()
                            findNavController().popBackStack()
                        }
                        true
                    }

                    else -> false
                }
            }

        }
    }
}