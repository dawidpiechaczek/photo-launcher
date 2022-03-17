package com.appsirise.photolauncher

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val IMAGE_CHOOSER_TYPE = "image/*"
private const val TEMP_PHOTO_NAME = "temp.jpg"
private const val FILE_NAME_PATTERN = "yyyyMMdd_HHmmss"

class PhotoLauncherProvider {
    private var takePhotoLauncher: ActivityResultLauncher<Intent>? = null
    private var galleryPhotoLauncher: ActivityResultLauncher<Intent>? = null

    fun setupTakePhotoLauncher(
        fragment: Fragment,
        onSuccessPhotoLoad: ((Uri) -> Unit)? = null,
        onFailedPhotoLoad: (() -> Unit)? = null
    ) {
        takePhotoLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            if (activityResult.resultCode != Activity.RESULT_OK) {
                onFailedPhotoLoad?.invoke()
            } else {
                val fileUri = getCameraTempFileUri(fragment.requireContext())
                onSuccessPhotoLoad?.invoke(fileUri)
            }
        }
    }

    fun launchCapturePhoto(context: Context) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, getCameraTempFileUri(context))
        }
        takePhotoLauncher?.launch(intent)
    }

    fun setupGalleryPhotoLauncher(
        fragment: Fragment,
        onSuccessPhotoLoad: ((Uri?) -> Unit)? = null,
        onFailedPhotoLoad: (() -> Unit)? = null
    ) {
        galleryPhotoLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            if (activityResult.resultCode != Activity.RESULT_OK) {
                onFailedPhotoLoad?.invoke()
            } else {
                val fileUri = activityResult.data?.data
                onSuccessPhotoLoad?.invoke(fileUri)
            }
        }
    }

    fun launchGallerySelector() {
        val intent = Intent().apply {
            type = IMAGE_CHOOSER_TYPE
            action = Intent.ACTION_GET_CONTENT
        }
        galleryPhotoLauncher?.launch(intent)
    }

    private fun getCameraTempFileUri(context: Context): Uri {
        val file = File(context.cacheDir, TEMP_PHOTO_NAME).also { it.createNewFile() }
        return context.getUriForFile(file)
    }

    private fun generatePhotoFileName(): String {
        val timeStamp = SimpleDateFormat(FILE_NAME_PATTERN, Locale.ROOT).format(Date())
        return "FILE_$timeStamp"
    }

    private fun Context.getUriForFile(file: File): Uri = FileProvider.getUriForFile(
        this,
        applicationContext.packageName + ".provider",
        file
    )
}

fun Uri.convertToBitmap(context: Context): Bitmap? {
    val contentResolver: ContentResolver = context.contentResolver
    return try {
        if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(contentResolver, this)
        } else {
            val source: ImageDecoder.Source =
                ImageDecoder.createSource(contentResolver, this)
            ImageDecoder.decodeBitmap(source)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
