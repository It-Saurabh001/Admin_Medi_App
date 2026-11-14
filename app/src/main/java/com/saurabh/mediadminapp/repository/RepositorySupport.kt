package com.saurabh.mediadminapp.repository

import com.saurabh.mediadminapp.common.ResultState
import com.saurabh.mediadminapp.data.v1.core.ApiEnvelope
import com.saurabh.mediadminapp.data.v1.core.ApiResult
import com.saurabh.mediadminapp.data.v1.core.OrderStatus
import com.saurabh.mediadminapp.data.v1.core.PagedEnvelope
import com.saurabh.mediadminapp.data.v1.core.Pagination
import com.saurabh.mediadminapp.data.v1.core.safeApiCall
import com.saurabh.mediadminapp.data.v1.core.safeApiCallPaged
import com.saurabh.mediadminapp.data.v1.error.AppError
import com.saurabh.mediadminapp.data.v1.repository.AuditFilter
import com.saurabh.mediadminapp.data.v1.repository.DateRange
import com.saurabh.mediadminapp.data.v1.repository.ImagePart
import com.saurabh.mediadminapp.data.v1.repository.OrderFilter
import com.saurabh.mediadminapp.data.v1.repository.ProductFilter
import com.saurabh.mediadminapp.data.v1.repository.SalesFilter
import com.saurabh.mediadminapp.data.v1.repository.StockMovementFilter
import com.saurabh.mediadminapp.data.v1.repository.UserFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

/** Carried by `ResultState.Error.exception`; read `.error` for code / message / fields. */
class AppException(val error: AppError) : Exception(error.userMessage())

data class PagedData<T>(val items: List<T>, val pagination: Pagination?)

/** Enveloped call -> Loading, then Success(data) or Error(AppException). [onSuccess] runs before Success is emitted. */
internal fun <T> v1Flow(
    onSuccess: suspend (T) -> Unit = {},
    block: suspend () -> Response<ApiEnvelope<T>>
): Flow<ResultState<T>> = flow {
    emit(ResultState.Loading)
    when (val r = safeApiCall(block)) {
        is ApiResult.Success -> { onSuccess(r.data); emit(ResultState.Success(r.data)) }
        is ApiResult.Failure -> emit(ResultState.Error(AppException(r.error)))
    }
}

internal fun <T> v1Paged(block: suspend () -> Response<PagedEnvelope<T>>): Flow<ResultState<PagedData<T>>> = flow {
    emit(ResultState.Loading)
    when (val r = safeApiCallPaged(block)) {
        is ApiResult.Success -> emit(ResultState.Success(PagedData(r.data, r.pagination)))
        is ApiResult.Failure -> emit(ResultState.Error(AppException(r.error)))
    }
}

/** Query map: drops null/blank; Instant/LocalDate -> ISO string, OrderStatus -> wire, Boolean -> "true"/"false". */
fun queryOf(vararg pairs: Pair<String, Any?>): Map<String, String> = buildMap {
    for ((k, v) in pairs) {
        val s = when (v) {
            null -> null
            is OrderStatus -> v.wire
            is Enum<*> -> v.name
            else -> v.toString()
        }
        if (!s.isNullOrBlank()) put(k, s)
    }
}

fun DateRange?.toQuery(): Map<String, String> = queryOf("from" to this?.from, "to" to this?.to)

fun ProductFilter.toQuery() = queryOf(
    "q" to q, "category" to category, "schedule_type" to scheduleType,
    "requires_prescription" to requiresPrescription, "is_active" to isActive,
    "in_stock" to inStock, "updated_since" to updatedSince
)

fun OrderFilter.toQuery() = queryOf(
    "status" to status, "product_id" to productId, "user_id" to userId, "updated_since" to updatedSince
) + dateRange.toQuery()

fun SalesFilter.toQuery() = queryOf(
    "product_id" to productId, "user_id" to userId, "order_id" to orderId,
    "batch_id" to batchId, "updated_since" to updatedSince
) + dateRange.toQuery()

fun UserFilter.toQuery() = queryOf(
    "q" to q, "is_approved" to isApproved, "is_blocked" to isBlocked,
    "include_deleted" to includeDeleted, "updated_since" to updatedSince
)

fun StockMovementFilter.toQuery() = queryOf(
    "product_id" to productId, "batch_id" to batchId, "reason" to reason, "performed_by" to performedBy
) + dateRange.toQuery()

fun AuditFilter.toQuery() = queryOf(
    "entity" to entity, "entity_id" to entityId, "actor_id" to actorId, "action" to action
) + dateRange.toQuery()

fun ImagePart.toPart(field: String = "image"): MultipartBody.Part =
    MultipartBody.Part.createFormData(field, fileName, bytes.toRequestBody(mimeType.toMediaTypeOrNull()))