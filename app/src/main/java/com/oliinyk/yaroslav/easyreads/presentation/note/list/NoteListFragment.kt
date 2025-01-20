package com.oliinyk.yaroslav.easyreads.presentation.note.list

import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.oliinyk.yaroslav.easyreads.R
import com.oliinyk.yaroslav.easyreads.databinding.FragmentNoteListBinding
import com.oliinyk.yaroslav.easyreads.domain.model.Note
import com.oliinyk.yaroslav.easyreads.domain.util.AlertDialogHelper
import com.oliinyk.yaroslav.easyreads.presentation.note.add_edit.NoteAddEditDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoteListFragment : Fragment() {

    private var _binding: FragmentNoteListBinding? = null
    private val binding: FragmentNoteListBinding
        get() = checkNotNull(_binding) {
            getString(R.string.msg_error__cannot_access_binding)
        }

    private val args: NoteListFragmentArgs by navArgs()
    private lateinit var _menuProvider: MenuProvider

    private val viewModel: NoteListViewModel by viewModels()

    private val _adapter: NoteListAdapter by lazy {
        NoteListAdapter(
            onClickedEdit = { note ->
                findNavController().navigate(
                    NoteListFragmentDirections.showEditNoteDialog(note)
                )
            },
            onClickedRemove = { note ->
                AlertDialogHelper.showConfirmationDialog(
                    requireContext(),
                    R.string.note_list__dialog__confirmation_note_remove_message_text
                ) {
                    viewModel.remove(note)
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _menuProvider = createMenuProvider()

        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_in_from_bottom)

        viewModel.loadNotes(args.bookId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNoteListBinding
            .inflate(inflater, container, false)
            .apply {
                noteList.layoutManager = LinearLayoutManager(context)
                noteList.adapter = _adapter
            }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateUi.collect { notes ->
                    updateUi(notes)
                }
            }
        }

        setFragmentResultListener(
            NoteAddEditDialogFragment.REQUEST_KEY_NOTE
        ) { _, bundle ->
            val note = bundle.getParcelable(NoteAddEditDialogFragment.BUNDLE_KEY_NOTE) as Note?
            note?.let {
                if (it.bookId == null) {
                    viewModel.addNote(note.copy(bookId = args.bookId))
                } else {
                    viewModel.update(it)
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

    private fun createMenuProvider(): MenuProvider {
        return object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_note_list, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_add_note -> {
                        findNavController().navigate(
                            NoteListFragmentDirections.showEditNoteDialog(Note())
                        )
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun updateUi(notes: List<Note>) {
        requireActivity().title = getString(
            R.string.note_list__toolbar__title_text,
            notes.size
        )

        updateListItemsWithKeepingScrollPosition(notes)
        
        if (notes.isNotEmpty()) {
            binding.listEmptyText.visibility = View.GONE
        } else {
            binding.listEmptyText.visibility = View.VISIBLE
        }
    }

    private fun updateListItemsWithKeepingScrollPosition(notes: List<Note>) {
        if (notes.isNotEmpty()) {
            binding.apply {
                val linearLayoutManager = noteList.layoutManager as LinearLayoutManager
                val scrollPosition = linearLayoutManager.findFirstVisibleItemPosition()
                val offsetView = noteList.getChildAt(0)
                val offset = offsetView?.top ?: 0

                _adapter.updateData(notes)

                linearLayoutManager.scrollToPositionWithOffset(scrollPosition, offset)
            }
        } else {
            _adapter.updateData(notes)
        }
    }
}