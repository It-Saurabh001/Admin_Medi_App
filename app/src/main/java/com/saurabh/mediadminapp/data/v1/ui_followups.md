# UI follow-ups (ADMIN APP ONLY)

One line per skipped admin UI item. VM names refer to `viewmodel_api.md`.

- AddProductScreen: stock field still shown and validated but ignored (products start at 0) -> hide it; stock comes from purchases (no VM fn yet; add when inventory UI is built).
- UpdateProductScreen: stock field still shown but not sent -> hide or make read-only from `getSpecificProductState.success.stock`.
- UpdateUserDetailsScreen: approve switch calls `isApprovedUser(id, false)` which now BLOCKS the user -> call only when the switch changed; "blocked" switch is ignored -> wire to `isApprovedUser(id, false)` / `unblockUser(id)`.
- UserDetailsScreen / HomeScreen user list: add Unblock action -> `unblockUser(userId)`, state `isApprovedUser[userId]`.
- ProfileScreen / admin edit: password and role edits removed -> remove those inputs if any (`updateAdmin(adminId, name, email, phoneNumber, image)`).
- SpecificOrderScreen: add "Mark sold" -> `recordSell(orderId)`, state `getRecordSellHistory[orderId]`; show `appError.code` OUT_OF_STOCK / PRESCRIPTION_NOT_VERIFIED / ALREADY_SOLD.
- SpecificOrderScreen: add "Return" (status SOLD) -> `returnOrder(orderId, reason)`, state `returnOrderState[orderId]`.
- SpecificOrderScreen: add "Verify prescription" (requiresPrescription && !prescriptionVerified) -> `verifyPrescription(orderId)`, state `verifyPrescriptionState[orderId]`; show prescription image from `order.prescriptionUrl`.
- SpecificOrderScreen: approve/reject buttons -> `isApproveOrder(orderId, true|false, reason)`; reject reason input.
- Order screens: delete action is now cancel -> rename label, optional reason -> `deleteOrder(orderId, reason)`; edit action only patient/doctor name -> `updateOrder(orderId, patientName, doctorName)`.
- Invoice screen (SOLD/RETURNED orders) -> `getInvoice(orderId)`, `invoiceState: UiState<dto.Invoice>`.
- Order status chips/filters: replace approved/pending booleans with `OrderStatus` (PENDING, APPROVED, SOLD, CANCELLED, RETURNED).
- Load-more triggers at list end -> `loadMoreUsers()`, `loadMoreProducts()`, `loadMoreOrders()`, `loadMoreSellHistory()`, `loadMoreAdmins()`; footer spinner from `isLoadingMore`, stop at `endReached`.
- Home dashboard: replace client-side counts with `getDashboardSummary()` / `dashboardSummaryState`.
- Force-update screen: show blocking UI when `upgradeRequiredVersion != null`.
- Blocked / session messages: already snackbar + logout via VM; add a dedicated blocked screen if desired (AppEvent.AccountBlocked is handled in VM).
- Login: show "awaiting approval" / "blocked" from `loginAdminState.appError` (`ErrorCode.ACCOUNT_NOT_APPROVED` / `ACCOUNT_BLOCKED`) and "Admin account required" from `verifyOtpState.error`.
- Forms: show field errors from `appError.fields` (Login, VerifyOtp, PasswordReset, PasswordResetOtp, CreateAdmin, AddProduct, UpdateProduct, UpdateUser).
- Forgot/reset password: confirm step must pass the EMAIL (`resetPasswordOtp(email, otp, newPassword)`), not an admin id.
- Products: deactivated products -> show "Reactivate" -> `reactivateProduct(productId)`; label delete as "Deactivate".
- History dates: grouping uses `soldAt` UTC date; convert to local time for display.
