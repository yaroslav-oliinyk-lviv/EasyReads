package com.oliinyk.yaroslav.easyreads.presentation.reading_goal

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.TransitionInflater
import com.oliinyk.yaroslav.easyreads.R
import com.oliinyk.yaroslav.easyreads.databinding.FragmentReadingGoalBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Date

@AndroidEntryPoint
class ReadingGoalFragment : Fragment() {

    private var _binding: FragmentReadingGoalBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            getString(R.string.msg_error__cannot_access_binding)
        }

    private val viewModel: ReadingGoalViewModel by viewModels()

    private lateinit var currentYear: String
    private lateinit var _adapter: ReadingGoalBookGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_in_from_bottom)

        currentYear = DateFormat.format(getString(R.string.date_year_format), Date()).toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReadingGoalBinding
            .inflate(inflater, container, false)
            .apply {
                val spanCount = 4
                listSummeryBooks.layoutManager = GridLayoutManager(context, spanCount)
                _adapter = ReadingGoalBookGridAdapter(
                    holderSize = ReadingGoalGridBookHolder.ReadingGoalGridHolderSize.DEFAULT
                ) { book ->
                    findNavController().navigate(
                        ReadingGoalFragmentDirections.showDetails(book)
                    )
                }
                listSummeryBooks.adapter = _adapter
            }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = getString(
            R.string.reading_goal__toolbar__title_text,
            currentYear
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateUi.collect { stateUi ->
                    updateUi(stateUi)
                }
            }
        }

        setOnClickListeners()
    }

    private fun updateUi(stateUi: ReadingGoalUiState) {
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
                stateUi.readBooksCount,
                stateUi.readingGoals
            )
            progress.progress = if (stateUi.readingGoals > 0) {
                stateUi.readBooksCount * 100 / stateUi.readingGoals
            } else {
                0
            }

            //Summery
            labelSummeryReadAveragePagesHour.text = getString(
                R.string.reading_goal__label__summery_read_average_pages_hour_text,
                stateUi.averagePagesHour
            )

            labelSummeryReadTime.text = getString(
                R.string.reading_goal__label__summery_read_time_text,
                stateUi.readHours,
                stateUi.readMinutes
            )

            labelSummeryReadPages.text = getString(
                R.string.reading_goal__label__summery_read_pages_text,
                stateUi.readPages
            )

            //Books
            labelSummeryBooksTitle.text = getString(
                R.string.reading_goal__label__summery_books_title_text,
                stateUi.readBooksCount
            )
            _adapter.updateData(stateUi.books)
        }
    }

    private fun setOnClickListeners() {
        binding.apply {
            //Goals
            //TODO: Change Your Goal
        }
    }
}