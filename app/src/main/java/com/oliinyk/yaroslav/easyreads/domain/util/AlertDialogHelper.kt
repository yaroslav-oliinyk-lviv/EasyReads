package com.oliinyk.yaroslav.easyreads.domain.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.oliinyk.yaroslav.easyreads.R

object AlertDialogHelper {

    fun showConfirmationDialog(
        context: Context,
        @StringRes message: Int,
        onClickedOK: () -> Unit
    ) {
        AlertDialog.Builder(context)
//            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(message)
//            .setMessage(message)
            .setPositiveButton(
                context.getString(R.string.confirmation_dialog__button__ok_text)
            ) { dialog, _ ->
                onClickedOK()
                dialog.cancel()
            }
            .setNegativeButton(
                context.getString(R.string.confirmation_dialog__button__cancel_text)
            ) { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }
}