package com.saurabh.mediadminapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.mediadminapp.common.ResultState
import com.saurabh.mediadminapp.network.TokenManager
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
import com.saurabh.mediadminapp.utils.ScreensState.GetDeleteSellHistoryState
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
import com.saurabh.mediadminapp.utils.ScreensState.UpdateUserState
import com.saurabh.mediadminapp.utils.ScreensState.VerifyOtpState
import com.saurabh.mediadminapp.utils.UiEvent
import com.saurabh.mediadminapp.utils.removeItem
import com.saurabh.mediadminapp.utils.restoreItem
import com.saurabh.mediadminapp.utils.updateItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: Repository,
    private val tokenManager: TokenManager
) : ViewModel() {

    // FIX 1: Tri-State (null = checking, true = logged in, false = logged out)
    // Isse app start par Auth screen flash bilkul band ho jayega!
    private val _isAdminLoggedIn = MutableStateFlow<Boolean?>(null)
    val isAdminLoggedIn = _isAdminLoggedIn.asStateFlow()

    private val _loggedInAdminId = MutableStateFlow<String?>(null)
    val loggedInAdminId = _loggedInAdminId.asStateFlow()

    // ===========================
    // 1. AUTHENTICATION STATES (All 'val', never 'var')
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

    private val _isApproved = MutableStateFlow(emptyMap<String, IsApprovedUserState>())
    val isApprovedUser = _isApproved.asStateFlow()

    private val _updateOrderState = MutableStateFlow(emptyMap<String, UpdateOrderState>())
    val updateOrderState = _updateOrderState.asStateFlow()

    private val _deleteUserState = MutableStateFlow(DeleteUserState())
    val deleteUserState = _deleteUserState.asStateFlow()

    private val _updateUserState = MutableStateFlow(UpdateUserState())
    val updateUserState = _updateUserState.asStateFlow()

    private val _getAllOrderState = MutableStateFlow(GetAllOrdersState())
    val getAllOrderState = _getAllOrderState.asStateFlow()

    private val _getUsersOrdersState = MutableStateFlow(GetUsersOrderState())
    val getUsersOrdersState = _getUsersOrdersState.asStateFlow()

    private val _deleteOrderState = MutableStateFlow(DeleteOrderState())
    val deleteOrderState = _deleteOrderState.asStateFlow()

    private val _isApproveState = MutableStateFlow(emptyMap<String, ApproveOrderState>())
    val isApproveOrdder = _isApproveState.asStateFlow()

    private val _getSellHistory = MutableStateFlow(GetSellHistoryState())
    val getSellHistory = _getSellHistory.asStateFlow()

    private val _getRecordSellHistory = MutableStateFlow(RecordSellHistoryState())
    val getRecordSellHistory = _getRecordSellHistory.asStateFlow()

    private val _getProductSellHistory = MutableStateFlow(GetProductSellHistoryState())
    val getProductSellHistory = _getProductSellHistory.asStateFlow()

    private val _getUserSellHistory = MutableStateFlow(GetUserSellHistoryState())
    val getUserSellHistory = _getUserSellHistory.asStateFlow()

    private val _deleteSellHistory = MutableStateFlow(GetDeleteSellHistoryState())
    val deleteSellHistory = _deleteSellHistory.asStateFlow()

    private val _getOrderByIdState = MutableStateFlow(GetOrderByIdState())
    val getOrderByIdState = _getOrderByIdState.asStateFlow()

    // FIX 2: Track last requested IDs for parameterized queries so clicking a different item works!
    private var lastProductId: String? = null
    private var lastUserOrdersId: String? = null
    private var lastOrderId: String? = null

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        checkLoginStatus()
        observeSessionExpiry()
    }

    private fun observeSessionExpiry() {
        viewModelScope.launch {
            tokenManager.sessionExpiredEvent.collect {
                Log.w("MyViewModel", "Session expired event received — forcing logout")
                resetAllStatesOnLogout()
            }
        }
    }

    fun checkLoginStatus() {
        // Synchronous fast check if SharedPreferences is already in memory, or IO fallback
        viewModelScope.launch(Dispatchers.IO) {
            _loggedInAdminId.value = tokenManager.getAdminId()
            _isAdminLoggedIn.value = tokenManager.isLoggedIn()
        }
    }

    fun setAdminLoggedIn() {
        _loggedInAdminId.value = tokenManager.getAdminId()
        _isAdminLoggedIn.value = true
    }

    fun setAdminLoggedOut() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // optional network logout
            } finally {
                tokenManager.clearTokens()
                resetAllStatesOnLogout()
            }
        }
    }

    // FIX 3: Master Reset Function so re-logging in never shows stale data or locked buttons
    private fun resetAllStatesOnLogout() {
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
        lastProductId = null
        lastUserOrdersId = null
        lastOrderId = null
    }

    // ===========================
    // AUTH ACTIONS (Fixed Guard Conditions)
    // ===========================
    fun createAdmin(
        name: String,
        email: String,
        password: String,
        phoneNumber: String,
        image: MultipartBody.Part? = null
    ) {
        if (_createAdminState.value.isLoading) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.createAdmin(name, password, email, phoneNumber, image).collect { result ->
                when (result) {
                    is ResultState.Loading -> _createAdminState.value =
                        CreateAdminState(isLoading = true)

                    is ResultState.Error -> _createAdminState.value =
                        CreateAdminState(error = result.exception.message)

                    is ResultState.Success -> _createAdminState.value =
                        CreateAdminState(success = result.data)
                }
            }
        }
    }

    fun loginAdmin(email: String, password: String) {
        if (_loginAdminState.value.isLoading) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.loginAdmin(email, password).collect { result ->
                when (result) {
                    is ResultState.Loading -> _loginAdminState.value =
                        LoginAdminState(isLoading = true)

                    is ResultState.Error -> _loginAdminState.value =
                        LoginAdminState(error = result.exception.message)

                    is ResultState.Success -> _loginAdminState.value =
                        LoginAdminState(success = result.data)
                }
            }
        }
    }

    fun verifyAdminOtp(adminId: String, otp: String) {
        if (_verifyOtpState.value.isLoading) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.verifyAdminOtp(adminId, otp).collect { result ->
                when (result) {
                    is ResultState.Loading -> _verifyOtpState.value =
                        VerifyOtpState(isLoading = true)

                    is ResultState.Error -> _verifyOtpState.value =
                        VerifyOtpState(error = result.exception.message)

                    is ResultState.Success -> {
                        val data = result.data
                        if (data.status == 200 && data.access_token != null) {
                            tokenManager.saveTokens(
                                accessToken = data.access_token,
                                refreshToken = data.refresh_token ?: "",
                                role = data.role ?: "",
                                adminId = adminId
                            )
                            setAdminLoggedIn()
                            _verifyOtpState.value = VerifyOtpState(success = data)
                        } else {
                            val errMsg = data.message ?: "OTP verification failed"
                            _verifyOtpState.value = VerifyOtpState(error = errMsg)
                        }
                    }
                }
            }
        }
    }

    fun requestPasswordReset(email: String) {
        if (_passwordResetState.value.isLoading) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.requestAdminPasswordReset(email).collect { result ->
                when (result) {
                    is ResultState.Loading -> _passwordResetState.value =
                        PasswordResetState(isLoading = true)

                    is ResultState.Error -> _passwordResetState.value =
                        PasswordResetState(error = result.exception.message)

                    is ResultState.Success -> _passwordResetState.value =
                        PasswordResetState(success = result.data)
                }
            }
        }
    }

    fun resetPasswordOtp(userId: String? = null, otp: String, newPassword: String) {
        if (_passwordResetOtpState.value.isLoading) return
        val targetAdminId = userId ?: _passwordResetState.value.success?.userId.orEmpty()
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetAdminPasswordWithOtp(targetAdminId, otp, newPassword)
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> _passwordResetOtpState.value =
                            PasswordResetOtpState(isLoading = true)

                        is ResultState.Error -> _passwordResetOtpState.value =
                            PasswordResetOtpState(error = result.exception.message)

                        is ResultState.Success -> _passwordResetOtpState.value =
                            PasswordResetOtpState(success = result.data)
                    }
                }
        }
    }

    fun getAllAdmin(force: Boolean = false) {
        val s = _getAllAdminState.value
        if (!force && s.success != null && !s.isLoading && s.error == null) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllAdmins().collect { result ->
                when (result) {
                    is ResultState.Loading -> _getAllAdminState.value =
                        GetAllAdminState(isLoading = true)

                    is ResultState.Error -> _getAllAdminState.value =
                        GetAllAdminState(error = result.exception.message)

                    is ResultState.Success -> _getAllAdminState.value =
                        GetAllAdminState(success = result.data)
                }
            }
        }
    }

    fun updateAdmin(
        adminId: String,
        name: String? = null,
        password: String? = null,
        email: String? = null,
        phoneNumber: String? = null,
        image: MultipartBody.Part? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAdmin(adminId, name, password, email, phoneNumber, image)
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> {}
                        is ResultState.Error -> _uiEvent.trySend(
                            UiEvent.ShowSnackbar(
                                result.exception.message ?: "Update failed"
                            )
                        )

                        is ResultState.Success -> {
                            getAllAdmin(force = true) // Force refresh admin list!
                            _uiEvent.trySend(UiEvent.ShowSnackbar("Admin updated successfully"))
                        }
                    }
                }
        }
    }

    // ===========================
    // HISTORY, USER, ORDER & PRODUCT FUNCTIONS
    // ===========================
    fun getAllSellHistory(force: Boolean = false) {
        val s = _getSellHistory.value
        if (!force && s.success != null && !s.isLoading && s.error == null) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllSellHistory().collect { sellHistory ->
                when (sellHistory) {
                    is ResultState.Loading -> _getSellHistory.value =
                        GetSellHistoryState(isLoading = true)

                    is ResultState.Error -> _getSellHistory.value =
                        GetSellHistoryState(error = sellHistory.exception.message)

                    is ResultState.Success -> _getSellHistory.value =
                        GetSellHistoryState(success = sellHistory.data)
                }
            }
        }
    }

    fun getAllUsers(force: Boolean = false) {
        val s = _getAllUserState.value
        if (!force && s.success != null && !s.isLoading && s.error == null) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllUsers().collect {
                when (it) {
                    is ResultState.Loading -> _getAllUserState.value =
                        GetAllUserState(isLoading = true)

                    is ResultState.Error -> _getAllUserState.value =
                        GetAllUserState(error = it.exception.message)

                    is ResultState.Success -> _getAllUserState.value =
                        GetAllUserState(success = it.data)
                }
            }
        }
    }

    fun deleteUser(userId: String) {
        val current = _getAllUserState.value.success
        val userToDelete = current?.users?.find { it.user_id == userId }
        val index = current?.users?.indexOfFirst { it.user_id == userId } ?: -1

        if (current != null && userToDelete != null && index != -1) {
            val updated = current.copy(users = current.users.removeItem(userId) { it.user_id })
            _getAllUserState.value = GetAllUserState(success = updated)
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUser(userId).collect { result ->
                when (result) {
                    is ResultState.Loading -> _deleteUserState.value =
                        DeleteUserState(isLoading = true)

                    is ResultState.Error -> {
                        val currentRollback = _getAllUserState.value.success
                        if (currentRollback != null && userToDelete != null && index != -1) {
                            val reverted = currentRollback.copy(
                                users = currentRollback.users.restoreItem(
                                    userToDelete,
                                    index
                                )
                            )
                            _getAllUserState.value = GetAllUserState(success = reverted)
                        }
                        _deleteUserState.value = DeleteUserState(error = result.exception.message)
                        _uiEvent.trySend(
                            UiEvent.ShowSnackbar(
                                result.exception.message ?: "Delete failed"
                            )
                        )
                    }

                    is ResultState.Success -> {
                        _deleteUserState.value = DeleteUserState(success = result.data)
                        _uiEvent.trySend(UiEvent.ShowSnackbar("User deleted successfully"))
                    }
                }
            }
        }
    }

    fun getAllOrders(force: Boolean = false) {
        val s = _getAllOrderState.value
        if (!force && s.success != null && !s.isLoading && s.error == null) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllOrders().collect { order ->
                when (order) {
                    is ResultState.Loading -> _getAllOrderState.value =
                        GetAllOrdersState(isLoading = true)

                    is ResultState.Error -> _getAllOrderState.value =
                        GetAllOrdersState(error = order.exception.message)

                    is ResultState.Success -> _getAllOrderState.value =
                        GetAllOrdersState(success = order.data)
                }
            }
        }
    }

    fun getUsersOrders(userId: String, force: Boolean = false) {
        val s = _getUsersOrdersState.value
        if (!force && lastUserOrdersId == userId && s.success != null && !s.isLoading && s.error == null) return
        lastUserOrdersId = userId

        viewModelScope.launch(Dispatchers.IO) {
            repository.getUserOrders(userId).collect {
                when (it) {
                    is ResultState.Loading -> _getUsersOrdersState.value =
                        GetUsersOrderState(isLoading = true)

                    is ResultState.Error -> _getUsersOrdersState.value =
                        GetUsersOrderState(error = it.exception.message)

                    is ResultState.Success -> _getUsersOrdersState.value =
                        GetUsersOrderState(success = it.data)
                }
            }
        }
    }

    fun getOrderById(orderId: String, force: Boolean = false) {
        val s = _getOrderByIdState.value
        if (!force && lastOrderId == orderId && s.success != null && !s.isLoading && s.error == null) return
        lastOrderId = orderId

        viewModelScope.launch(Dispatchers.IO) {
            repository.getOrdersById(orderId).collect { order ->
                when (order) {
                    is ResultState.Loading -> _getOrderByIdState.value =
                        GetOrderByIdState(isLoading = true)

                    is ResultState.Error -> _getOrderByIdState.value =
                        GetOrderByIdState(error = order.exception.message)

                    is ResultState.Success -> _getOrderByIdState.value =
                        GetOrderByIdState(success = order.data)
                }
            }
        }
    }

    fun deleteOrder(orderId: String) {
        val current = _getAllOrderState.value.success
        val itemToDelete = current?.orders?.find { it.order_id == orderId }
        val index = current?.orders?.indexOfFirst { it.order_id == orderId } ?: -1

        if (current != null && itemToDelete != null && index != -1) {
            val updated = current.copy(orders = current.orders.removeItem(orderId) { it.order_id })
            _getAllOrderState.value = GetAllOrdersState(success = updated)
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteOrder(orderId).collect { result ->
                when (result) {
                    is ResultState.Loading -> _deleteOrderState.value =
                        DeleteOrderState(isLoading = true)

                    is ResultState.Error -> {
                        val currentRollback = _getAllOrderState.value.success
                        if (currentRollback != null && itemToDelete != null && index != -1) {
                            val reverted = currentRollback.copy(
                                orders = currentRollback.orders.restoreItem(
                                    itemToDelete,
                                    index
                                )
                            )
                            _getAllOrderState.value = GetAllOrdersState(success = reverted)
                        }
                        _deleteOrderState.value = DeleteOrderState(error = result.exception.message)
                        _uiEvent.trySend(
                            UiEvent.ShowSnackbar(
                                result.exception.message ?: "Delete failed"
                            )
                        )
                    }

                    is ResultState.Success -> {
                        _deleteOrderState.value = DeleteOrderState(success = result.data)
                        _uiEvent.trySend(UiEvent.ShowSnackbar("Order deleted"))
                    }
                }
            }
        }
    }

    fun getAllProduct(force: Boolean = false) {
        val s = _getAllProduct.value
        if (!force && s.success != null && !s.isLoading && s.error == null) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllProduct().collect {
                when (it) {
                    is ResultState.Loading -> _getAllProduct.value =
                        GetAllProductState(isLoading = true)

                    is ResultState.Error -> _getAllProduct.value =
                        GetAllProductState(error = it.exception.message)

                    is ResultState.Success -> _getAllProduct.value =
                        GetAllProductState(success = it.data)
                }
            }
        }
    }

    fun addProduct(
        name: String,
        price: Double,
        category: String,
        stock: Int,
        image: MultipartBody.Part? = null
    ) {
        if (_addProductState.value.isLoading) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.getAddProduct(name, price, category, stock, image).collect {
                when (it) {
                    is ResultState.Loading -> _addProductState.value =
                        AddProductState(isLoading = true)

                    is ResultState.Error -> _addProductState.value =
                        AddProductState(error = it.exception.message)

                    is ResultState.Success -> {
                        _addProductState.value = AddProductState(success = it.data)
                        // AUTO-SYNC: Product add hote hi list ko background mein refresh kar lo!
                        getAllProduct(force = true)
                    }
                }
            }
        }
    }

    fun updateProduct(
        productId: String,
        name: String? = null,
        price: Double? = null,
        category: String? = null,
        stock: Int? = null,
        image: MultipartBody.Part? = null
    ) {
        if (_updateProductState.value.isLoading) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProduct(productId, name, price, category, stock, image).collect {
                when (it) {
                    is ResultState.Loading -> _updateProductState.value =
                        UpdateProductState(isLoading = true)

                    is ResultState.Error -> _updateProductState.value =
                        UpdateProductState(error = it.exception.message)

                    is ResultState.Success -> {
                        _updateProductState.value = UpdateProductState(success = it.data)
                        // AUTO-SYNC: Product update hote hi master list aur detail invalidate/refresh karo!
                        getAllProduct(force = true)
                        if (lastProductId == productId) lastProductId = null
                    }
                }
            }
        }
    }

    fun deleteProduct(productId: String) {
        val current = _getAllProduct.value.success
        val itemToDelete = current?.products?.find { it.Product_id == productId }
        val index = current?.products?.indexOfFirst { it.Product_id == productId } ?: -1

        if (current != null && itemToDelete != null && index != -1) {
            val updated =
                current.copy(products = current.products.removeItem(productId) { it.Product_id })
            _getAllProduct.value = GetAllProductState(success = updated)
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProduct(productId).collect { result ->
                when (result) {
                    is ResultState.Loading -> _deleteProductState.value =
                        DeleteProductState(isLoading = true)

                    is ResultState.Error -> {
                        val currentRollback = _getAllProduct.value.success
                        if (currentRollback != null && itemToDelete != null && index != -1) {
                            val reverted = currentRollback.copy(
                                products = currentRollback.products.restoreItem(
                                    itemToDelete,
                                    index
                                )
                            )
                            _getAllProduct.value = GetAllProductState(success = reverted)
                        }
                        _deleteProductState.value =
                            DeleteProductState(error = result.exception.message)
                        _uiEvent.trySend(
                            UiEvent.ShowSnackbar(
                                result.exception.message ?: "Delete failed"
                            )
                        )
                    }

                    is ResultState.Success -> {
                        _deleteProductState.value = DeleteProductState(success = result.data)
                        _uiEvent.trySend(UiEvent.ShowSnackbar("Product deleted"))
                    }
                }
            }
        }
    }

    fun getSpecificProduct(productId: String, force: Boolean = false) {
        val s = _getSpecificProductState.value
        if (!force && lastProductId == productId && s.success != null && !s.isLoading && s.error == null) return
        lastProductId = productId

        viewModelScope.launch(Dispatchers.IO) {
            repository.getSpecificProduct(productId).collect {
                when (it) {
                    is ResultState.Loading -> _getSpecificProductState.value =
                        GetSpecificProductState(isLoading = true)

                    is ResultState.Error -> _getSpecificProductState.value =
                        GetSpecificProductState(error = it.exception.message)

                    is ResultState.Success -> _getSpecificProductState.value =
                        GetSpecificProductState(success = it.data)
                }
            }
        }
    }

    fun updateUser(
        userId: String,
        name: String? = null,
        password: String? = null,
        isApproved: Boolean? = null,
        block: Boolean? = null,
        address: String? = null,
        email: String? = null,
        phonenumber: String? = null,
        pincode: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUser(
                userId,
                name,
                password,
                isApproved,
                block,
                address,
                email,
                phonenumber,
                pincode
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> _updateUserState.value =
                        UpdateUserState(isLoading = true)

                    is ResultState.Error -> {
                        _updateUserState.value = UpdateUserState(error = result.exception.message)
                        _uiEvent.trySend(
                            UiEvent.ShowSnackbar(
                                result.exception.message ?: "Update failed"
                            )
                        )
                    }

                    is ResultState.Success -> {
                        val current = _getAllUserState.value.success
                        if (current != null) {
                            val updated = current.copy(
                                users = current.users.updateItem(userId, { it.user_id }) { user ->
                                    user.copy(
                                        name = name ?: user.name,
                                        email = email ?: user.email,
                                        phone_number = phonenumber ?: user.phone_number,
                                        address = address ?: user.address,
                                        pin_code = pincode ?: user.pin_code,
                                        _isApproved = isApproved ?: user._isApproved,
                                        _block = block ?: user._block
                                    )
                                }
                            )
                            _getAllUserState.value = GetAllUserState(success = updated)
                        }
                        _updateUserState.value = UpdateUserState(success = result.data)
                        _uiEvent.trySend(UiEvent.ShowSnackbar("User updated"))
                    }
                }
            }
        }
    }

    fun updateOrder(
        orderId: String,
        isApproved: Boolean? = null,
        quantity: Int? = null,
        price: Float? = null
    ) {
        val isApprovedInt = isApproved?.let { if (it) 1 else 0 }
        // Atomic thread-safe map update
        _updateOrderState.update { it + (orderId to UpdateOrderState(isLoading = true)) }

        viewModelScope.launch(Dispatchers.IO) {
            repository.updateOrder(orderId, isApprovedInt, quantity, price).collect { result ->
                val newState = when (result) {
                    is ResultState.Loading -> UpdateOrderState(isLoading = true)
                    is ResultState.Error -> {
                        _uiEvent.trySend(
                            UiEvent.ShowSnackbar(
                                result.exception.message ?: "Update failed"
                            )
                        )
                        UpdateOrderState(error = result.exception.message)
                    }

                    is ResultState.Success -> {
                        val current = _getAllOrderState.value.success
                        if (current != null) {
                            val updated = current.copy(
                                orders = current.orders.updateItem(
                                    orderId,
                                    { it.order_id }) { order ->
                                    order.copy(
                                        _isApproved = isApproved ?: order._isApproved,
                                        quantity = quantity ?: order.quantity,
                                        price = price?.toDouble() ?: order.price
                                    )
                                }
                            )
                            _getAllOrderState.value = GetAllOrdersState(success = updated)
                        }
                        // AUTO-SYNC: Order change hone par Product stock aur Sell History ko sync karo!
                        _getSellHistory.value = GetSellHistoryState()
                        getAllProduct(force = true)

                        _uiEvent.trySend(UiEvent.ShowSnackbar("Order updated"))
                        UpdateOrderState(success = result.data)
                    }
                }
                _updateOrderState.update { it + (orderId to newState) }
            }
        }
    }

    fun isApprovedUser(userId: String, isApproveds: Boolean) {
        _isApproved.update { it + (userId to IsApprovedUserState(isLoading = true)) }

        viewModelScope.launch(Dispatchers.IO) {
            repository.isApprovedUser(userId, isApproveds).collect { result ->
                val newState = when (result) {
                    is ResultState.Loading -> IsApprovedUserState(isLoading = true)
                    is ResultState.Error -> {
                        _uiEvent.trySend(
                            UiEvent.ShowSnackbar(
                                result.exception.message ?: "Approval failed"
                            )
                        )
                        IsApprovedUserState(error = result.exception.message)
                    }

                    is ResultState.Success -> {
                        val current = _getAllUserState.value.success
                        if (current != null) {
                            val updated = current.copy(
                                users = current.users.updateItem(userId, { it.user_id }) { user ->
                                    user.copy(_isApproved = isApproveds)
                                }
                            )
                            _getAllUserState.value = GetAllUserState(success = updated)
                        }
                        _uiEvent.trySend(UiEvent.ShowSnackbar(if (isApproveds) "User approved" else "Approval revoked"))
                        IsApprovedUserState(success = result.data)
                    }
                }
                _isApproved.update { it + (userId to newState) }
            }
        }
    }

    fun isApproveOrder(orderId: String, isApproved: Boolean) {
        _isApproveState.update { it + (orderId to ApproveOrderState(isLoading = true)) }

        viewModelScope.launch(Dispatchers.IO) {
            repository.approveOrder(orderId, isApproved).collect { result ->
                val newState = when (result) {
                    is ResultState.Loading -> ApproveOrderState(isLoading = true)
                    is ResultState.Error -> {
                        _uiEvent.trySend(
                            UiEvent.ShowSnackbar(
                                result.exception.message ?: "Approval failed"
                            )
                        )
                        ApproveOrderState(error = result.exception.message)
                    }

                    is ResultState.Success -> {
                        val current = _getAllOrderState.value.success
                        if (current != null) {
                            val updated = current.copy(
                                orders = current.orders.updateItem(
                                    orderId,
                                    { it.order_id }) { order ->
                                    order.copy(_isApproved = isApproved)
                                }
                            )
                            _getAllOrderState.value = GetAllOrdersState(success = updated)
                        }
                        // AUTO-SYNC: Order approve hote hi Sell History invalidate karo aur Products refresh karo!
                        _getSellHistory.value = GetSellHistoryState()
                        getAllProduct(force = true)

                        _uiEvent.trySend(UiEvent.ShowSnackbar(if (isApproved) "Order approved" else "Order approval revoked"))
                        ApproveOrderState(success = result.data)
                    }
                }
                _isApproveState.update { it + (orderId to newState) }
            }
        }
    }

    // Clear helpers (Kept for backward compatibility with your existing UI calls)
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
}