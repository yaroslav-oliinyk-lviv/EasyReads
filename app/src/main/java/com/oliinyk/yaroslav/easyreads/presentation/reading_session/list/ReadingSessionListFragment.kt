package com.oliinyk.yaroslav.easyreads.presentation.reading_session.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.oliinyk.yaroslav.easyreads.R
import com.oliinyk.yaroslav.easyreads.databinding.FragmentReadingSessionListBinding
import com.oliinyk.yaroslav.easyreads.domain.model.ReadingSession
import com.oliinyk.yaroslav.easyreads.domain.util.AlertDialogHelper
import com.oliinyk.yaroslav.easyreads.presentation.reading_session.add_edit.ReadingSessionAddEditDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReadingSessionListFragment : Fragment() {

    private var _binding: FragmentReadingSessionListBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            getString(R.string.msg_error__cannot_access_binding)
        }

    private val args: ReadingSessionListFragmentArgs by navArgs()
    private val viewModel: ReadingSessionListViewModel by viewModels()

    private val _adapter: ReadingSessionListAdapter by lazy {
        ReadingSessionListAdapter(
            onClickedEdit = { readingSession ->
                findNavController().navigate(
                    ReadingSessionListFragmentDirections.showReadingSessionAddEdit(
                        readingSession
                    )
                )
            },
            onClickedRemove = { readingSession ->
                AlertDialogHelper.showConfirmationDialog(
                    requireContext(),
                    R.string.reading_session_list__dialog__confirmation_remove_message_text
                ) {
                    viewModel.remove(readingSession)
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_in_from_bottom)

        viewModel.loadReadingSessionsByBookId(args.book)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReadingSessionListBinding
            .inflate(inflater, container, false)
            .apply {
                readingSessionList.layoutManager = LinearLayoutManager(requireContext())
                readingSessionList.adapter = _adapter
            }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateUi.collect {
                    updateUi(it)
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

    private fun updateUi(stateUi: ReadingSessionListUiState) {
        requireActivity().title = getString(
            R.string.reading_session_list__toolbar__title_text,
            stateUi.readingSessions.size
        )

        updateListItemsWithKeepingScrollPosition(stateUi.readingSessions)

        if (stateUi.readingSessions.isNotEmpty()) {
            binding.labelListEmpty.visibility = View.GONE
        } else {
            binding.labelListEmpty.visibility = View.VISIBLE
        }
    }

    private fun updateListItemsWithKeepingScrollPosition(readingSessions: List<ReadingSession>) {
        if (readingSessions.isNotEmpty()) {
            binding.apply {
                val linearLayoutManager = readingSessionList.layoutManager as LinearLayoutManager
                val scrollPosition = linearLayoutManager.findFirstVisibleItemPosition()
                val offsetView = readingSessionList.getChildAt(0)
                val offset = offsetView?.top ?: 0

                _adapter.updateData(readingSessions)

                linearLayoutManager.scrollToPositionWithOffset(scrollPosition, offset)
            }
        } else {
            _adapter.updateData(readingSessions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}