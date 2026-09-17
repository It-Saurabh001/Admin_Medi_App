package com.saurabh.mediadminapp.utils.utilityFunctions

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Extension function to convert a content Uri into a MultipartBody.Part
 * for Retrofit file uploads.
 */
fun Uri.toMultipartBodyPart(context: Context, partName: String): MultipartBody.Part? {
    return try {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(this) ?: return null
        val mimeType = contentResolver.getType(this) ?: "image/*"
        val bytes = inputStream.readBytes()
        inputStream.close()

        val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull(), 0, bytes.size)
        MultipartBody.Part.createFormData(partName, "image.jpg", requestFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Extension function to convert a plain string into a plain-text RequestBody
 * for Retrofit multipart requests.
 */
fun String.toTextRequestBody(): RequestBody {
    return this.toRequestBody("text/plain".toMediaTypeOrNull())
}
