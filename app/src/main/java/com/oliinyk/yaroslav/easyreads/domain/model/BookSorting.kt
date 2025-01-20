package com.oliinyk.yaroslav.easyreads.domain.model

data class BookSorting(
    val bookSortingType: BookSortingType = BookSortingType.ADDED_DATE,
    val bookSortingOrderType: BookSortingOrderType = BookSortingOrderType.DESC
) {

    override fun toString(): String {
        return "${bookSortingType};${bookSortingOrderType}"
    }

    companion object {
        fun fromString(bookSortingString: String): BookSorting {
            val strings = bookSortingString.split(';')
            return BookSorting(
                bookSortingType = BookSortingType.valueOf(strings[0]),
                bookSortingOrderType = BookSortingOrderType.valueOf(strings[1])
            )
        }
    }
}
