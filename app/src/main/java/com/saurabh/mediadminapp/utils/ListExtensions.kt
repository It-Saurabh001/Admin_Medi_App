package com.saurabh.mediadminapp.utils

/**
 * Thread-safe atomic in-memory list mutation helpers.
 * All functions return a NEW list — they never mutate the original,
 * making them safe for use with MutableStateFlow.update { }.
 */

/**
 * Returns a new list with the first item matching [idSelector](item) == [id]
 * replaced by [transform](matchedItem). Items that don't match pass through
 * unchanged. O(n), allocation-minimal (no intermediate collections).
 */
inline fun <T, ID> List<T>.updateItem(
    id: ID,
    idSelector: (T) -> ID,
    transform: (T) -> T
): List<T> = map { item -> if (idSelector(item) == id) transform(item) else item }

/**
 * Returns a new list with all items matching [idSelector](item) == [id] removed.
 */
inline fun <T, ID> List<T>.removeItem(
    id: ID,
    idSelector: (T) -> ID
): List<T> = filter { idSelector(it) != id }

/**
 * Re-inserts [item] at [originalIndex] (clamped to valid range) in a new list.
 * Used to roll back an optimistic delete when the server responds with an error,
 * restoring the item at its exact previous position so the UI doesn't reorder.
 */
fun <T> List<T>.restoreItem(item: T, originalIndex: Int): List<T> {
    val safeIndex = originalIndex.coerceIn(0, size)
    return toMutableList().apply { add(safeIndex, item) }
}
