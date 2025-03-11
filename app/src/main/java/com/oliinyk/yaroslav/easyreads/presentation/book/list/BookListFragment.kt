package com.oliinyk.yaroslav.easyreads.presentation.book.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.oliinyk.yaroslav.easyreads.R
import com.oliinyk.yaroslav.easyreads.databinding.FragmentBookListBinding
import com.oliinyk.yaroslav.easyreads.domain.model.Book
import com.oliinyk.yaroslav.easyreads.domain.model.BookSortingType
import com.oliinyk.yaroslav.easyreads.domain.model.BookSortingOrderType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookListFragment : Fragment() {

    private var _binding: FragmentBookListBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            getString(R.string.msg_error__cannot_access_binding)
        }

    private val viewModel: BookListViewModel by viewModels()
    private lateinit var _menuProvider: MenuProvider

    private lateinit var _adapter: BookListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _menuProvider = createMenuProvider()

        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_in_from_bottom)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookListBinding
            .inflate(inflater, container, false)
            .apply {
                bookList.layoutManager = LinearLayoutManager(context)
                _adapter = BookListAdapter(
                    holderSize = viewModel.stateUi.value.holderSize
                ) { book ->
                    findNavController().navigate(
                        BookListFragmentDirections.showDetails(book)
                    )
                }
                bookList.adapter = _adapter
            }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinnerSortBy()
        setupButtonSortOrder()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateUi.collect { stateUi ->
                    updateUi(stateUi)
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

    private fun updateUi(stateUi: StateUiBookList) {
        requireActivity().title = getString(
            R.string.book_list__toolbar__title_text,
            stateUi.books.size
        )

        if (_adapter.holderSize == stateUi.holderSize) {
            updateListItemsWithKeepingScrollPosition(stateUi)
        } else {
            _adapter = _adapter.copy(holderSize = stateUi.holderSize)
            binding.bookList.adapter = _adapter
        }

        binding.apply {
            if (spinnerSortBy.selectedItemPosition != stateUi.bookSorting.bookSortingType.ordinal) {
                spinnerSortBy.setSelection(stateUi.bookSorting.bookSortingType.ordinal)
            }
        }
    }

    private fun updateListItemsWithKeepingScrollPosition(stateUi: StateUiBookList) {
        if (stateUi.books.isNotEmpty()) {
            binding.apply {
                val linearLayoutManager = bookList.layoutManager as LinearLayoutManager
                val scrollPosition = linearLayoutManager.findFirstVisibleItemPosition()
                val offsetView = bookList.getChildAt(0)
                val offset = offsetView?.top ?: 0

                _adapter.updateData(stateUi.books)

                linearLayoutManager.scrollToPositionWithOffset(scrollPosition, offset)
            }
        } else {
            _adapter.updateData(stateUi.books)
        }
    }

    private fun createMenuProvider(): MenuProvider {
        return object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_book_list, menu)

                when (viewModel.stateUi.value.holderSize) {
                    BookHolder.HolderSize.LARGE -> {
                        menu.findItem(R.id.menu_select_large)?.setChecked(true)
                    }

                    BookHolder.HolderSize.DEFAULT -> {
                        menu.findItem(R.id.menu_select_default)?.setChecked(true)
                    }

                    BookHolder.HolderSize.SMALL -> {
                        menu.findItem(R.id.menu_select_small)?.setChecked(true)
                    }
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_add_book -> {
                        findNavController().navigate(
                            BookListFragmentDirections.showAddBook(Book())
                        )
                        true
                    }

                    R.id.menu_select_large -> {
                        viewModel.updateHolderSize(BookHolder.HolderSize.LARGE)
                        menuItem.setChecked(true)
                        true
                    }

                    R.id.menu_select_default -> {
                        viewModel.updateHolderSize(BookHolder.HolderSize.DEFAULT)
                        menuItem.setChecked(true)
                        true
                    }

                    R.id.menu_select_small -> {
                        viewModel.updateHolderSize(BookHolder.HolderSize.SMALL)
                        menuItem.setChecked(true)
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun setupSpinnerSortBy() {
        binding.apply {
            val bookSortingTypes = listOf(
                getString(
                    R.string.book_list__button__sorted_by_text,
                    getString(R.string.book_list__button__sorted_by_title_text)
                ),
                getString(
                    R.string.book_list__button__sorted_by_text,
                    getString(R.string.book_list__button__sorted_by_author_text)
                ),
                getString(
                    R.string.book_list__button__sorted_by_text,
                    getString(R.string.book_list__button__sorted_by_added_date_text)
                ),
                getString(
                    R.string.book_list__button__sorted_by_text,
                    getString(R.string.book_list__button__sorted_by_updated_date_text)
                )
            )
            val bookSortingTypeArrayAdapter = ArrayAdapter<String>(
                requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                bookSortingTypes
            )
            spinnerSortBy.adapter = bookSortingTypeArrayAdapter
            spinnerSortBy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.updateBookSorting(
                        viewModel.bookSorting.copy(
                            bookSortingType = BookSortingType.entries[position]
                        )
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    /* no-op */
                }
            }
        }
    }

    private fun setupButtonSortOrder() {
        binding.apply {
            buttonSortOrder.setOnClickListener {
                if (viewModel.bookSorting.bookSortingOrderType == BookSortingOrderType.DESC) {
                    viewModel.updateBookSorting(viewModel.bookSorting.copy(bookSortingOrderType = BookSortingOrderType.ASC))
                } else {
                    viewModel.updateBookSorting(viewModel.bookSorting.copy(bookSortingOrderType = BookSortingOrderType.DESC))
                }
            }
        }
    }
}