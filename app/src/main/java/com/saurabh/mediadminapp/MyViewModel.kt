package com.saurabh.mediadminapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.mediadminapp.common.ResultState
import com.saurabh.mediadminapp.data.v1.core.AppEvent
import com.saurabh.mediadminapp.data.v1.core.AppEvents
import com.saurabh.mediadminapp.data.v1.core.IdempotencyKeyProvider
import com.saurabh.mediadminapp.data.v1.core.OrderStatus
import com.saurabh.mediadminapp.data.v1.core.PageRequest
import com.saurabh.mediadminapp.data.v1.dto.StockPurchaseRequest
import com.saurabh.mediadminapp.data.v1.repository.ProductFilter
import com.saurabh.mediadminapp.data.v1.core.Pagination
import com.saurabh.mediadminapp.data.v1.core.UiState
import com.saurabh.mediadminapp.data.v1.core.loadMore
import com.saurabh.mediadminapp.data.v1.dto.AdminCreateRequest
import com.saurabh.mediadminapp.data.v1.dto.AdminUpdateRequest
import com.saurabh.mediadminapp.data.v1.dto.DashboardSummary
import com.saurabh.mediadminapp.data.v1.dto.Invoice
import com.saurabh.mediadminapp.data.v1.dto.Order
import com.saurabh.mediadminapp.data.v1.dto.ProductCreateRequest
import com.saurabh.mediadminapp.data.v1.dto.ProductUpdateRequest
import com.saurabh.mediadminapp.data.v1.error.AppError
import com.saurabh.mediadminapp.data.v1.repository.ImagePart
import com.saurabh.mediadminapp.repository.AppException
import com.saurabh.mediadminapp.repository.PagedData
import com.saurabh.mediadminapp.repository.Repository
import com.saurabh.mediadminapp.utils.ScreensState.AddProductState
import com.saurabh.mediadminapp.utils.ScreensState.AdminState
import com.saurabh.mediadminapp.utils.ScreensState.ApproveOrderState
import com.saurabh.mediadminapp.utils.ScreensState.CreateAdminState
import com.saurabh.mediadminapp.utils.ScreensState.DeleteOrderState
import com.saurabh.mediadminapp.utils.ScreensState.DeleteProductState
import com.saurabh.mediadminapp.utils.ScreensState.DeleteUserState
import com.saurabh.mediadminapp.utils.ScreensState.GetAllAdminState
import com.saurabh.mediadminapp.utils.ScreensState.GetAllOrdersState
import com.saurabh.mediadminapp.utils.ScreensState.GetAllProductState
import com.saurabh.mediadminapp.utils.ScreensState.GetAllUserState
import com.saurabh.mediadminapp.utils.ScreensState.GetOrderByIdState
import com.saurabh.mediadminapp.utils.ScreensState.GetProductSellHistoryState
import com.saurabh.mediadminapp.utils.ScreensState.GetSellHistoryState
import com.saurabh.mediadminapp.utils.ScreensState.GetSpecificProductState
import com.saurabh.mediadminapp.utils.ScreensState.GetUserSellHistoryState
import com.saurabh.mediadminapp.utils.ScreensState.GetUsersOrderState
import com.saurabh.mediadminapp.utils.ScreensState.IsApprovedUserState
import com.saurabh.mediadminapp.utils.ScreensState.LoginAdminState
import com.saurabh.mediadminapp.utils.ScreensState.PasswordResetOtpState
import com.saurabh.mediadminapp.utils.ScreensState.PasswordResetState
import com.saurabh.mediadminapp.utils.ScreensState.RecordSellHistoryState
import com.saurabh.mediadminapp.utils.ScreensState.UpdateOrderState
import com.saurabh.mediadminapp.utils.ScreensState.UpdateProductState
import com.saurabh.mediadminapp.utils.ScreensState.VerifyOtpState
import com.saurabh.mediadminapp.utils.UiEvent
import com.saurabh.mediadminapp.utils.removeItem
import com.saurabh.mediadminapp.utils.restoreItem
import com.saurabh.mediadminapp.utils.updateItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: Repository,
    private val appEvents: AppEvents,
    private val idempotencyKeyProvider: IdempotencyKeyProvider
) : ViewModel() {

    // FIX 1: Tri-State (null = checking, true = logged in, false = logged out)
    private val _isAdminLoggedIn = MutableStateFlow<Boolean?>(null)
    val isAdminLoggedIn = _isAdminLoggedIn.asStateFlow()

    private val _loggedInAdminId = MutableStateFlow<String?>(null)
    val loggedInAdminId = _loggedInAdminId.asStateFlow()

    /** Non-null = server requires an app update (minimum version, or "latest" when unknown). */
    private val _upgradeRequiredVersion = MutableStateFlow<String?>(null)
    val upgradeRequiredVersion: StateFlow<String?> = _upgradeRequiredVersion.asStateFlow()

    /** Non-null when GET /app-config failed and nothing is cached (Splash shows Retry). */
    private val _appConfigError = MutableStateFlow<String?>(null)
    val appConfigError: StateFlow<String?> = _appConfigError.asStateFlow()

    // ===========================
    // 1. AUTHENTICATION STATES
    // ===========================
    private val _createAdminState = MutableStateFlow(CreateAdminState())
    val createAdminState = _createAdminState.asStateFlow()

    private val _loginAdminState = MutableStateFlow(LoginAdminState())
    val loginAdminState = _loginAdminState.asStateFlow()

    private val _getAllAdminState = MutableStateFlow(GetAllAdminState())
    val getAllAdminState = _getAllAdminState.asStateFlow()

    private val _specificAdminState = MutableStateFlow(AdminState())
    val specificAdminState = _specificAdminState.asStateFlow()

    private val _verifyOtpState = MutableStateFlow(VerifyOtpState())
    val verifyOtpState = _verifyOtpState.asStateFlow()

    private val _passwordResetOtpState = MutableStateFlow(PasswordResetOtpState())
    val passwordResetOtpState = _passwordResetOtpState.asStateFlow()

    private val _passwordResetState = MutableStateFlow(PasswordResetState())
    val passwordResetState = _passwordResetState.asStateFlow()

    // ===========================
    // 2. USER, PRODUCT, ORDER & HISTORY STATES
    // ===========================
    private val _getAllUserState = MutableStateFlow(GetAllUserState())
    val getAllUserState = _getAllUserState.asStateFlow()

    private val _getAllProduct = MutableStateFlow(GetAllProductState())
    val getAllProduct = _getAllProduct.asStateFlow()

    private val _addProductState = MutableStateFlow(AddProductState())
    val addProductState = _addProductState.asStateFlow()

    private val _updateProductState = MutableStateFlow(UpdateProductState())
    val updateProductState = _updateProductState.asStateFlow()

    private val _deleteProductState = MutableStateFlow(DeleteProductState())
    val deleteProductState = _deleteProductState.asStateFlow()

    private val _getSpecificProductState = MutableStateFlow(GetSpecificProductState())
    val getSpecificProductState = _getSpecificProductState.asStateFlow()

    /** key = userId */
    private val _isApproved = MutableStateFlow(emptyMap<String, IsApprovedUserState>())
    val isApprovedUser = _isApproved.asStateFlow()

    /** key = orderId */
    private val _updateOrderState = MutableStateFlow(emptyMap<String, UpdateOrderState>())
    val updateOrderState = _updateOrderState.asStateFlow()

    private val _deleteUserState = MutableStateFlow(DeleteUserState())
    val deleteUserState = _deleteUserState.asStateFlow()

    private val _getAllOrderState = MutableStateFlow(GetAllOrdersState())
    val getAllOrderState = _getAllOrderState.asStateFlow()

    private val _getUsersOrdersState = MutableStateFlow(GetUsersOrderState())
    val getUsersOrdersState = _getUsersOrdersState.asStateFlow()

    private val _deleteOrderState = MutableStateFlow(DeleteOrderState())
    val deleteOrderState = _deleteOrderState.asStateFlow()

    /** key = orderId */
    private val _isApproveState = MutableStateFlow(emptyMap<String, ApproveOrderState>())
    val isApproveOrdder = _isApproveState.asStateFlow()

    private val _getSellHistory = MutableStateFlow(GetSellHistoryState())
    val getSellHistory = _getSellHistory.asStateFlow()

    /** key = orderId (sell). */
    private val _getRecordSellHistory = MutableStateFlow(emptyMap<String, RecordSellHistoryState>())
    val getRecordSellHistory = _getRecordSellHistory.asStateFlow()

    /** key = orderId (return). */
    private val _returnOrderState = MutableStateFlow(emptyMap<String, UpdateOrderState>())
    val returnOrderState = _returnOrderState.asStateFlow()

    /** key = orderId (verify prescription). */
    private val _verifyPrescriptionState = MutableStateFlow(emptyMap<String, UpdateOrderState>())
    val verifyPrescriptionState = _verifyPrescriptionState.asStateFlow()

    private val _getProductSellHistory = MutableStateFlow(GetProductSellHistoryState())
    val getProductSellHistory = _getProductSellHistory.asStateFlow()

    private val _getUserSellHistory = MutableStateFlow(GetUserSellHistoryState())
    val getUserSellHistory = _getUserSellHistory.asStateFlow()

    private val _getOrderByIdState = MutableStateFlow(GetOrderByIdState())
    val getOrderByIdState = _getOrderByIdState.asStateFlow()

    private val _invoiceState = MutableStateFlow<UiState<Invoice>>(UiState.Empty)
    val invoiceState: StateFlow<UiState<Invoice>> = _invoiceState.asStateFlow()

    private val _dashboardSummaryState = MutableStateFlow<UiState<DashboardSummary>>(UiState.Empty)
    val dashboardSummaryState: StateFlow<UiState<DashboardSummary>> = _dashboardSummaryState.asStateFlow()

    // FIX 2: Track last requested IDs for parameterized queries so clicking a different item works!
    private var lastProductId: String? = null
    private var lastUserOrdersId: String? = null
    private var lastOrderId: String? = null

    // Paging cursors (first page = PageRequest()).
    private var usersPage = PageRequest()
    private var productsPage = PageRequest()
    private var ordersPage = PageRequest()
    private var salesPage = PageRequest()
    private var adminsPage = PageRequest(limit = 100)

    /** Email entered on the forgot-password step (confirm needs it). */
    private var resetEmail: String? = null

    /** One Idempotency-Key per order sell action, reused on retry, removed on success. */
    private val sellKeys = mutableMapOf<String, String>()

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        checkLoginStatus()
        observeAppEvents()
        getAppConfig()
    }

    // ===========================
    // HELPERS
    // ===========================
    private suspend fun <T> Flow<ResultState<T>>.collectInto(
        onLoading: () -> Unit = {},
        onError: suspend (String?, AppError?) -> Unit,
        onSuccess: suspend (T) -> Unit
    ) = collect { r ->
        when (r) {
            is ResultState.Loading -> onLoading()
            is ResultState.Error -> onError(r.exception.message, (r.exception as? AppException)?.error)
            is ResultState.Success -> onSuccess(r.data)
        }
    }

    private fun snackbar(message: String) {
        _uiEvent.trySend(UiEvent.ShowSnackbar(message))
    }

    private fun PageRequest.isLast(p: Pagination?): Boolean = p == null || loadMore(p) == null

    private fun <T> PagedData<T>?.append(more: PagedData<T>): PagedData<T> =
        PagedData((this?.items ?: emptyList()) + more.items, more.pagination)

    /** Replace an order row everywhere it is shown (keeps `sales` of the detail if the new row has none). */
    private fun replaceOrderRow(order: Order) {
        _getAllOrderState.update { s ->
            val d = s.success ?: return@update s
            s.copy(success = d.copy(items = d.items.updateItem(order.orderId, { it.orderId }) { order }))
        }
        _getUsersOrdersState.update { s ->
            val d = s.success ?: return@update s
            s.copy(success = d.copy(items = d.items.updateItem(order.orderId, { it.orderId }) { order }))
        }
        _getOrderByIdState.update { s ->
            val old = s.success
            if (old?.orderId == order.orderId) s.copy(success = order.copy(sales = order.sales ?: old.sales)) else s
        }
    }

    private fun observeAppEvents() {
        viewModelScope.launch {
            appEvents.events.collect { event ->
                when (event) {
                    AppEvent.SessionInvalid -> resetAllStatesOnLogout()
                    AppEvent.AccountBlocked -> {
                        snackbar("Account blocked, contact the pharmacy")
                        setAdminLoggedOut()
                    }
                    AppEvent.RateLimited -> snackbar("Too many attempts, please wait")
                    is AppEvent.UpgradeRequired -> _upgradeRequiredVersion.value = event.minVersion ?: "latest"
                }
            }
        }
    }

    /** Last fetched app-config (uploads limits, enums, otp); null until loaded. */
    fun cachedAppConfig(): com.saurabh.mediadminapp.data.v1.dto.AppConfig? = repository.cachedAppConfig()

    /** Fetches app-config once at startup; flags a forced update when min_app_version > this build. */
    fun getAppConfig() {
        _appConfigError.value = null
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAppConfig().collectInto(
                onError = { msg, _ ->
                    if (repository.cachedAppConfig() == null) _appConfigError.value = msg?.takeIf { it.isNotBlank() } ?: "Could not reach the server"
                },
                onSuccess = { c ->
                    _appConfigError.value = null
                    val min = c.minAppVersion
                    if (min != null && isVersionLower(BuildConfig.VERSION_NAME, min)) _upgradeRequiredVersion.value = min
                }
            )
        }
    }

    /** Dotted-int compare: "1.4" < "1.10". Non-numeric parts count as 0. */
    private fun isVersionLower(current: String, min: String): Boolean {
        val a = current.split('.').map { it.toIntOrNull() ?: 0 }
        val b = min.split('.').map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x < y
        }
        return false
    }

    // ===========================
    // SESSION
    // ===========================
    fun checkLoginStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            _loggedInAdminId.value = repository.currentAccountId()
            _isAdminLoggedIn.value = repository.isLoggedIn()
        }
    }

    fun setAdminLoggedIn() {
        _loggedInAdminId.value = repository.currentAccountId()
        _isAdminLoggedIn.value = true
    }

    /** Server logout (unregisters device, revokes tokens); local session is always cleared. */
    fun setAdminLoggedOut() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.logout().collectInto(onError = { _, _ -> }, onSuccess = { })
            } finally {
                resetAllStatesOnLogout()
            }
        }
    }

    // FIX 3: Master Reset Function so re-logging in never shows stale data or locked buttons
    private fun resetAllStatesOnLogout() {
        _salesReportState.value = SalesReportState()
        _topProductsState.value = TopProductsState()
        _productFilter.value = ProductFilter()
        _registerAdminState.value = RegisterAdminState()
        _adminActionIds.value = emptySet()
        clearStockPurchaseState()
        clearBatchAction()
        _batchDetail.value = UiState.Empty
        _productBatches.value = UiState.Empty
        _barcodeLookup.value = BarcodeLookupState.Idle
        _isAdminLoggedIn.value = false
        _loggedInAdminId.value = null
        _createAdminState.value = CreateAdminState()
        _loginAdminState.value = LoginAdminState()
        _verifyOtpState.value = VerifyOtpState()
        _passwordResetState.value = PasswordResetState()
        _passwordResetOtpState.value = PasswordResetOtpState()
        _getAllAdminState.value = GetAllAdminState()
        _getAllUserState.value = GetAllUserState()
        _getAllProduct.value = GetAllProductState()
        _getAllOrderState.value = GetAllOrdersState()
        _getSellHistory.value = GetSellHistoryState()
        _isApproved.value = emptyMap()
        _isApproveState.value = emptyMap()
        _updateOrderState.value = emptyMap()
        _getRecordSellHistory.value = emptyMap()
        _returnOrderState.value = emptyMap()
        _verifyPrescriptionState.value = emptyMap()
        _invoiceState.value = UiState.Empty
        _dashboardSummaryState.value = UiState.Empty
        resetInventoryAdminData()
        sellKeys.clear()
        resetEmail = null
        lastProductId = null
        lastUserOrdersId = null
        lastOrderId = null
    }

    // ===========================
    // AUTH ACTIONS
    // ===========================
    fun createAdmin(
        name: String,
        email: String,
        password: String,
        phoneNumber: String,
        image: ImagePart? = null
    ) {
        if (_createAdminState.value.isLoading) return
        val request = AdminCreateRequest(name, email, password, phoneNumber.ifBlank { null })
        viewModelScope.launch(Dispatchers.IO) {
            repository.createAdmin(request, image).collectInto(
                onLoading = { _createAdminState.value = CreateAdminState(isLoading = true) },
                onError = { m, e -> _createAdminState.value = CreateAdminState(error = m, appError = e) },
                onSuccess = { _createAdminState.value = CreateAdminState(success = it) }
            )
        }
    }

    fun loginAdminWithGoogle(idToken: String) {
        if (_loginAdminState.value.isLoading) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.loginAdminWithGoogle(idToken).collectInto(
                onLoading = { _loginAdminState.value = LoginAdminState(isLoading = true) },
                onError = { m, e -> _loginAdminState.value = LoginAdminState(error = m, appError = e) },
                onSuccess = { _loginAdminState.value = LoginAdminState(success = it) }
            )
        }
    }

    fun loginAdmin(email: String, password: String) {
        if (_loginAdminState.value.isLoading) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.loginAdmin(email, password).collectInto(
                onLoading = { _loginAdminState.value = LoginAdminState(isLoading = true) },
                onError = { m, e -> _loginAdminState.value = LoginAdminState(error = m, appError = e) },
                onSuccess = { _loginAdminState.value = LoginAdminState(success = it) }
            )
        }
    }

    /** [accountId] = LoginData.accountId. Repository saves the session; non-admin roles are logged out. */
    fun verifyAdminOtp(accountId: String, otp: String) {
        if (_verifyOtpState.value.isLoading) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.verifyAdminOtp(accountId, otp).collectInto(
                onLoading = { _verifyOtpState.value = VerifyOtpState(isLoading = true) },
                onError = { m, e -> _verifyOtpState.value = VerifyOtpState(error = m, appError = e) },
                onSuccess = { t ->
                    if (t.role != "admin") {
                        repository.logout().collectInto(onError = { _, _ -> }, onSuccess = { })
                        _verifyOtpState.value = VerifyOtpState(error = "Admin account required")
                    } else {
                        setAdminLoggedIn()
                        _verifyOtpState.value = VerifyOtpState(success = t)
                    }
                }
            )
        }
    }

    private val _emailVerifyState = MutableStateFlow(EmailVerifyState())
    val emailVerifyState: StateFlow<EmailVerifyState> = _emailVerifyState.asStateFlow()

    fun verifyAdminEmail(email: String, otp: String) {
        if (_emailVerifyState.value.isLoading) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.verifyAdminEmail(email, otp).collectInto(
                onLoading = { _emailVerifyState.value = EmailVerifyState(isLoading = true) },
                onError = { m, e -> _emailVerifyState.value = EmailVerifyState(error = m ?: "Verification failed", appError = e) },
                onSuccess = { _emailVerifyState.value = EmailVerifyState(verified = true) }
            )
        }
    }

    fun resendAdminEmailOtp(email: String) {
        if (_emailVerifyState.value.isLoading) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.resendAdminEmailOtp(email).collectInto(
                onLoading = { _emailVerifyState.value = EmailVerifyState(isLoading = true) },
                onError = { m, e -> _emailVerifyState.value = EmailVerifyState(error = m ?: "Could not resend code", appError = e) },
                onSuccess = { _emailVerifyState.value = EmailVerifyState(resent = true) }
            )
        }
    }

    fun clearEmailVerifyState() { _emailVerifyState.value = EmailVerifyState() }

    fun clearPasswordResetStates() {
        _passwordResetState.value = PasswordResetState()
        _passwordResetOtpState.value = PasswordResetOtpState()
    }

    /** Email last used for requestPasswordReset (prefills ResetPassword). */
    fun lastResetEmail(): String? = resetEmail

    fun requestPasswordReset(email: String) {
        if (_passwordResetState.value.isLoading) return
        resetEmail = email
        viewModelScope.launch(Dispatchers.IO) {
            repository.requestAdminPasswordReset(email).collectInto(
                onLoading = { _passwordResetState.value = PasswordResetState(isLoading = true) },
                onError = { m, e -> _passwordResetState.value = PasswordResetState(error = m, appError = e) },
                onSuccess = { _passwordResetState.value = PasswordResetState(success = it) }
            )
        }
    }

    /** [email] defaults to the one given to [requestPasswordReset]. */
    fun resetPasswordOtp(email: String? = null, otp: String, newPassword: String) {
        if (_passwordResetOtpState.value.isLoading) return
        val target = email ?: resetEmail.orEmpty()
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetAdminPasswordWithOtp(target, otp, newPassword).collectInto(
                onLoading = { _passwordResetOtpState.value = PasswordResetOtpState(isLoading = true) },
                onError = { m, e -> _passwordResetOtpState.value = PasswordResetOtpState(error = m, appError = e) },
                onSuccess = { _passwordResetOtpState.value = PasswordResetOtpState(success = Unit) }
            )
        }
    }

    // ===========================
    // ADMINS
    // ===========================
    /** Public admin signup (Login -> Create account). */
    private val _registerAdminState = MutableStateFlow(RegisterAdminState())
    val registerAdminState: StateFlow<RegisterAdminState> = _registerAdminState.asStateFlow()

    /** app-config `admin_signup`; null = old server or config not loaded (treat as enabled, no code). */
    fun adminSignupConfig(): com.saurabh.mediadminapp.data.v1.dto.AppConfigAdminSignup? = repository.cachedAppConfig()?.adminSignup

    fun registerAdmin(
        name: String,
        email: String,
        password: String,
        phoneNumber: String? = null,
        signupCode: String? = null,
        image: ImagePart? = null
    ) {
        if (_registerAdminState.value.isLoading) return
        val request = com.saurabh.mediadminapp.data.v1.dto.AdminRegisterRequest(
            name = name.trim(), email = email.trim(), password = password,
            phoneNumber = phoneNumber?.trim()?.ifBlank { null }, signupCode = signupCode?.trim()?.ifBlank { null }
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.registerAdmin(request, image).collectInto(
                onLoading = { _registerAdminState.value = RegisterAdminState(isLoading = true) },
                onError = { m, e -> _registerAdminState.value = RegisterAdminState(error = m ?: "Sign up failed", appError = e) },
                onSuccess = { _registerAdminState.value = RegisterAdminState(success = it) }
            )
        }
    }

    fun registerAdminWithGoogle(idToken: String, signupCode: String? = null, phoneNumber: String? = null) {
        if (_registerAdminState.value.isLoading) return
        val request = com.saurabh.mediadminapp.data.v1.dto.AdminGoogleRegisterRequest(
            idToken = idToken, signupCode = signupCode?.trim()?.ifBlank { null },
            phoneNumber = phoneNumber?.trim()?.ifBlank { null }
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.registerAdminWithGoogle(request).collectInto(
                onLoading = { _registerAdminState.value = RegisterAdminState(isLoading = true) },
                onError = { m, e -> _registerAdminState.value = RegisterAdminState(error = m ?: "Sign up failed", appError = e) },
                onSuccess = { _registerAdminState.value = RegisterAdminState(success = it) }
            )
        }
    }

    fun clearRegisterAdminState() { _registerAdminState.value = RegisterAdminState() }

    /** Admin ids with an approve/delete request in flight (row spinners, double-tap guard). */
    private val _adminActionIds = MutableStateFlow<Set<String>>(emptySet())
    val adminActionIds: StateFlow<Set<String>> = _adminActionIds.asStateFlow()

    fun approveAdmin(adminId: String) {
        if (adminId in _adminActionIds.value) return
        _adminActionIds.update { it + adminId }
        viewModelScope.launch(Dispatchers.IO) {
            repository.approveAdmin(adminId).collectInto(
                onError = { m, _ -> _adminActionIds.update { it - adminId }; snackbar(m ?: "Approve failed") },
                onSuccess = { a ->
                    _getAllAdminState.update { st ->
                        st.copy(success = st.success?.let { d -> d.copy(items = d.items.map { if (it.adminId == a.adminId) a else it }) })
                    }
                    _adminActionIds.update { it - adminId }
                    snackbar("${a.name} approved")
                }
            )
        }
    }

    /** DELETE admins/{id}: rejects a pending signup or removes an admin. Owner 403, last approved admin 409. */
    fun deleteAdmin(adminId: String) {
        if (adminId in _adminActionIds.value) return
        _adminActionIds.update { it + adminId }
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAdmin(adminId).collectInto(
                onError = { m, _ -> _adminActionIds.update { it - adminId }; snackbar(m ?: "Delete failed") },
                onSuccess = {
                    _getAllAdminState.update { st ->
                        st.copy(success = st.success?.let { d -> d.copy(items = d.items.filterNot { it.adminId == adminId }) })
                    }
                    _adminActionIds.update { it - adminId }
                    snackbar("Admin removed")
                }
            )
        }
    }

    fun getAllAdmin(force: Boolean = false) {
        val s = _getAllAdminState.value
        if (!force && s.success != null && !s.isLoading && s.error == null) return
        adminsPage = PageRequest(limit = 100)
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllAdmins(page = adminsPage).collectInto(
                onLoading = { _getAllAdminState.update { it.copy(isLoading = true, error = null, isLoadingMore = false) } },
                onError = { m, _ -> _getAllAdminState.update { it.copy(isLoading = false, isLoadingMore = false, error = m ?: "Could not load") } },
                onSuccess = { d ->
                    _getAllAdminState.value = GetAllAdminState(
                        success = d, pagination = d.pagination, endReached = adminsPage.isLast(d.pagination)
                    )
                }
            )
        }
    }

    fun loadMoreAdmins() {
        val s = _getAllAdminState.value
        if (s.isLoading || s.isLoadingMore || s.endReached) return
        val next = s.pagination?.let { adminsPage.loadMore(it) }
        if (next == null) { _getAllAdminState.update { it.copy(endReached = true) }; return }
        _getAllAdminState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllAdmins(page = next).collectInto(
                onError = { m, _ -> _getAllAdminState.update { it.copy(isLoadingMore = false) }; snackbar(m ?: "Load failed") },
                onSuccess = { d ->
                    adminsPage = next
                    _getAllAdminState.update {
                        it.copy(success = it.success.append(d), pagination = d.pagination, isLoadingMore = false, endReached = next.isLast(d.pagination))
                    }
                }
            )
        }
    }

    /** Admin password/role edits are not supported here (owner-only with current_password in v1). */
    fun updateAdmin(
        adminId: String,
        name: String? = null,
        email: String? = null,
        phoneNumber: String? = null,
        image: ImagePart? = null
    ) {
        val request = AdminUpdateRequest(name = name, email = email, phoneNumber = phoneNumber)
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAdmin(adminId, request, image).collectInto(
                onError = { m, _ -> snackbar(m ?: "Update failed") },
                onSuccess = {
                    getAllAdmin(force = true)
                    snackbar("Admin updated successfully")
                }
            )
        }
    }

    // ===========================
    // REPORTS (GET /reports/sales, /reports/top-products)
    // ===========================
    private val _salesReportState = MutableStateFlow(SalesReportState())
    val salesReportState: StateFlow<SalesReportState> = _salesReportState.asStateFlow()
    private var salesReportPage = PageRequest()
    private var salesReportJob: kotlinx.coroutines.Job? = null

    /** Loads page 1 for [query] (null = keep current). Cached unless the query changed or [force]. */
    fun loadSalesReport(query: ReportQuery? = null, force: Boolean = false) {
        val s = _salesReportState.value
        val q = query ?: s.query
        if (!force && q == s.query && s.loaded && s.error == null) return
        salesReportPage = PageRequest(sort = q.sort, order = q.order)
        val page = salesReportPage
        salesReportJob?.cancel()
        salesReportJob = viewModelScope.launch(Dispatchers.IO) {
            repository.getSalesReport(q.range, q.groupBy, page).collectInto(
                onLoading = { _salesReportState.update { it.copy(query = q, isLoading = true, error = null, appError = null) } },
                onError = { m, e ->
                    _salesReportState.update { it.copy(query = q, isLoading = false, error = m ?: "Could not load report", appError = e) }
                },
                onSuccess = { r ->
                    _salesReportState.value = SalesReportState(
                        query = q, rows = r.rows, summary = r.summary, pagination = r.pagination,
                        endReached = page.isLast(r.pagination), loaded = true
                    )
                }
            )
        }
    }

    fun loadMoreSalesReport() {
        val s = _salesReportState.value
        if (s.isLoading || s.isLoadingMore || s.endReached) return
        val next = s.pagination?.let { salesReportPage.loadMore(it) }
        if (next == null) { _salesReportState.update { it.copy(endReached = true) }; return }
        _salesReportState.update { it.copy(isLoadingMore = true) }
        val q = s.query
        viewModelScope.launch(Dispatchers.IO) {
            repository.getSalesReport(q.range, q.groupBy, next).collectInto(
                onError = { m, _ -> _salesReportState.update { it.copy(isLoadingMore = false) }; snackbar(m ?: "Load failed") },
                onSuccess = { r ->
                    if (_salesReportState.value.query != q) return@collectInto
                    salesReportPage = next
                    _salesReportState.update {
                        it.copy(rows = it.rows + r.rows, pagination = r.pagination, isLoadingMore = false, endReached = next.isLast(r.pagination))
                    }
                }
            )
        }
    }

    private val _topProductsState = MutableStateFlow(TopProductsState())
    val topProductsState: StateFlow<TopProductsState> = _topProductsState.asStateFlow()
    private var topProductsPage = PageRequest()
    private var topProductsJob: kotlinx.coroutines.Job? = null

    fun loadTopProducts(query: ReportQuery? = null, force: Boolean = false) {
        val s = _topProductsState.value
        val q = query ?: s.query
        if (!force && q == s.query && s.loaded && s.error == null) return
        topProductsPage = PageRequest(sort = q.sort, order = q.order)
        val page = topProductsPage
        topProductsJob?.cancel()
        topProductsJob = viewModelScope.launch(Dispatchers.IO) {
            repository.getTopProducts(q.range, page).collectInto(
                onLoading = { _topProductsState.update { it.copy(query = q, isLoading = true, error = null, appError = null) } },
                onError = { m, e ->
                    _topProductsState.update { it.copy(query = q, isLoading = false, error = m ?: "Could not load top products", appError = e) }
                },
                onSuccess = { d ->
                    _topProductsState.value = TopProductsState(
                        query = q, rows = d.items, pagination = d.pagination,
                        endReached = page.isLast(d.pagination), loaded = true
                    )
                }
            )
        }
    }

    fun loadMoreTopProducts() {
        val s = _topProductsState.value
        if (s.isLoading || s.isLoadingMore || s.endReached) return
        val next = s.pagination?.let { topProductsPage.loadMore(it) }
        if (next == null) { _topProductsState.update { it.copy(endReached = true) }; return }
        _topProductsState.update { it.copy(isLoadingMore = true) }
        val q = s.query
        viewModelScope.launch(Dispatchers.IO) {
            repository.getTopProducts(q.range, next).collectInto(
                onError = { m, _ -> _topProductsState.update { it.copy(isLoadingMore = false) }; snackbar(m ?: "Load failed") },
                onSuccess = { d ->
                    if (_topProductsState.value.query != q) return@collectInto
                    topProductsPage = next
                    _topProductsState.update {
                        it.copy(rows = it.rows + d.items, pagination = d.pagination, isLoadingMore = false, endReached = next.isLast(d.pagination))
                    }
                }
            )
        }
    }

    // ===========================
    // SALES HISTORY
    // ===========================
    fun getAllSellHistory(force: Boolean = false) {
        val s = _getSellHistory.value
        if (!force && s.success != null && !s.isLoading && s.error == null) return
        salesPage = PageRequest()
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllSellHistory(page = salesPage).collectInto(
                onLoading = { _getSellHistory.update { it.copy(isLoading = true, error = null, isLoadingMore = false) } },
                onError = { m, _ -> _getSellHistory.update { it.copy(isLoading = false, isLoadingMore = false, error = m ?: "Could not load") } },
                onSuccess = { d ->
                    _getSellHistory.value = GetSellHistoryState(
                        success = d, pagination = d.pagination, endReached = salesPage.isLast(d.pagination)
                    )
                }
            )
        }
    }

    fun loadMoreSellHistory() {
        val s = _getSellHistory.value
        if (s.isLoading || s.isLoadingMore || s.endReached) return
        val next = s.pagination?.let { salesPage.loadMore(it) }
        if (next == null) { _getSellHistory.update { it.copy(endReached = true) }; return }
        _getSellHistory.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllSellHistory(page = next).collectInto(
                onError = { m, _ -> _getSellHistory.update { it.copy(isLoadingMore = false) }; snackbar(m ?: "Load failed") },
                onSuccess = { d ->
                    salesPage = next
                    _getSellHistory.update {
                        it.copy(success = it.success.append(d), pagination = d.pagination, isLoadingMore = false, endReached = next.isLast(d.pagination))
                    }
                }
            )
        }
    }

    // ===========================
    // USERS
    // ===========================
    fun getAllUsers(force: Boolean = false) {
        val s = _getAllUserState.value
        if (!force && s.success != null && !s.isLoading && s.error == null) return
        usersPage = PageRequest()
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllUsers(page = usersPage).collectInto(
                onLoading = { _getAllUserState.update { it.copy(isLoading = true, error = null, isLoadingMore = false) } },
                onError = { m, _ -> _getAllUserState.update { it.copy(isLoading = false, isLoadingMore = false, error = m ?: "Could not load") } },
                onSuccess = { d ->
                    _getAllUserState.value = GetAllUserState(
                        success = d, pagination = d.pagination, endReached = usersPage.isLast(d.pagination)
                    )
                }
            )
        }
    }

    fun loadMoreUsers() {
        val s = _getAllUserState.value
        if (s.isLoading || s.isLoadingMore || s.endReached) return
        val next = s.pagination?.let { usersPage.loadMore(it) }
        if (next == null) { _getAllUserState.update { it.copy(endReached = true) }; return }
        _getAllUserState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllUsers(page = next).collectInto(
                onError = { m, _ -> _getAllUserState.update { it.copy(isLoadingMore = false) }; snackbar(m ?: "Load failed") },
                onSuccess = { d ->
                    usersPage = next
                    _getAllUserState.update {
                        it.copy(success = it.success.append(d), pagination = d.pagination, isLoadingMore = false, endReached = next.isLast(d.pagination))
                    }
                }
            )
        }
    }

    /** Soft delete; optimistic removal with rollback. */
    fun deleteUser(userId: String) {
        val current = _getAllUserState.value.success
        val userToDelete = current?.items?.find { it.userId == userId }
        val index = current?.items?.indexOfFirst { it.userId == userId } ?: -1

        if (current != null && userToDelete != null && index != -1) {
            _getAllUserState.update { it.copy(success = current.copy(items = current.items.removeItem(userId) { u -> u.userId })) }
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUser(userId).collectInto(
                onLoading = { _deleteUserState.value = DeleteUserState(isLoading = true) },
                onError = { m, _ ->
                    val rollback = _getAllUserState.value.success
                    if (rollback != null && userToDelete != null && index != -1) {
                        _getAllUserState.update { it.copy(success = rollback.copy(items = rollback.items.restoreItem(userToDelete, index))) }
                    }
                    _deleteUserState.value = DeleteUserState(error = m)
                    snackbar(m ?: "Delete failed")
                },
                onSuccess = {
                    _deleteUserState.value = DeleteUserState(success = it)
                    snackbar("User deleted successfully")
                }
            )
        }
    }

    /** true -> approve, false -> block. key = userId. */
    fun isApprovedUser(userId: String, isApproveds: Boolean) {
        _isApproved.update { it + (userId to IsApprovedUserState(isLoading = true)) }
        viewModelScope.launch(Dispatchers.IO) {
            repository.isApprovedUser(userId, isApproveds).collectInto(
                onError = { m, _ ->
                    snackbar(m ?: "Approval failed")
                    _isApproved.update { it + (userId to IsApprovedUserState(error = m)) }
                },
                onSuccess = { user -> onUserStatusChanged(userId, user, if (isApproveds) "User approved" else "User blocked") }
            )
        }
    }

    /** NEW. POST users/{id}/unblock. key = userId in [isApprovedUser]. */
    fun unblockUser(userId: String) {
        _isApproved.update { it + (userId to IsApprovedUserState(isLoading = true)) }
        viewModelScope.launch(Dispatchers.IO) {
            repository.unblockUser(userId).collectInto(
                onError = { m, _ ->
                    snackbar(m ?: "Unblock failed")
                    _isApproved.update { it + (userId to IsApprovedUserState(error = m)) }
                },
                onSuccess = { user -> onUserStatusChanged(userId, user, "User unblocked") }
            )
        }
    }

    private fun onUserStatusChanged(userId: String, user: com.saurabh.mediadminapp.data.v1.dto.User, message: String) {
        _getAllUserState.update { s ->
            val d = s.success ?: return@update s
            s.copy(success = d.copy(items = d.items.updateItem(userId, { it.userId }) { user }))
        }
        snackbar(message)
        _isApproved.update { it + (userId to IsApprovedUserState(success = user)) }
    }

    // ===========================
    // ORDERS
    // ===========================
    fun getAllOrders(force: Boolean = false) {
        val s = _getAllOrderState.value
        if (!force && s.success != null && !s.isLoading && s.error == null) return
        ordersPage = PageRequest()
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllOrders(page = ordersPage).collectInto(
                onLoading = { _getAllOrderState.update { it.copy(isLoading = true, error = null, isLoadingMore = false) } },
                onError = { m, _ -> _getAllOrderState.update { it.copy(isLoading = false, isLoadingMore = false, error = m ?: "Could not load") } },
                onSuccess = { d ->
                    _getAllOrderState.value = GetAllOrdersState(
                        success = d, pagination = d.pagination, endReached = ordersPage.isLast(d.pagination)
                    )
                }
            )
        }
    }

    fun loadMoreOrders() {
        val s = _getAllOrderState.value
        if (s.isLoading || s.isLoadingMore || s.endReached) return
        val next = s.pagination?.let { ordersPage.loadMore(it) }
        if (next == null) { _getAllOrderState.update { it.copy(endReached = true) }; return }
        _getAllOrderState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllOrders(page = next).collectInto(
                onError = { m, _ -> _getAllOrderState.update { it.copy(isLoadingMore = false) }; snackbar(m ?: "Load failed") },
                onSuccess = { d ->
                    ordersPage = next
                    _getAllOrderState.update {
                        it.copy(success = it.success.append(d), pagination = d.pagination, isLoadingMore = false, endReached = next.isLast(d.pagination))
                    }
                }
            )
        }
    }

    fun getUsersOrders(userId: String, force: Boolean = false) {
        val s = _getUsersOrdersState.value
        if (!force && lastUserOrdersId == userId && s.success != null && !s.isLoading && s.error == null) return
        lastUserOrdersId = userId
        viewModelScope.launch(Dispatchers.IO) {
            repository.getUserOrders(userId, page = PageRequest(limit = 100)).collectInto(
                onLoading = { _getUsersOrdersState.update { it.copy(isLoading = true, error = null) } },
                onError = { m, _ -> _getUsersOrdersState.update { it.copy(isLoading = false, error = m ?: "Could not load") } },
                onSuccess = { _getUsersOrdersState.value = GetUsersOrderState(success = it) }
            )
        }
    }

    fun getOrderById(orderId: String, force: Boolean = false) {
        val s = _getOrderByIdState.value
        if (!force && lastOrderId == orderId && s.success != null && !s.isLoading && s.error == null) return
        lastOrderId = orderId
        viewModelScope.launch(Dispatchers.IO) {
            repository.getOrdersById(orderId).collectInto(
                onLoading = { _getOrderByIdState.update { it.copy(isLoading = true, error = null) } },
                onError = { m, _ -> _getOrderByIdState.update { it.copy(isLoading = false, error = m ?: "Could not load") } },
                onSuccess = { _getOrderByIdState.value = GetOrderByIdState(success = it) }
            )
        }
    }

    /** Cancel (no hard delete in v1): the row stays and shows status CANCELLED. */
    fun deleteOrder(orderId: String, reason: String? = null) {
        if (_deleteOrderState.value.isLoading) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteOrder(orderId, reason).collectInto(
                onLoading = { _deleteOrderState.value = DeleteOrderState(isLoading = true) },
                onError = { m, e ->
                    _deleteOrderState.value = DeleteOrderState(error = m, appError = e)
                    snackbar(m ?: "Cancel failed")
                },
                onSuccess = { order ->
                    replaceOrderRow(order)
                    _getSellHistory.value = GetSellHistoryState()
                    getAllProduct(force = true)
                    _deleteOrderState.value = DeleteOrderState(success = order)
                    snackbar("Order cancelled")
                }
            )
        }
    }

    /** Edits patient/doctor name of a pending order. key = orderId. */
    fun updateOrder(orderId: String, patientName: String? = null, doctorName: String? = null) {
        _updateOrderState.update { it + (orderId to UpdateOrderState(isLoading = true)) }
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateOrder(orderId, patientName, doctorName).collectInto(
                onError = { m, e ->
                    snackbar(m ?: "Update failed")
                    _updateOrderState.update { it + (orderId to UpdateOrderState(error = m, appError = e)) }
                },
                onSuccess = { order ->
                    replaceOrderRow(order)
                    snackbar("Order updated")
                    _updateOrderState.update { it + (orderId to UpdateOrderState(success = order)) }
                }
            )
        }
    }

    /** true -> approve, false -> reject (with optional reason). key = orderId. */
    fun isApproveOrder(orderId: String, isApproved: Boolean, reason: String? = null) {
        _isApproveState.update { it + (orderId to ApproveOrderState(isLoading = true)) }
        viewModelScope.launch(Dispatchers.IO) {
            repository.approveOrder(orderId, isApproved, reason).collectInto(
                onError = { m, e ->
                    snackbar(m ?: "Approval failed")
                    _isApproveState.update { it + (orderId to ApproveOrderState(error = m, appError = e)) }
                },
                onSuccess = { order ->
                    replaceOrderRow(order)
                    _getSellHistory.value = GetSellHistoryState()
                    getAllProduct(force = true)
                    snackbar(if (isApproved) "Order approved" else "Order rejected")
                    _isApproveState.update { it + (orderId to ApproveOrderState(success = order)) }
                }
            )
        }
    }

    /** NEW. Sell an approved order (idempotent: same key reused on retry). key = orderId. */
    fun recordSell(orderId: String) {
        if (_getRecordSellHistory.value[orderId]?.isLoading == true) return
        val key = sellKeys.getOrPut(orderId) { idempotencyKeyProvider.newKey() }
        _getRecordSellHistory.update { it + (orderId to RecordSellHistoryState(isLoading = true)) }
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordSellHistory(orderId, key).collectInto(
                onError = { m, e ->
                    snackbar(m ?: "Sell failed")
                    _getRecordSellHistory.update { it + (orderId to RecordSellHistoryState(error = m, appError = e)) }
                },
                onSuccess = { order ->
                    sellKeys.remove(orderId)
                    replaceOrderRow(order)
                    _getSellHistory.value = GetSellHistoryState()
                    getAllOrders(force = true)
                    getAllProduct(force = true)
                    snackbar("Order sold")
                    _getRecordSellHistory.update { it + (orderId to RecordSellHistoryState(success = order)) }
                }
            )
        }
    }

    /** NEW. Return a sold order (restocks batches). key = orderId. */
    fun returnOrder(orderId: String, reason: String? = null) {
        if (_returnOrderState.value[orderId]?.isLoading == true) return
        _returnOrderState.update { it + (orderId to UpdateOrderState(isLoading = true)) }
        viewModelScope.launch(Dispatchers.IO) {
            repository.returnOrder(orderId, reason).collectInto(
                onError = { m, e ->
                    snackbar(m ?: "Return failed")
                    _returnOrderState.update { it + (orderId to UpdateOrderState(error = m, appError = e)) }
                },
                onSuccess = { order ->
                    replaceOrderRow(order)
                    _getSellHistory.value = GetSellHistoryState()
                    getAllProduct(force = true)
                    snackbar("Order returned")
                    _returnOrderState.update { it + (orderId to UpdateOrderState(success = order)) }
                }
            )
        }
    }

    /** NEW. Mark the prescription verified (required before selling Rx orders). key = orderId. */
    fun verifyPrescription(orderId: String, verified: Boolean = true) {
        if (_verifyPrescriptionState.value[orderId]?.isLoading == true) return
        _verifyPrescriptionState.update { it + (orderId to UpdateOrderState(isLoading = true)) }
        viewModelScope.launch(Dispatchers.IO) {
            repository.verifyPrescription(orderId, verified).collectInto(
                onError = { m, e ->
                    snackbar(m ?: "Verification failed")
                    _verifyPrescriptionState.update { it + (orderId to UpdateOrderState(error = m, appError = e)) }
                },
                onSuccess = { order ->
                    replaceOrderRow(order)
                    snackbar(if (verified) "Prescription verified" else "Prescription marked unverified")
                    _verifyPrescriptionState.update { it + (orderId to UpdateOrderState(success = order)) }
                }
            )
        }
    }

    /** NEW. Invoice of a sold/returned order. */
    fun getInvoice(orderId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getInvoice(orderId).collectInto(
                onLoading = { _invoiceState.value = UiState.Loading },
                onError = { m, e -> _invoiceState.value = UiState.Error(e ?: AppError.Unknown(Exception(m))) },
                onSuccess = { _invoiceState.value = UiState.Success(it) }
            )
        }
    }

    /** Combined invoice of a checkout (shares [invoiceState]; InvoiceScreen matches it by checkoutId). */
    fun getCheckoutInvoice(checkoutId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getCheckoutInvoice(checkoutId).collectInto(
                onLoading = { _invoiceState.value = UiState.Loading },
                onError = { m, e -> _invoiceState.value = UiState.Error(e ?: AppError.Unknown(Exception(m))) },
                onSuccess = { _invoiceState.value = UiState.Success(it) }
            )
        }
    }

    private val _checkoutGroups = MutableStateFlow(emptyMap<String, CheckoutGroupState>())

    /** Orders of each loaded checkout (key = checkoutId). */
    val checkoutGroups: StateFlow<Map<String, CheckoutGroupState>> = _checkoutGroups.asStateFlow()

    fun loadCheckoutOrders(checkoutId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllOrders(
                com.saurabh.mediadminapp.data.v1.repository.OrderFilter(checkoutId = checkoutId),
                PageRequest(limit = 100, sort = "created_at", order = "asc")
            ).collectInto(
                onLoading = { _checkoutGroups.update { it + (checkoutId to (it[checkoutId] ?: CheckoutGroupState()).copy(isLoading = true, error = null)) } },
                onError = { m, _ -> _checkoutGroups.update { it + (checkoutId to (it[checkoutId] ?: CheckoutGroupState()).copy(isLoading = false, error = m)) } },
                onSuccess = { page ->
                    _checkoutGroups.update {
                        it + (checkoutId to (it[checkoutId] ?: CheckoutGroupState()).copy(
                            isLoading = false, orders = page.items, total = page.pagination?.total ?: page.items.size, error = null
                        ))
                    }
                }
            )
        }
    }

    /** Approve every pending order of a checkout (one call per order); continues on failure. */
    fun approveCheckout(checkoutId: String) = runCheckoutAction(checkoutId, "approved", OrderStatus.PENDING) { id ->
        repository.approveOrder(id, true)
    }

    /** Sell every approved order of a checkout (new Idempotency-Key per order); continues on failure. */
    fun sellCheckout(checkoutId: String) = runCheckoutAction(checkoutId, "sold", OrderStatus.APPROVED) { id ->
        repository.recordSellHistory(id, idempotencyKeyProvider.newKey())
    }

    private fun runCheckoutAction(
        checkoutId: String,
        verb: String,
        from: OrderStatus,
        call: (String) -> Flow<ResultState<Order>>,
    ) {
        val group = _checkoutGroups.value[checkoutId] ?: return
        if (group.actionRunning) return
        val targets = group.orders.filter { it.status == from }
        if (targets.isEmpty()) return
        _checkoutGroups.update { it + (checkoutId to group.copy(actionRunning = true, actionSummary = null)) }
        viewModelScope.launch(Dispatchers.IO) {
            var ok = 0
            val failed = mutableListOf<String>()
            for (order in targets) {
                call(order.orderId).collectInto(
                    onError = { m, _ -> failed += "${order.productName}: ${m ?: "failed"}" },
                    onSuccess = { ok++ }
                )
            }
            val summary = buildString {
                append("$ok $verb")
                if (failed.isNotEmpty()) append(", ${failed.size} failed (${failed.joinToString("; ")})")
            }
            _checkoutGroups.update { it + (checkoutId to (it[checkoutId] ?: group).copy(actionRunning = false, actionSummary = summary)) }
            snackbar(summary)
            loadCheckoutOrders(checkoutId)
            getAllOrders(force = true)
            if (verb == "sold") getAllProduct(force = true)
        }
    }

    /** NEW. Server-side dashboard counters. */
    fun getDashboardSummary() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getDashboardSummary().collectInto(
                onLoading = { _dashboardSummaryState.value = UiState.Loading },
                onError = { m, e -> _dashboardSummaryState.value = UiState.Error(e ?: AppError.Unknown(Exception(m))) },
                onSuccess = { _dashboardSummaryState.value = UiState.Success(it) }
            )
        }
    }

    // ===========================
    // PRODUCTS
    // ===========================
    /** ProductList filters (q, category, schedule_type, in_stock, is_active) and sort. */
    private val _productFilter = MutableStateFlow(ProductFilter())
    val productFilter: StateFlow<ProductFilter> = _productFilter.asStateFlow()
    private val _productSort = MutableStateFlow(PageRequest(sort = "name", order = "asc"))
    val productSort: StateFlow<PageRequest> = _productSort.asStateFlow()
    private var productsJob: kotlinx.coroutines.Job? = null

    /** Replace filter and/or sort (`sort`: name|price|mrp|stock|created_at|updated_at) and reload page 1. */
    fun setProductFilter(filter: ProductFilter = _productFilter.value, sort: String? = _productSort.value.sort, order: String? = _productSort.value.order) {
        val newSort = PageRequest(sort = sort, order = order)
        if (filter == _productFilter.value && newSort == _productSort.value) return
        _productFilter.value = filter
        _productSort.value = newSort
        getAllProduct(force = true)
    }

    fun getAllProduct(force: Boolean = false) {
        val s = _getAllProduct.value
        if (!force && s.success != null && !s.isLoading && s.error == null) return
        productsPage = _productSort.value.copy(offset = 0)
        val filter = _productFilter.value
        val page = productsPage
        productsJob?.cancel()
        productsJob = viewModelScope.launch(Dispatchers.IO) {
            repository.getAllProduct(filter = filter, page = page).collectInto(
                // Keep the old list visible while a filter/refresh reloads.
                onLoading = { _getAllProduct.update { it.copy(isLoading = true, error = null, isLoadingMore = false) } },
                onError = { m, _ -> _getAllProduct.update { it.copy(isLoading = false, error = m ?: "Could not load products") } },
                onSuccess = { d ->
                    _getAllProduct.value = GetAllProductState(
                        success = d, pagination = d.pagination, endReached = page.isLast(d.pagination)
                    )
                }
            )
        }
    }

    fun loadMoreProducts() {
        val s = _getAllProduct.value
        if (s.isLoading || s.isLoadingMore || s.endReached) return
        val next = s.pagination?.let { productsPage.loadMore(it) }
        if (next == null) { _getAllProduct.update { it.copy(endReached = true) }; return }
        _getAllProduct.update { it.copy(isLoadingMore = true) }
        val filter = _productFilter.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllProduct(filter = filter, page = next).collectInto(
                onError = { m, _ -> _getAllProduct.update { it.copy(isLoadingMore = false) }; snackbar(m ?: "Load failed") },
                onSuccess = { d ->
                    if (_productFilter.value != filter) return@collectInto
                    productsPage = next
                    _getAllProduct.update {
                        it.copy(success = it.success.append(d), pagination = d.pagination, isLoadingMore = false, endReached = next.isLast(d.pagination))
                    }
                }
            )
        }
    }

    /** GET product-categories (first 100, by name) for ProductList filter chips. */
    private val _productCategories = MutableStateFlow<UiState<List<com.saurabh.mediadminapp.data.v1.dto.ProductCategory>>>(UiState.Empty)
    val productCategories: StateFlow<UiState<List<com.saurabh.mediadminapp.data.v1.dto.ProductCategory>>> = _productCategories.asStateFlow()

    fun loadProductCategories(force: Boolean = false) {
        if (!force && _productCategories.value is UiState.Success) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.getProductCategories(PageRequest(limit = 100, sort = "name", order = "asc")).collectInto(
                onLoading = { _productCategories.value = UiState.Loading },
                onError = { m, e -> _productCategories.value = UiState.Error(e ?: AppError.Unknown(Exception(m))) },
                onSuccess = { d -> _productCategories.value = if (d.items.isEmpty()) UiState.Empty else UiState.Success(d.items) }
            )
        }
    }

    /** GET products/by-barcode/{barcode} for BarcodeScanner. */
    private val _barcodeLookup = MutableStateFlow<BarcodeLookupState>(BarcodeLookupState.Idle)
    val barcodeLookup: StateFlow<BarcodeLookupState> = _barcodeLookup.asStateFlow()
    private var barcodeJob: kotlinx.coroutines.Job? = null

    fun lookupBarcode(barcode: String) {
        val code = barcode.trim()
        if (code.isEmpty() || _barcodeLookup.value is BarcodeLookupState.Loading) return
        barcodeJob?.cancel()
        barcodeJob = viewModelScope.launch(Dispatchers.IO) {
            repository.getProductByBarcode(code).collectInto(
                onLoading = { _barcodeLookup.value = BarcodeLookupState.Loading(code) },
                onError = { m, e ->
                    _barcodeLookup.value = if ((e as? AppError.Api)?.httpStatus == 404) BarcodeLookupState.NotFound(code)
                    else BarcodeLookupState.Failed(code, m ?: "Lookup failed", e)
                },
                onSuccess = { p -> _barcodeLookup.value = BarcodeLookupState.Found(code, p) }
            )
        }
    }

    fun clearBarcodeLookup() { barcodeJob?.cancel(); _barcodeLookup.value = BarcodeLookupState.Idle }

    /** Stock is not set here (v1 products start at 0; stock comes from purchases/batches). */
    fun addProduct(
        request: ProductCreateRequest,
        image: ImagePart? = null,
        openingStock: OpeningStock? = null
    ) {
        if (_addProductState.value.isLoading) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAddProduct(request, image).collectInto(
                onLoading = { _addProductState.value = AddProductState(isLoading = true) },
                onError = { m, e -> _addProductState.value = AddProductState(error = m, appError = e) },
                onSuccess = { created ->
                    if (openingStock != null) {
                        // Product exists now; a failed stock step must not hide that.
                        val req = StockPurchaseRequest(
                            batchNo = openingStock.batchNo, expiryDate = openingStock.expiryDate, quantity = openingStock.quantity,
                            purchasePrice = openingStock.purchasePrice, mrp = openingStock.mrp
                        )
                        repository.purchaseStock(created.productId, req, idempotencyKeyProvider.newKey()).collectInto(
                            onError = { m, _ -> snackbar("Product saved, but stock was not added: ${m ?: "error"}. Add it from the product page.") },
                            onSuccess = { snackbar("Product added with ${openingStock.quantity} in stock") }
                        )
                    }
                    _addProductState.value = AddProductState(success = created)
                    getAllProduct(force = true)
                }
            )
        }
    }

    // ---- Receive stock (POST products/{id}/stock/purchase) ----
    private val _stockPurchaseState = MutableStateFlow(StockPurchaseState())
    val stockPurchaseState: StateFlow<StockPurchaseState> = _stockPurchaseState.asStateFlow()
    /** One key per attempt, reused on retry, dropped on success/clear (no double receive on double tap). */
    private var purchaseKey: String? = null

    fun purchaseStock(productId: String, request: StockPurchaseRequest) {
        if (_stockPurchaseState.value.isLoading) return
        val key = purchaseKey ?: idempotencyKeyProvider.newKey().also { purchaseKey = it }
        viewModelScope.launch(Dispatchers.IO) {
            repository.purchaseStock(productId, request, key).collectInto(
                onLoading = { _stockPurchaseState.value = StockPurchaseState(isLoading = true) },
                onError = { m, e -> _stockPurchaseState.value = StockPurchaseState(error = m ?: "Could not add stock", appError = e) },
                onSuccess = { r ->
                    purchaseKey = null
                    _stockPurchaseState.value = StockPurchaseState(success = r)
                    snackbar("Added ${request.quantity} to stock")
                    loadProductBatches(productId, force = true)
                    getSpecificProduct(productId, force = true)
                    getAllProduct(force = true)
                }
            )
        }
    }

    fun clearStockPurchaseState() { purchaseKey = null; _stockPurchaseState.value = StockPurchaseState() }

    /** Batches of one product (edit-product stock section). */
    private val _productBatches = MutableStateFlow<UiState<List<com.saurabh.mediadminapp.data.v1.dto.Batch>>>(UiState.Empty)
    val productBatches: StateFlow<UiState<List<com.saurabh.mediadminapp.data.v1.dto.Batch>>> = _productBatches.asStateFlow()

    fun loadProductBatches(productId: String, force: Boolean = false) {
        val cur = _productBatches.value
        if (!force && cur is UiState.Success && cur.data.firstOrNull()?.productId == productId) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.getBatches(productId, includeExpired = true, page = PageRequest(limit = 100)).collectInto(
                onLoading = { if (_productBatches.value !is UiState.Success) _productBatches.value = UiState.Loading },
                onError = { m, e -> _productBatches.value = UiState.Error(e ?: AppError.Unknown(Exception(m))) },
                onSuccess = { d -> _productBatches.value = if (d.items.isEmpty()) UiState.Empty else UiState.Success(d.items) }
            )
        }
    }

    /** One batch (BatchDetail). Keeps the previous value while reloading. */
    private val _batchDetail = MutableStateFlow<UiState<com.saurabh.mediadminapp.data.v1.dto.Batch>>(UiState.Empty)
    val batchDetail: StateFlow<UiState<com.saurabh.mediadminapp.data.v1.dto.Batch>> = _batchDetail.asStateFlow()
    private val _batchAction = MutableStateFlow(BatchActionState())
    val batchAction: StateFlow<BatchActionState> = _batchAction.asStateFlow()

    fun getBatch(batchId: String, force: Boolean = false) {
        val cur = _batchDetail.value
        if (!force && cur is UiState.Success && cur.data.batchId == batchId) return
        if (cur is UiState.Success && cur.data.batchId != batchId) _batchDetail.value = UiState.Empty
        viewModelScope.launch(Dispatchers.IO) {
            repository.getBatch(batchId).collectInto(
                onLoading = { if (_batchDetail.value !is UiState.Success) _batchDetail.value = UiState.Loading },
                onError = { m, e -> if (_batchDetail.value !is UiState.Success) _batchDetail.value = UiState.Error(e ?: AppError.Unknown(Exception(m))) else snackbar(m ?: "Refresh failed") },
                onSuccess = { _batchDetail.value = UiState.Success(it) }
            )
        }
    }

    private fun afterBatchChange(b: com.saurabh.mediadminapp.data.v1.dto.Batch) {
        _batchDetail.value = UiState.Success(b)
        loadProductBatches(b.productId, force = true)
        getSpecificProduct(b.productId, force = true)
        getAllProduct(force = true)
    }

    /** PATCH batch metadata (not quantity). */
    fun updateBatch(batchId: String, request: com.saurabh.mediadminapp.data.v1.dto.BatchUpdateRequest) {
        if (_batchAction.value.isLoading) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBatch(batchId, request).collectInto(
                onLoading = { _batchAction.value = BatchActionState(isLoading = true) },
                onError = { m, e -> _batchAction.value = BatchActionState(error = m ?: "Update failed", appError = e) },
                onSuccess = { b -> _batchAction.value = BatchActionState(success = b); afterBatchChange(b); snackbar("Batch updated") }
            )
        }
    }

    /** Writes off the whole remaining quantity (EXPIRED_WRITEOFF). */
    fun writeOffBatch(batchId: String, note: String?) {
        if (_batchAction.value.isLoading) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.writeOffBatch(batchId, note?.trim()?.ifBlank { null }).collectInto(
                onLoading = { _batchAction.value = BatchActionState(isLoading = true) },
                onError = { m, e -> _batchAction.value = BatchActionState(error = m ?: "Write-off failed", appError = e) },
                onSuccess = { r -> _batchAction.value = BatchActionState(success = r.batch, wroteOff = true); afterBatchChange(r.batch); snackbar("Batch written off") }
            )
        }
    }

    fun clearBatchAction() { _batchAction.value = BatchActionState() }

    /** POST stock/adjust: signed [changeQty] on one batch (never below zero server-side). Shares stockPurchaseState. */
    fun adjustStock(productId: String, batchId: String, changeQty: Int, reason: String) {
        if (_stockPurchaseState.value.isLoading || changeQty == 0) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.adjustStock(productId, com.saurabh.mediadminapp.data.v1.dto.StockAdjustRequest(batchId, changeQty, reason.trim())).collectInto(
                onLoading = { _stockPurchaseState.value = StockPurchaseState(isLoading = true) },
                onError = { m, e -> _stockPurchaseState.value = StockPurchaseState(error = m ?: "Could not adjust stock", appError = e) },
                onSuccess = { r ->
                    _stockPurchaseState.value = StockPurchaseState(success = r)
                    loadProductBatches(productId, force = true)
                    getSpecificProduct(productId, force = true)
                    getAllProduct(force = true)
                }
            )
        }
    }

    /** PATCH: send only changed fields in [request] (null = unchanged). */
    fun updateProduct(
        productId: String,
        request: ProductUpdateRequest,
        image: ImagePart? = null
    ) {
        if (_updateProductState.value.isLoading) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProduct(productId, request, image).collectInto(
                onLoading = { _updateProductState.value = UpdateProductState(isLoading = true) },
                onError = { m, e -> _updateProductState.value = UpdateProductState(error = m, appError = e) },
                onSuccess = {
                    _updateProductState.value = UpdateProductState(success = it)
                    getAllProduct(force = true)
                    if (lastProductId == productId) lastProductId = null
                }
            )
        }
    }

    /** Deactivate (soft delete); optimistic removal with rollback. */
    fun deleteProduct(productId: String) {
        val current = _getAllProduct.value.success
        val itemToDelete = current?.items?.find { it.productId == productId }
        val index = current?.items?.indexOfFirst { it.productId == productId } ?: -1

        if (current != null && itemToDelete != null && index != -1) {
            _getAllProduct.update { it.copy(success = current.copy(items = current.items.removeItem(productId) { p -> p.productId })) }
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProduct(productId).collectInto(
                onLoading = { _deleteProductState.value = DeleteProductState(isLoading = true) },
                onError = { m, _ ->
                    val rollback = _getAllProduct.value.success
                    if (rollback != null && itemToDelete != null && index != -1) {
                        _getAllProduct.update { it.copy(success = rollback.copy(items = rollback.items.restoreItem(itemToDelete, index))) }
                    }
                    _deleteProductState.value = DeleteProductState(error = m)
                    snackbar(m ?: "Deactivate failed")
                },
                onSuccess = {
                    _deleteProductState.value = DeleteProductState(success = it)
                    snackbar("Product deactivated")
                }
            )
        }
    }

    /** NEW. Reactivate a deactivated product. */
    fun reactivateProduct(productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.reactivateProduct(productId).collectInto(
                onError = { m, _ -> snackbar(m ?: "Reactivate failed") },
                onSuccess = { p ->
                    if (_getSpecificProductState.value.success?.productId == productId) {
                        _getSpecificProductState.value = GetSpecificProductState(success = p)
                    }
                    getAllProduct(force = true)
                    snackbar("Product reactivated")
                }
            )
        }
    }

    fun getSpecificProduct(productId: String, force: Boolean = false) {
        val s = _getSpecificProductState.value
        if (!force && lastProductId == productId && s.success != null && !s.isLoading && s.error == null) return
        lastProductId = productId
        viewModelScope.launch(Dispatchers.IO) {
            repository.getSpecificProduct(productId).collectInto(
                onLoading = { _getSpecificProductState.update { it.copy(isLoading = true, error = null) } },
                onError = { m, _ -> _getSpecificProductState.update { it.copy(isLoading = false, error = m ?: "Could not load") } },
                onSuccess = { _getSpecificProductState.value = GetSpecificProductState(success = it) }
            )
        }
    }

    // ===========================
    // CLEAR HELPERS
    // ===========================
    fun clearApprovalState(userId: String? = null) {
        _isApproved.update { if (userId != null) it - userId else emptyMap() }
    }

    fun resetDeleteUserState() {
        _deleteUserState.value = DeleteUserState()
        _getAllUserState.value = GetAllUserState()
    }

    fun clearAddProductState() {
        _addProductState.value = AddProductState()
    }

    fun clearUpdateProductState() {
        _updateProductState.value = UpdateProductState()
    }

    fun clearGetSpecificProductState() {
        _getSpecificProductState.value = GetSpecificProductState()
        lastProductId = null
    }

    fun clearDeleteProductState() {
        _deleteProductState.value = DeleteProductState()
    }

    fun clearGetAllProductState() {
        _getAllProduct.value = GetAllProductState()
    }

    fun clearGetUsersOrdersState() {
        _getUsersOrdersState.value = GetUsersOrderState()
        lastUserOrdersId = null
    }

    fun clearGetOrderByIdState() {
        _getOrderByIdState.value = GetOrderByIdState()
        lastOrderId = null
    }

    fun clearUpdateOrderSteate() {
        _updateOrderState.value = emptyMap()
    }

    fun clearGetAllOrdersState() {
        _getAllOrderState.value = GetAllOrdersState()
    }

    fun clearLoginState() {
        _loginAdminState.value = LoginAdminState()
    }

    fun clearVerifyOtpState() {
        _verifyOtpState.value = VerifyOtpState()
    }

    // =====================================================================
    // INVENTORY ALERTS, STOCK MOVEMENTS, SUPPLIERS, AUDIT LOGS (Part 11)
    // Endpoints verified against response.md #44-48, #63-67, #71.
    // =====================================================================

    /** One paged list: state + current page + the fetch bound to the current filter [key]. */
    private class PagedSource<T>(val pageSize: Int = 20) {
        val state = MutableStateFlow(PagedListState<T>())
        var page = PageRequest(limit = pageSize)
        var key: Any? = Unit // Unit = never loaded
        var fetch: ((PageRequest) -> Flow<ResultState<PagedData<T>>>)? = null
        var job: kotlinx.coroutines.Job? = null

        fun reset() { job?.cancel(); state.value = PagedListState(); page = PageRequest(limit = pageSize); key = Unit; fetch = null }
    }

    /** First page for [key]; skipped when already loaded for the same key (unless [force]). Items clear when the key changes. */
    private fun <T> PagedSource<T>.load(
        key: Any?, force: Boolean, sort: String? = null, order: String? = null,
        fetch: (PageRequest) -> Flow<ResultState<PagedData<T>>>,
    ) {
        val s = state.value
        if (!force && key == this.key && s.loaded && s.error == null) return
        val keyChanged = key != this.key
        this.key = key
        this.fetch = fetch
        val p = PageRequest(limit = pageSize, sort = sort, order = order)
        page = p
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            fetch(p).collectInto(
                onLoading = {
                    state.update {
                        if (keyChanged) PagedListState(isLoading = true) else it.copy(isLoading = true, error = null, appError = null)
                    }
                },
                onError = { m, e -> state.update { it.copy(isLoading = false, isLoadingMore = false, error = m ?: "Could not load", appError = e) } },
                onSuccess = { d ->
                    state.value = PagedListState(items = d.items, pagination = d.pagination, endReached = p.isLast(d.pagination), loaded = true)
                }
            )
        }
    }

    /** Next page with the same filter; ignored while loading or at the end. */
    private fun <T> PagedSource<T>.more() {
        val s = state.value
        val f = fetch ?: return
        if (s.isLoading || s.isLoadingMore || s.endReached) return
        val next = s.pagination?.let { page.loadMore(it) }
        if (next == null) { state.update { it.copy(endReached = true) }; return }
        val forKey = key
        state.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch(Dispatchers.IO) {
            f(next).collectInto(
                onError = { m, _ -> state.update { it.copy(isLoadingMore = false) }; snackbar(m ?: "Load failed") },
                onSuccess = { d ->
                    if (key != forKey) return@collectInto // filter changed meanwhile
                    page = next
                    state.update {
                        it.copy(items = it.items + d.items, pagination = d.pagination, isLoadingMore = false, endReached = next.isLast(d.pagination))
                    }
                }
            )
        }
    }

    // ── Stock movements (GET stock-movements, newest first) ──
    private val stockMovementsSource = PagedSource<com.saurabh.mediadminapp.data.v1.dto.StockMovement>()
    val stockMovements: StateFlow<PagedListState<com.saurabh.mediadminapp.data.v1.dto.StockMovement>> = stockMovementsSource.state.asStateFlow()
    private val _stockMovementFilter = MutableStateFlow(com.saurabh.mediadminapp.data.v1.repository.StockMovementFilter())
    val stockMovementFilter: StateFlow<com.saurabh.mediadminapp.data.v1.repository.StockMovementFilter> = _stockMovementFilter.asStateFlow()

    /** [filter] null = keep the current one. reason: PURCHASE|SALE|ADJUSTMENT|EXPIRED_WRITEOFF|RETURN|CANCEL_RESTOCK. */
    fun loadStockMovements(filter: com.saurabh.mediadminapp.data.v1.repository.StockMovementFilter? = null, force: Boolean = false) {
        val f = filter ?: _stockMovementFilter.value
        _stockMovementFilter.value = f
        stockMovementsSource.load(f, force, sort = "created_at", order = "desc") { p -> repository.getStockMovements(f, p) }
    }

    fun loadMoreStockMovements() = stockMovementsSource.more()

    // ── Inventory alerts (GET inventory/low-stock | out-of-stock | near-expiry | expired) ──
    private val lowStockSource = PagedSource<com.saurabh.mediadminapp.data.v1.dto.Product>()
    private val outOfStockSource = PagedSource<com.saurabh.mediadminapp.data.v1.dto.Product>()
    private val nearExpirySource = PagedSource<com.saurabh.mediadminapp.data.v1.dto.Batch>()
    private val expiredSource = PagedSource<com.saurabh.mediadminapp.data.v1.dto.Batch>()
    val lowStock: StateFlow<PagedListState<com.saurabh.mediadminapp.data.v1.dto.Product>> = lowStockSource.state.asStateFlow()
    val outOfStock: StateFlow<PagedListState<com.saurabh.mediadminapp.data.v1.dto.Product>> = outOfStockSource.state.asStateFlow()
    val nearExpiry: StateFlow<PagedListState<com.saurabh.mediadminapp.data.v1.dto.Batch>> = nearExpirySource.state.asStateFlow()
    val expiredBatches: StateFlow<PagedListState<com.saurabh.mediadminapp.data.v1.dto.Batch>> = expiredSource.state.asStateFlow()

    /** Sorted per response.md: products by stock asc, batches by expiry_date asc. [nearExpiryDays] default 90 (server). */
    fun loadInventoryAlert(type: InventoryAlertType, force: Boolean = false, nearExpiryDays: Int? = null) {
        when (type) {
            InventoryAlertType.LOW_STOCK -> lowStockSource.load(type, force, "stock", "asc") { p -> repository.getLowStock(p) }
            InventoryAlertType.OUT_OF_STOCK -> outOfStockSource.load(type, force, "name", "asc") { p -> repository.getOutOfStock(p) }
            InventoryAlertType.NEAR_EXPIRY -> nearExpirySource.load(nearExpiryDays, force, "expiry_date", "asc") { p -> repository.getNearExpiry(nearExpiryDays, p) }
            InventoryAlertType.EXPIRED -> expiredSource.load(type, force, "expiry_date", "asc") { p -> repository.getExpired(p) }
        }
    }

    fun loadMoreInventoryAlert(type: InventoryAlertType) = when (type) {
        InventoryAlertType.LOW_STOCK -> lowStockSource.more()
        InventoryAlertType.OUT_OF_STOCK -> outOfStockSource.more()
        InventoryAlertType.NEAR_EXPIRY -> nearExpirySource.more()
        InventoryAlertType.EXPIRED -> expiredSource.more()
    }

    // ── Suppliers (GET/POST suppliers, GET/PATCH suppliers/{id}, POST .../deactivate) ──
    private val suppliersSource = PagedSource<com.saurabh.mediadminapp.data.v1.dto.Supplier>()
    val suppliers: StateFlow<PagedListState<com.saurabh.mediadminapp.data.v1.dto.Supplier>> = suppliersSource.state.asStateFlow()
    private val _supplierQuery = MutableStateFlow(SupplierQuery())
    val supplierQuery: StateFlow<SupplierQuery> = _supplierQuery.asStateFlow()

    fun loadSuppliers(query: SupplierQuery? = null, force: Boolean = false) {
        val q = query ?: _supplierQuery.value
        _supplierQuery.value = q
        suppliersSource.load(q, force, "name", "asc") { p -> repository.getSuppliers(q.q.trim().ifBlank { null }, q.isActive, p) }
    }

    fun loadMoreSuppliers() = suppliersSource.more()

    private val _supplierDetail = MutableStateFlow<UiState<com.saurabh.mediadminapp.data.v1.dto.Supplier>>(UiState.Empty)
    val supplierDetail: StateFlow<UiState<com.saurabh.mediadminapp.data.v1.dto.Supplier>> = _supplierDetail.asStateFlow()

    /** Shows the cached list row at once (if any) and refreshes from GET suppliers/{id}. */
    fun getSupplier(supplierId: String, force: Boolean = false) {
        val cur = _supplierDetail.value
        if (!force && cur is UiState.Success && cur.data.supplierId == supplierId) return
        val cached = suppliersSource.state.value.items.firstOrNull { it.supplierId == supplierId }
        _supplierDetail.value = cached?.let { UiState.Success(it) } ?: UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            repository.getSupplier(supplierId).collectInto(
                onError = { m, e ->
                    if (_supplierDetail.value !is UiState.Success) _supplierDetail.value = UiState.Error(e ?: AppError.Unknown(Exception(m)))
                    else snackbar(m ?: "Could not refresh supplier")
                },
                onSuccess = { s -> _supplierDetail.value = UiState.Success(s) }
            )
        }
    }

    private val _supplierAction = MutableStateFlow(SupplierActionState())
    val supplierAction: StateFlow<SupplierActionState> = _supplierAction.asStateFlow()

    fun clearSupplierAction() { _supplierAction.value = SupplierActionState(seq = _supplierAction.value.seq) }

    /** Puts the new/updated supplier into the list + detail so screens update without a reload. */
    private fun onSupplierChanged(s: com.saurabh.mediadminapp.data.v1.dto.Supplier, created: Boolean) {
        suppliersSource.state.update { st ->
            val exists = st.items.any { it.supplierId == s.supplierId }
            val q = _supplierQuery.value
            val visible = q.isActive == null || q.isActive == s.isActive
            val items = when {
                exists && visible -> st.items.updateItem(s.supplierId, { it.supplierId }) { s }
                exists -> st.items.filterNot { it.supplierId == s.supplierId }
                created && visible -> listOf(s) + st.items
                else -> st.items
            }
            st.copy(items = items)
        }
        _supplierDetail.update { d -> if (d is UiState.Success && d.data.supplierId != s.supplierId) d else UiState.Success(s) }
    }

    private fun runSupplierAction(
        flow: Flow<ResultState<com.saurabh.mediadminapp.data.v1.dto.Supplier>>,
        created: Boolean,
        successMessage: String,
    ) {
        if (_supplierAction.value.isLoading) return
        val seq = _supplierAction.value.seq + 1
        _supplierAction.value = SupplierActionState(seq = seq, isLoading = true)
        viewModelScope.launch(Dispatchers.IO) {
            flow.collectInto(
                onError = { m, e ->
                    _supplierAction.value = SupplierActionState(seq = seq, error = m ?: "Request failed", appError = e)
                    snackbar(m ?: "Request failed")
                },
                onSuccess = { s ->
                    onSupplierChanged(s, created)
                    _supplierAction.value = SupplierActionState(seq = seq, success = s)
                    snackbar(successMessage)
                }
            )
        }
    }

    /** POST suppliers. name required (max 200); gst_number = 15-char GSTIN. */
    fun createSupplier(request: com.saurabh.mediadminapp.data.v1.dto.SupplierCreateRequest) =
        runSupplierAction(repository.createSupplier(request), created = true, successMessage = "Supplier ${request.name} added")

    /** PATCH suppliers/{id}: send only changed fields (name is always sent; response.md marks it required). */
    fun updateSupplier(supplierId: String, request: com.saurabh.mediadminapp.data.v1.dto.SupplierUpdateRequest) =
        runSupplierAction(repository.updateSupplier(supplierId, request), created = false, successMessage = "Supplier updated")

    /** POST suppliers/{id}/deactivate (409 CONFLICT when already inactive; no reactivate endpoint). */
    fun deactivateSupplier(supplierId: String) =
        runSupplierAction(repository.deactivateSupplier(supplierId), created = false, successMessage = "Supplier deactivated")

    // ── Audit logs (GET audit-logs, newest first) ──
    private val auditSource = PagedSource<com.saurabh.mediadminapp.data.v1.dto.AuditLog>()
    val auditLogs: StateFlow<PagedListState<com.saurabh.mediadminapp.data.v1.dto.AuditLog>> = auditSource.state.asStateFlow()
    private val _auditFilter = MutableStateFlow(com.saurabh.mediadminapp.data.v1.repository.AuditFilter())
    val auditFilter: StateFlow<com.saurabh.mediadminapp.data.v1.repository.AuditFilter> = _auditFilter.asStateFlow()

    fun loadAuditLogs(filter: com.saurabh.mediadminapp.data.v1.repository.AuditFilter? = null, force: Boolean = false) {
        val f = filter ?: _auditFilter.value
        _auditFilter.value = f
        auditSource.load(f, force, sort = "created_at", order = "desc") { p -> repository.getAuditLogs(f, p) }
    }

    fun loadMoreAuditLogs() = auditSource.more()

    /** Called from [resetAllStatesOnLogout]. */
    private fun resetInventoryAdminData() {
        listOf(stockMovementsSource, lowStockSource, outOfStockSource, nearExpirySource, expiredSource, suppliersSource, auditSource)
            .forEach { it.reset() }
        _stockMovementFilter.value = com.saurabh.mediadminapp.data.v1.repository.StockMovementFilter()
        _supplierQuery.value = SupplierQuery()
        _supplierDetail.value = UiState.Empty
        _supplierAction.value = SupplierActionState()
        _auditFilter.value = com.saurabh.mediadminapp.data.v1.repository.AuditFilter()
    }
}
