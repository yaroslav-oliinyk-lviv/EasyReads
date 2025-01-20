package com.oliinyk.yaroslav.easyreads.domain.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import androidx.core.view.doOnLayout
import com.oliinyk.yaroslav.easyreads.R
import java.io.File
import kotlin.math.roundToInt

fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {
    // Read in the dimensions of the image on disk
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    val srcWidth = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()

    // Figure out how much to scale down by
    val sampleSize = if (srcHeight <= destHeight && srcWidth <= destWidth) {
        1
    } else {
        val heightScale = srcHeight / destHeight
        val widthScale = srcWidth / destWidth

        minOf(heightScale, widthScale).roundToInt()
    }

    // Read in and create final bitmap
    return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply {
        inSampleSize = sampleSize
    })
}

fun deleteBookCoverImage(context: Context, fileName: String?) {
    fileName?.let {
        val photoFile = File(context.applicationContext.filesDir, it)
        if (photoFile.exists()) {
            photoFile.delete()
        }
    }
}

fun updateBookCoverImage(context: Context, imageView: ImageView, photoFileName: String?) {
    if (imageView.tag != photoFileName) {

        val photoFile = photoFileName?.let {
            File(context.applicationContext.filesDir, it)
        }

        if (photoFile?.exists() == true) {
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.tag = photoFileName
            imageView.setImageURI(Uri.fromFile(photoFile))
        } else {
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            imageView.tag = null
            imageView.setImageURI(null)
        }
    }
}

//fun updateBookCoverImage(context: Context, imageView: ImageView, photoFileName: String?) {
//    if (imageView.tag != photoFileName) {
//
////        imageView.tag?.let { fileName ->
////            if (fileName is String) {
////                deleteBookCoverImage(context, fileName)
////            }
////        }
//
//        val photoFile = photoFileName?.let {
//            File(context.applicationContext.filesDir, it)
//        }
//
//        if (photoFile?.exists() == true) {
//            imageView.doOnLayout { measureView ->
//                val scaledBitmap = getScaledBitmap(
//                    photoFile.path,
//                    measureView.width,
//                    measureView.height
//                )
//                imageView.setImageBitmap(scaledBitmap)
//                imageView.tag = photoFileName
//            }
//            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
//        } else {
//            imageView.setImageBitmap(null)
//            imageView.tag = null
//            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
//        }
//    }
//}