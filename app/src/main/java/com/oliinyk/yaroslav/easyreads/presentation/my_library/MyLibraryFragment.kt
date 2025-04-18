package com.oliinyk.yaroslav.easyreads.presentation.my_library

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.oliinyk.yaroslav.easyreads.R
import com.oliinyk.yaroslav.easyreads.databinding.FragmentMyLibraryBinding
import com.oliinyk.yaroslav.easyreads.domain.model.Book
import com.oliinyk.yaroslav.easyreads.domain.model.BookShelveType
import com.oliinyk.yaroslav.easyreads.presentation.book.list.BookHolder
import com.oliinyk.yaroslav.easyreads.presentation.book.list.BookListFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Date

@AndroidEntryPoint
class MyLibraryFragment : Fragment() {

    private var _binding: FragmentMyLibraryBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            getString(R.string.msg_error__cannot_access_binding)
        }

    private val viewModel: MyLibraryViewModel by viewModels()
    private lateinit var _menuProvider: MenuProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _menuProvider = createMenuProvider()

        val inflater = TransitionInflater.from(requireContext())
        exitTransition = inflater.inflateTransition(R.transition.fade)
        reenterTransition = inflater.inflateTransition(R.transition.fade)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyLibraryBinding
            .inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = getString(R.string.my_library__toolbar__title_text)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateUi.collect { stateUi ->
                    updateUi(stateUi)
                }
            }
        }

        setOnClickListeners()
    }

    private fun updateUi(stateUi: MyLibraryUiState) {
        binding.apply {
            labelGoalsTitle.text = getString(
                R.string.reading_goal__label__goal_title_text,
                DateFormat.format(
                    getString(R.string.date_year_format),
                    Date()
                ).toString()
            )
            labelGoalsReadingProgress.text = getString(
                R.string.reading_goal__label__goal_reading_progress_text,
                stateUi.currentYearFinishedBooksCount,
                stateUi.readingGoals
            )

            progress.progress = if (stateUi.readingGoals > 0) {
                stateUi.currentYearFinishedBooksCount * 100 / stateUi.readingGoals
            } else {
                0
            }

            shelveFinished.text = getString(
                R.string.my_library__label__shelve_finished_text,
                stateUi.finishedCount
            )
            shelveReading.text = getString(
                R.string.my_library__label__shelve_reading_text,
                stateUi.readingCount
            )
            shelveWantToRead.text = getString(
                R.string.my_library__label__shelve_want_to_read_text,
                stateUi.wantToReadCount
            )
            shelveSeeAllBooks.text = getString(
                R.string.my_library__label__shelve_see_all_books,
                stateUi.allCount
            )
        }
    }

    private fun setOnClickListeners() {
        binding.apply {
            //Goals
            containerReadingGoal.setOnClickListener {
                findNavController().navigate(
                    MyLibraryFragmentDirections.showReadingGoal()
                )
            }

            //Shelves
            shelveFinished.setOnClickListener {
                findNavController().navigate(
                    MyLibraryFragmentDirections.showBooks(BookShelveType.FINISHED)
                )
            }
            shelveReading.setOnClickListener {
                findNavController().navigate(
                    MyLibraryFragmentDirections.showBooks(BookShelveType.READING)
                )
            }
            shelveWantToRead.setOnClickListener {
                findNavController().navigate(
                    MyLibraryFragmentDirections.showBooks(BookShelveType.WANT_TO_READ)
                )
            }
            shelveSeeAllBooks.setOnClickListener {
                findNavController().navigate(
                    MyLibraryFragmentDirections.showBooks(null)
                )
            }
        }
    }

    private fun createMenuProvider(): MenuProvider {
        return object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_my_library, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_add_book -> {
                        findNavController().navigate(
                            MyLibraryFragmentDirections.showAddBook(Book())
                        )
                        true
                    }

                    else -> false
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
}