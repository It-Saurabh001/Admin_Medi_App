package com.saurabh.mediadminapp

import com.saurabh.mediadminapp.data.v1.core.Pagination
import com.saurabh.mediadminapp.data.v1.dto.Supplier
import com.saurabh.mediadminapp.data.v1.error.AppError

/**
 * Generic paged list state (stock movements, inventory alerts, suppliers, audit logs).
 * [loaded] = first page arrived for the current filter; [items] stay visible while refreshing.
 */
data class PagedListState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false,
    val loaded: Boolean = false,
    val pagination: Pagination? = null,
    val error: String? = null,
    val appError: AppError? = null,
) {
    val total: Int? get() = pagination?.total
}

/** GET inventory/{low-stock | out-of-stock | near-expiry | expired}. */
enum class InventoryAlertType(val label: String) {
    LOW_STOCK("Low stock"),
    OUT_OF_STOCK("Out of stock"),
    NEAR_EXPIRY("Near expiry"),
    EXPIRED("Expired"),
}

/** Suppliers list query (GET suppliers `q`, `is_active`). */
data class SupplierQuery(val q: String = "", val isActive: Boolean? = true)

/**
 * Result of create / update / deactivate supplier. [seq] increases on every request so a screen
 * can ignore results it did not start (state is shared and kept until [MyViewModel.clearSupplierAction]).
 */
data class SupplierActionState(
    val seq: Int = 0,
    val isLoading: Boolean = false,
    val success: Supplier? = null,
    val error: String? = null,
    val appError: AppError? = null,
)
