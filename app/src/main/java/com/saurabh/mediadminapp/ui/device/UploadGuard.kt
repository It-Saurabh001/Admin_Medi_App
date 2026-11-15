package com.saurabh.mediadminapp.ui.device

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.scale
import com.saurabh.mediadminapp.data.v1.dto.AppConfigUploads
import com.saurabh.mediadminapp.data.v1.error.AppError
import com.saurabh.mediadminapp.data.v1.repository.ImagePart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/** Result of preparing a file for upload. */
sealed interface UploadResult {
    data class Ready(val part: ImagePart) : UploadResult
    data class Rejected(val message: String) : UploadResult
}

/**
 * Validates and prepares an upload off the main thread.
 * - Images: decoded, downscaled to <= [maxPx] on the long edge, re-encoded as JPEG [quality].
 * - Non-images (PDF prescriptions): sent as-is.
 * - Checks MIME against [uploads] lists (image_mime_types or allowed_mime_types) and size <= max_upload_mb.
 * Defaults (5 MB, jpeg/png/webp[/pdf]) from response.md apply while app-config is not loaded.
 */
suspend fun prepareUpload(
    context: Context,
    uri: Uri,
    uploads: AppConfigUploads?,
    imageOnly: Boolean = true,
    maxPx: Int = 2048,
    quality: Int = 85,
): UploadResult = withContext(Dispatchers.IO) {
    val cr = context.contentResolver
    val mime = cr.getType(uri) ?: "image/jpeg"
    val allowed = when {
        imageOnly -> uploads?.imageMimeTypes ?: listOf("image/jpeg", "image/png", "image/webp")
        else -> uploads?.allowedMimeTypes ?: listOf(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf"
        )
    }
    if (mime !in allowed) return@withContext UploadResult.Rejected(
        "Unsupported file type. Use ${
            allowed.joinToString {
                it.substringAfter(
                    '/'
                )
            }
        }."
    )
    val maxBytes = (uploads?.maxUploadMb ?: 5) * 1024L * 1024L

    val bytes: ByteArray
    val outMime: String
    if (mime.startsWith("image/")) {
        val raw = runCatching { cr.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()
            ?: return@withContext UploadResult.Rejected("Could not read the image.")
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(raw, 0, raw.size, bounds)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxPx) sample *= 2
        val bmp = BitmapFactory.decodeByteArray(
            raw,
            0,
            raw.size,
            BitmapFactory.Options().apply { inSampleSize = sample })
            ?: return@withContext UploadResult.Rejected("Could not read the image.")
        val scale = maxPx.toFloat() / maxOf(bmp.width, bmp.height)
        val scaled = if (scale < 1f) bmp.scale(
            (bmp.width * scale).toInt(),
            (bmp.height * scale).toInt()
        ) else bmp
        bytes = ByteArrayOutputStream().use { out ->
            scaled.compress(
                Bitmap.CompressFormat.JPEG,
                quality,
                out
            ); out.toByteArray()
        }
        outMime = "image/jpeg"
    } else {
        bytes = runCatching { cr.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()
            ?: return@withContext UploadResult.Rejected("Could not read the file.")
        outMime = mime
    }
    if (bytes.size > maxBytes) return@withContext UploadResult.Rejected("File is larger than ${maxBytes / (1024 * 1024)} MB.")
    val ext = if (outMime == "application/pdf") "pdf" else "jpg"
    UploadResult.Ready(ImagePart(fileName = "upload.$ext", mimeType = outMime, bytes = bytes))
}

/** Maps upload-related server errors (413/415) to a user message; null if not upload-related. */
fun AppError?.uploadMessage(): String? {
    val api = this as? AppError.Api ?: return null
    return when (api.code.name) {
        "PAYLOAD_TOO_LARGE" -> "File too large. Choose a smaller file."
        "UNSUPPORTED_MEDIA_TYPE" -> api.fields?.values?.firstOrNull() ?: "Unsupported file type."
        else -> null
    }
}
