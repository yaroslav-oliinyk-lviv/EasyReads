package com.oliinyk.yaroslav.easyreads.domain.util

import android.content.Context
import android.widget.Toast
import com.oliinyk.yaroslav.easyreads.R

object ToastHelper {

    fun show(
        context: Context,
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        Toast.makeText(
            context,
            message,
            duration
        ).show()
    }
}