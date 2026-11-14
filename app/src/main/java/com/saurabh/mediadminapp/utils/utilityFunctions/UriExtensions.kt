package com.saurabh.mediadminapp.utils.utilityFunctions

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.saurabh.mediadminapp.data.v1.repository.ImagePart
import com.yalantis.ucrop.UCrop
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * Extension function to convert a content Uri into a MultipartBody.Part
 * for Retrofit file uploads.
 */
fun Uri.toMultipartBodyPart(context: Context, partName: String): MultipartBody.Part? {
    val (bytes, mimeType) = readBytesAndType(context, "image/*") ?: return null
    val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull(), 0, bytes.size)
    return MultipartBody.Part.createFormData(partName, "image.jpg", requestFile)
}

/** Content Uri -> v1 [ImagePart] (jpeg/png/webp; the server checks magic bytes). */
fun Uri.toImagePart(context: Context): ImagePart? {
    val (bytes, mimeType) = readBytesAndType(context, "image/jpeg") ?: return null
    val ext = mimeType.substringAfter('/', "jpg").ifBlank { "jpg" }
    return ImagePart(fileName = "image.$ext", mimeType = mimeType, bytes = bytes)
}

private fun Uri.readBytesAndType(context: Context, fallbackMime: String): Pair<ByteArray, String>? = try {
    val contentResolver = context.contentResolver
    contentResolver.openInputStream(this)?.use { input ->
        input.readBytes() to (contentResolver.getType(this) ?: fallbackMime)
    }
} catch (e: Exception) {
    e.printStackTrace()
    null
}

/**
 * Extension function to convert a plain string into a plain-text RequestBody
 * for Retrofit multipart requests.
 */
fun String.toTextRequestBody(): RequestBody {
    return this.toRequestBody("text/plain".toMediaTypeOrNull())
}


fun Context.createImageUri(): Uri {
    val imageFile = File(cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        this,
        "${packageName}.provider", // 👈 Ye exact match hona chahiye Manifest se
        imageFile
    )
}

// ── Utility function for Image Cropping ──────────────────────────────────────
fun getUCropIntent(context: Context, sourceUri: Uri): Intent {
    val destinationUri = Uri.fromFile(
        File(context.cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg")
    )
    return UCrop.of(sourceUri, destinationUri)
        .withAspectRatio(1f, 1f) // Product image ke liye 1:1 square ratio best hai
        .withMaxResultSize(1000, 1000) // Size optimize karta hai taaki timeout na ho!
        .getIntent(context)
}