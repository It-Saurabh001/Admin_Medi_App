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
import com.saurabh.mediadminapp.utils.updateItem
import com.saurabh.mediadminapp.utils.restoreItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: Repository,
    private val tokenManager: TokenManager) : ViewModel (){
//    private val repository = Repository()
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn = _isAdminLoggedIn.asStateFlow()

    private val _loggedInAdminId = MutableStateFlow<String?>(null)
    val loggedInAdminId = _loggedInAdminId.asStateFlow()
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


    private var _getAllUserState = MutableStateFlow(GetAllUserState())
    val getAllUserState = _getAllUserState.asStateFlow()

    private var _getAllProduct = MutableStateFlow(GetAllProductState())
    val getAllProduct = _getAllProduct.asStateFlow()

    private var _addProductState = MutableStateFlow(AddProductState())
    val addProductState = _addProductState.asStateFlow()

    private var _updateProductState = MutableStateFlow(UpdateProductState())
    val updateProductState = _updateProductState.asStateFlow()

    private var _deleteProductState = MutableStateFlow(DeleteProductState())
    val deleteProductState = _deleteProductState.asStateFlow()

    private var _getSpecificProductState = MutableStateFlow(GetSpecificProductState())
    val getSpecificProductState = _getSpecificProductState.asStateFlow()

    private var _isApproved = MutableStateFlow<Map<String, IsApprovedUserState>>(emptyMap())
    val isApprovedUser = _isApproved.asStateFlow()

    private var _updateOrderState = MutableStateFlow<Map<String, UpdateOrderState>>(emptyMap())
    val updateOrderState = _updateOrderState.asStateFlow()

    private var _deleteUserState = MutableStateFlow(DeleteUserState())
    val deleteUserState = _deleteUserState.asStateFlow()

    private var _updateUserState = MutableStateFlow(UpdateUserState())
    val updateUserState = _updateUserState.asStateFlow()

    private var _getAllOrderState = MutableStateFlow(GetAllOrdersState())
    val getAllOrderState = _getAllOrderState.asStateFlow()

    private var _getUsersOrdersState = MutableStateFlow(GetUsersOrderState())
    val getUsersOrdersState = _getUsersOrdersState.asStateFlow()

    private var _deleteOrderState = MutableStateFlow(DeleteOrderState())
    val deleteOrderState = _deleteOrderState.asStateFlow()

    private var _isApproveState = MutableStateFlow<Map<String, ApproveOrderState>>(emptyMap())
    val isApproveOrdder = _isApproveState.asStateFlow()

    private var _getSellHistory = MutableStateFlow(GetSellHistoryState())
    val getSellHistory = _getSellHistory.asStateFlow()

    private var _getRecordSellHistory = MutableStateFlow(RecordSellHistoryState())
    val getRecordSellHistory = _getRecordSellHistory.asStateFlow()

    private var _getProductSellHistory = MutableStateFlow(GetProductSellHistoryState())
    val getProductSellHistory = _getProductSellHistory.asStateFlow()
    private var _getUserSellHistory = MutableStateFlow(GetUserSellHistoryState())
    val getUserSellHistory = _getUserSellHistory.asStateFlow()

    private var _deleteSellHistory = MutableStateFlow(GetDeleteSellHistoryState())
    val deleteSellHistory = _deleteSellHistory.asStateFlow()

    private var _getOrderByIdState = MutableStateFlow(GetOrderByIdState())
    val getOrderByIdState = _getOrderByIdState.asStateFlow()

    // Single-shot UI side-effects (snackbars) without triggering recomposition of list screens.
    // Capacity BUFFERED ensures tryEmit() from the IO thread never drops events.
    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        Log.d("PERF_TRACE", "MyViewModel init START [Thread: ${Thread.currentThread().name}]")
        checkLoginStatus()
        observeSessionExpiry()
        Log.d("PERF_TRACE", "MyViewModel init END [Thread: ${Thread.currentThread().name}]")
    }

    /**
     * Observes [TokenManager.sessionExpiredEvent].
     * When the TokenAuthenticator exhausts all refresh attempts, it calls
     * [TokenManager.invalidateSession] which emits on this flow.  We react by
     * setting [_isAdminLoggedIn] to false, which causes NavApp to navigate to
     * the Login screen automatically — no user action required.
     */
    private fun observeSessionExpiry() {
        viewModelScope.launch {
            tokenManager.sessionExpiredEvent.collect {
                Log.w("MyViewModel", "Session expired event received — forcing logout")
                _isAdminLoggedIn.value = false
                _loggedInAdminId.value = null
            }
        }
    }

    fun checkLoginStatus() {
        Log.d("PERF_TRACE", "checkLoginStatus START [Thread: ${Thread.currentThread().name}]")
        viewModelScope.launch(Dispatchers.IO) {
            _isAdminLoggedIn.value = tokenManager.isLoggedIn()
            _loggedInAdminId.value = tokenManager.getAdminId()
            Log.d("TAG", "checkLoginStatus: admin logged in status: ${_isAdminLoggedIn.value}")
            Log.d("PERF_TRACE", "checkLoginStatus END [Thread: ${Thread.currentThread().name}]")
        }
    }

    fun setAdminLoggedIn() {
        _isAdminLoggedIn.value = true
        _loggedInAdminId.value = tokenManager.getAdminId()
        Log.d("TAG", "setAdminLoggedIn: Admin logged in, tokens saved")
    }

    /**
     * Optimistic local-first logout.
     *
     * Pattern: try { optional network call } finally { wipe local storage }
     *
     * The finally block runs unconditionally — even if the network is down,
     * the server returns 401, or the coroutine is cancelled.  This guarantees
     * the user can always log out, and they are never trapped on an error screen
     * due to a failed backend logout call.
     */
    fun setAdminLoggedOut() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Optional: add a network logout call here if the backend
                // provides a POST /logout endpoint in the future.
                // e.g. repository.logout() — fire-and-forget, we don't care
                // about its success because the finally block handles everything.
            } finally {
                tokenManager.clearTokens()
                _isAdminLoggedIn.value = false
                _loggedInAdminId.value = null
                Log.d("TAG", "setAdminLoggedOut: Admin logged out, tokens cleared")
            }
        }
    }

    fun createAdmin(name: String, email: String, password: String, phoneNumber: String) {
        if (_createAdminState.value.success != null && !_createAdminState.value.isLoading && _createAdminState.value.error == null) return

        viewModelScope.launch(Dispatchers.IO) {
            _createAdminState.value = CreateAdminState(isLoading = true)
            repository.createAdmin(name, password, email,phoneNumber).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        _createAdminState.value = CreateAdminState(isLoading = true)
                    }
                    is ResultState.Error -> {
                        _createAdminState.value = CreateAdminState(error = result.exception.message)
                    }
                    is ResultState.Success -> {
                        _createAdminState.value = CreateAdminState(success = result.data, isLoading = false)
                    }
                }
            }
        }
    }

    fun loginAdmin(email: String, password: String) {
        Log.d("PERF_TRACE", "MyViewModel loginAdmin START [Thread: ${Thread.currentThread().name}]")
        if (_loginAdminState.value.success != null && !_loginAdminState.value.isLoading && _loginAdminState.value.error == null) return

        viewModelScope.launch(Dispatchers.IO) {
            _loginAdminState.value = LoginAdminState(isLoading = true)
            repository.loginAdmin(email, password).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        _loginAdminState.value = LoginAdminState(isLoading = true)
                    }
                    is ResultState.Error -> {
                        _loginAdminState.value = LoginAdminState(error = result.exception.message)
                    }
                    is ResultState.Success -> {
                        _loginAdminState.value = LoginAdminState(success = result.data, isLoading = false)
                    }
                }
            }
            Log.d("PERF_TRACE", "MyViewModel loginAdmin END [Thread: ${Thread.currentThread().name}]")
        }
    }
    fun verifyAdminOtp(adminId: String, otp: String) {
        if (_verifyOtpState.value.success != null && !_verifyOtpState.value.isLoading && _verifyOtpState.value.error == null) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.verifyAdminOtp(adminId, otp).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        _verifyOtpState.value = VerifyOtpState(isLoading = true)
                    }
                    is ResultState.Error -> {
                        Log.e("TAG", "verifyAdminOtp: Network/parse error: ${result.exception.message}")
                        _verifyOtpState.value = VerifyOtpState(error = result.exception.message)
                    }
                    is ResultState.Success -> {
                        val data = result.data
                        Log.d("TAG", "verifyAdminOtp: HTTP 200 received. Business status=${data.status}, message=${data.message}")

                        if (data.status == 200 && data.access_token != null) {
                            // Genuine OTP success — save tokens and mark logged in
                            tokenManager.saveTokens(
                                accessToken = data.access_token,
                                refreshToken = data.refresh_token ?: "",
                                role = data.role ?: "",
                                adminId = adminId
                            )
                            setAdminLoggedIn()
                            Log.d("TAG", "verifyAdminOtp: OTP verified. Tokens saved. Admin logged in.")
                            _verifyOtpState.value = VerifyOtpState(success = data, isLoading = false)
                        } else {
                            // Business-level failure (e.g. 400 No OTP request found)
                            val errMsg = data.message ?: "OTP verification failed"
                            Log.e("TAG", "verifyAdminOtp: Business error: $errMsg (status=${data.status})")
                            _verifyOtpState.value = VerifyOtpState(error = errMsg, isLoading = false)
                        }
                    }
                }
            }
        }
    }

    // New: Request Password Reset
    fun requestPasswordReset(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.requestAdminPasswordReset(email).collect { result ->
                when (result) {
                    is ResultState.Loading -> _passwordResetState.value = PasswordResetState(isLoading = true)
                    is ResultState.Error -> _passwordResetState.value = PasswordResetState(error = result.exception.message)
                    is ResultState.Success -> _passwordResetState.value = PasswordResetState(success = result.data)
                }
            }
        }
    }

    fun resetPasswordOtp(userId: String, otp: String,newPassword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetAdminPasswordWithOtp(userId,otp,newPassword).collect { result ->
                when (result) {
                    is ResultState.Loading -> _passwordResetOtpState.value = PasswordResetOtpState(isLoading = true)
                    is ResultState.Error -> _passwordResetOtpState.value = PasswordResetOtpState(error = result.exception.message)
                    is ResultState.Success -> _passwordResetOtpState.value = PasswordResetOtpState(success = result.data)
                }
            }
        }
    }

    fun getAllAdmin(){
        if (_getAllAdminState.value.success != null && !_getAllAdminState.value.isLoading && _getAllAdminState.value.error == null) return

        viewModelScope.launch(Dispatchers.IO) {
            _getAllAdminState.value = GetAllAdminState(isLoading = true)
            repository.getAllAdmins().collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        _getAllAdminState.value = GetAllAdminState(isLoading = true)
                    }
                    is ResultState.Error -> {
                        _getAllAdminState.value = GetAllAdminState(error = result.exception.message)
                    }
                    is ResultState.Success -> {
                        _getAllAdminState.value = GetAllAdminState(success = result.data, isLoading = false)
                    }
                }
            }
        }
    }

    // ===========================
    // History FUNCTIONS
    // ===========================



    fun getAllSellHistory() {
        if (_getSellHistory.value.success != null && !_getSellHistory.value.isLoading && _getSellHistory.value.error == null) return

        viewModelScope.launch(Dispatchers.IO) {
            _getSellHistory.value = GetSellHistoryState(isLoading = true)
            repository.getAllSellHistory().collect { sellHistory ->
                when (sellHistory) {
                    is ResultState.Loading -> {
                        _getSellHistory.value = GetSellHistoryState(isLoading = true)
                    }
                    is ResultState.Error -> {
                        _getSellHistory.value = GetSellHistoryState(error = sellHistory.exception.message)
                    }
                    is ResultState.Success -> {
                        _getSellHistory.value = GetSellHistoryState(success = sellHistory.data, isLoading = false)
                    }
                }
            }
        }
    }

    fun deleteUser(userId: String) {
        val current = _getAllUserState.value.success
        val userToDelete = current?.users?.find { it.user_id == userId }
        val index = current?.users?.indexOfFirst { it.user_id == userId } ?: -1

        if (current != null && userToDelete != null && index != -1) {
            val updated = current.copy(
                users = current.users.removeItem(userId) { it.user_id }
            )
            _getAllUserState.value = GetAllUserState(success = updated, isLoading = false)
        }

        viewModelScope.launch(Dispatchers.IO) {
            _deleteUserState.value = DeleteUserState(isLoading = true)
            repository.deleteUser(userId).collect { result ->
                when (result) {
                    is ResultState.Loading -> _deleteUserState.value = DeleteUserState(isLoading = true)
                    is ResultState.Error -> {
                        // Rollback
                        val currentRollback = _getAllUserState.value.success
                        if (currentRollback != null && userToDelete != null && index != -1) {
                            val reverted = currentRollback.copy(
                                users = currentRollback.users.restoreItem(userToDelete, index)
                            )
                            _getAllUserState.value = GetAllUserState(success = reverted, isLoading = false)
                        }
                        _deleteUserState.value = DeleteUserState(error = result.exception.message)
                        _uiEvent.trySend(UiEvent.ShowSnackbar(result.exception.message ?: "Delete failed"))
                    }
                    is ResultState.Success -> {
                        _deleteUserState.value = DeleteUserState(success = result.data, isLoading = false)
                        _uiEvent.trySend(UiEvent.ShowSnackbar("User deleted successfully"))
                    }
                }
            }
        }
    }



    fun getAllUsers(){
        if(_getAllUserState.value.success != null && !_getAllUserState.value.isLoading && _getAllUserState.value.error == null ) return
//        if(_getAllUserState.value.isLoading ) return  // only skip if already loading
        viewModelScope.launch(Dispatchers.IO) {
            _getAllUserState.value= GetAllUserState(isLoading = true)
            repository.getAllUsers().collect{
                when(it){
                    is ResultState.Loading ->{
                        _getAllUserState.value= GetAllUserState(isLoading = true)
                    }
                    is ResultState.Error ->{
                        _getAllUserState.value = GetAllUserState(error =  it.exception.message)
                    }
                    is ResultState.Success -> {
                        _getAllUserState.value = GetAllUserState(success = it.data, isLoading = false)
                    }
                }
            }
        }
    }

    fun getAllOrders() {
        if (_getAllOrderState.value.success != null && !_getAllOrderState.value.isLoading && _getAllOrderState.value.error == null) return

        viewModelScope.launch(Dispatchers.IO) {
            _getAllOrderState.value = GetAllOrdersState(isLoading = true)
            repository.getAllOrders().collect { order ->
                when (order) {
                    is ResultState.Loading -> {
                        _getAllOrderState.value = GetAllOrdersState(isLoading = true)
                    }
                    is ResultState.Error -> {
                        _getAllOrderState.value = GetAllOrdersState(error = order.exception.message)
                    }
                    is ResultState.Success -> {
                        _getAllOrderState.value = GetAllOrdersState(success = order.data, isLoading = false)
                    }
                }
            }
        }


    }

    fun getUsersOrders(userId: String) {
        if (_getUsersOrdersState.value.success != null && !_getUsersOrdersState.value.isLoading && _getUsersOrdersState.value.error == null) return

        viewModelScope.launch(Dispatchers.IO) {
            _getUsersOrdersState.value = GetUsersOrderState(isLoading = true)
            repository.getUserOrders(userId).collect {
                when (it) {
                    is ResultState.Loading -> {
                        _getUsersOrdersState.value = GetUsersOrderState(isLoading = true)
                    }
                    is ResultState.Error -> {
                        _getUsersOrdersState.value = GetUsersOrderState(error = it.exception.message)
                    }
                    is ResultState.Success -> {
                        _getUsersOrdersState.value = GetUsersOrderState(success = it.data, isLoading = false)
                    }
                }
            }
        }
    }

    fun getOrderById(orderId : String){
        if(_getOrderByIdState.value.success != null && !_getOrderByIdState.value.isLoading && _getOrderByIdState.value.error == null) return

        viewModelScope.launch(Dispatchers.IO) {
            _getOrderByIdState.value = GetOrderByIdState(isLoading = true)
            repository.getOrdersById(orderId).collect { order ->
                when (order) {
                    is ResultState.Loading -> {
                        _getOrderByIdState.value = GetOrderByIdState(isLoading = true)
                    }
                    is ResultState.Error -> {
                        _getOrderByIdState.value = GetOrderByIdState(error = order.exception.message)
                    }
                    is ResultState.Success -> {
                        _getOrderByIdState.value = GetOrderByIdState(success = order.data, isLoading = false)
                    }
                }
            }
        }
    }

    fun deleteOrder(orderId: String) {
        val current = _getAllOrderState.value.success
        val itemToDelete = current?.orders?.find { it.order_id == orderId }
        val index = current?.orders?.indexOfFirst { it.order_id == orderId } ?: -1

        if (current != null && itemToDelete != null && index != -1) {
            val updated = current.copy(
                orders = current.orders.removeItem(orderId) { it.order_id }
            )
            _getAllOrderState.value = GetAllOrdersState(success = updated, isLoading = false)
        }

        viewModelScope.launch(Dispatchers.IO) {
            _deleteOrderState.value = DeleteOrderState(isLoading = true)
            repository.deleteOrder(orderId).collect { result ->
                when (result) {
                    is ResultState.Loading -> _deleteOrderState.value = DeleteOrderState(isLoading = true)
                    is ResultState.Error -> {
                        // Rollback
                        val currentRollback = _getAllOrderState.value.success
                        if (currentRollback != null && itemToDelete != null && index != -1) {
                            val reverted = currentRollback.copy(
                                orders = currentRollback.orders.restoreItem(itemToDelete, index)
                            )
                            _getAllOrderState.value = GetAllOrdersState(success = reverted, isLoading = false)
                        }
                        _deleteOrderState.value = DeleteOrderState(error = result.exception.message)
                        _uiEvent.trySend(UiEvent.ShowSnackbar(result.exception.message ?: "Delete failed"))
                    }
                    is ResultState.Success -> {
                        _deleteOrderState.value = DeleteOrderState(success = result.data, isLoading = false)
                        _uiEvent.trySend(UiEvent.ShowSnackbar("Order deleted"))
                    }
                }
            }
        }
    }





    fun getAllProduct(force: Boolean = false){
        if(!force && _getAllProduct.value.success != null && !_getAllProduct.value.isLoading && _getAllProduct.value.error == null ) return
//        if(_getAllProductState.value.isLoading ) return  // only skip if already loading  not reload if already loaded means no recomposeition of screens
        viewModelScope.launch(Dispatchers.IO) {
            _getAllProduct.value= GetAllProductState(isLoading = true)
            repository.getAllProduct().collect{
                when(it){
                    is ResultState.Loading ->{
                        _getAllProduct.value= GetAllProductState(isLoading = true)
                    }
                    is ResultState.Error ->{
                        _getAllProduct.value = GetAllProductState(error =  it.exception.message)
                    }
                    is ResultState.Success -> {
                        _getAllProduct.value = GetAllProductState(success = it.data, isLoading = false)
                    }
                }
            }
        }
    }

    fun addProduct(name: String, price: Double, category: String, stock: Int, image: MultipartBody.Part? = null){
        if(_addProductState.value.success != null && !_addProductState.value.isLoading && _addProductState.value.error == null ) return

        viewModelScope.launch(Dispatchers.IO) {
            _addProductState.value = AddProductState(isLoading = true)
            repository.getAddProduct(name, price, category, stock, image).collect {
                when(it){
                    is ResultState.Loading ->{
                        _addProductState.value = AddProductState(isLoading = true)
                    }
                    is ResultState.Error ->{
                        _addProductState.value = AddProductState(error = it.exception.message)
                    }
                    is ResultState.Success ->{
                        _addProductState.value = AddProductState(success = it.data, isLoading = false)

                    }
                }
            }
        }
    }
    fun updateProduct(productId: String, name: String? = null, price: Double? = null, category: String? = null, stock: Int? = null, image: MultipartBody.Part? = null){
        if(_updateProductState.value.success != null && !_updateProductState.value.isLoading && _updateProductState.value.error == null ) return

        viewModelScope.launch(Dispatchers.IO) {
            _updateProductState.value = UpdateProductState(isLoading = true)
            repository.updateProduct(productId, name, price, category, stock, image).collect {
                when(it){
                    is ResultState.Loading ->{
                        _updateProductState.value = UpdateProductState(isLoading = true)
                    }
                    is ResultState.Error -> {
                        _updateProductState.value = UpdateProductState(error = it.exception.message)
                    }
                    is ResultState.Success ->{
                        _updateProductState.value = UpdateProductState(success = it.data, isLoading = false)
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
            val updated = current.copy(
                products = current.products.removeItem(productId) { it.Product_id }
            )
            _getAllProduct.value = GetAllProductState(success = updated, isLoading = false)
        }

        viewModelScope.launch(Dispatchers.IO) {
            _deleteProductState.value = DeleteProductState(isLoading = true)
            repository.deleteProduct(productId).collect { result ->
                when (result) {
                    is ResultState.Loading -> _deleteProductState.value = DeleteProductState(isLoading = true)
                    is ResultState.Error -> {
                        val currentRollback = _getAllProduct.value.success
                        if (currentRollback != null && itemToDelete != null && index != -1) {
                            val reverted = currentRollback.copy(
                                products = currentRollback.products.restoreItem(itemToDelete, index)
                            )
                            _getAllProduct.value = GetAllProductState(success = reverted, isLoading = false)
                        }
                        _deleteProductState.value = DeleteProductState(error = result.exception.message)
                        _uiEvent.trySend(UiEvent.ShowSnackbar(result.exception.message ?: "Delete failed"))
                    }
                    is ResultState.Success -> {
                        _deleteProductState.value = DeleteProductState(success = result.data, isLoading = false)
                        _uiEvent.trySend(UiEvent.ShowSnackbar("Product deleted"))
                    }
                }
            }
        }
    }

    fun getSpecificProduct(productId: String){
        if(_getSpecificProductState.value.success != null && !_getSpecificProductState.value.isLoading && _getSpecificProductState.value.error == null ) return
        viewModelScope.launch(Dispatchers.IO) {
            _getSpecificProductState.value = GetSpecificProductState(isLoading = true)
            repository.getSpecificProduct(productId).collect {
                when(it){
                    is ResultState.Loading ->{
                        _getSpecificProductState.value = GetSpecificProductState(isLoading = true)
                    }
                    is ResultState.Error ->{
                        _getSpecificProductState.value = GetSpecificProductState(error = it.exception.message)
                    }
                    is ResultState.Success ->{
                        _getSpecificProductState.value = GetSpecificProductState(success = it.data, isLoading = false)
                    }
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
            _updateUserState.value = UpdateUserState(isLoading = true)
            repository.updateUser(userId, name, password, isApproved, block, address, email, phonenumber, pincode).collect { result ->
                when (result) {
                    is ResultState.Loading -> _updateUserState.value = UpdateUserState(isLoading = true)
                    is ResultState.Error -> {
                        _updateUserState.value = UpdateUserState(error = result.exception.message)
                        _uiEvent.trySend(UiEvent.ShowSnackbar(result.exception.message ?: "Update failed"))
                    }
                    is ResultState.Success -> {
                        // Patch only the changed user in the cached list without a network round-trip.
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
                            _getAllUserState.value = GetAllUserState(success = updated, isLoading = false)
                        }
                        _updateUserState.value = UpdateUserState(success = result.data, isLoading = false)
                        _uiEvent.trySend(UiEvent.ShowSnackbar("User updated"))
                    }
                }
            }
        }
    }


    fun updateOrder(orderId: String, isApproved: Boolean? = null, quantity: Int? = null, price: Float? = null) {
        val isApprovedInt = isApproved?.let { if (it) 1 else 0 }
        _updateOrderState.value = _updateOrderState.value.toMutableMap().apply {
            this[orderId] = UpdateOrderState(isLoading = true)
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateOrder(orderId, isApprovedInt, quantity, price).collect { result ->
                val newState = when (result) {
                    is ResultState.Loading -> UpdateOrderState(isLoading = true)
                    is ResultState.Error -> {
                        _uiEvent.trySend(UiEvent.ShowSnackbar(result.exception.message ?: "Update failed"))
                        UpdateOrderState(error = result.exception.message)
                    }
                    is ResultState.Success -> {
                        // Patch only the changed order in the cached list.
                        val current = _getAllOrderState.value.success
                        if (current != null) {
                            val updated = current.copy(
                                orders = current.orders.updateItem(orderId, { it.order_id }) { order ->
                                    order.copy(
                                        _isApproved = isApproved ?: order._isApproved,
                                        quantity = quantity ?: order.quantity,
                                        price = price?.toDouble() ?: order.price
                                    )
                                }
                            )
                            _getAllOrderState.value = GetAllOrdersState(success = updated, isLoading = false)
                        }
                        _uiEvent.trySend(UiEvent.ShowSnackbar("Order updated"))
                        UpdateOrderState(success = result.data, isLoading = false)
                    }
                }
                _updateOrderState.value = _updateOrderState.value.toMutableMap().apply {
                    this[orderId] = newState
                }
            }
        }
    }

    fun isApprovedUser(userId: String, isApproveds: Boolean) {
        _isApproved.value = _isApproved.value.toMutableMap().apply {
            this[userId] = IsApprovedUserState(isLoading = true)
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.isApprovedUser(userId, isApproveds).collect { result ->
                val newState = when (result) {
                    is ResultState.Loading -> IsApprovedUserState(isLoading = true)
                    is ResultState.Error -> {
                        _uiEvent.trySend(UiEvent.ShowSnackbar(result.exception.message ?: "Approval failed"))
                        IsApprovedUserState(error = result.exception.message, isLoading = false)
                    }
                    is ResultState.Success -> {
                        // Patch the isApproved flag on the cached user — no GET request.
                        val current = _getAllUserState.value.success
                        if (current != null) {
                            val updated = current.copy(
                                users = current.users.updateItem(userId, { it.user_id }) { user ->
                                    user.copy(_isApproved = isApproveds)
                                }
                            )
                            _getAllUserState.value = GetAllUserState(success = updated, isLoading = false)
                        }
                        _uiEvent.trySend(UiEvent.ShowSnackbar(if (isApproveds) "User approved" else "Approval revoked"))
                        IsApprovedUserState(success = result.data, isLoading = false)
                    }
                }
                _isApproved.value = _isApproved.value.toMutableMap().apply {
                    this[userId] = newState
                }
            }
        }
    }

    fun isApproveOrder(orderId: String, isApproved: Boolean) {
        Log.d("ViewModel", "API Call: Order $orderId, Setting approved to: $isApproved")
        _isApproveState.value = _isApproveState.value.toMutableMap().apply {
            this[orderId] = ApproveOrderState(isLoading = true)
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.approveOrder(orderId, isApproved).collect { result ->
                val newState = when (result) {
                    is ResultState.Loading -> ApproveOrderState(isLoading = true)
                    is ResultState.Error -> {
                        _uiEvent.trySend(UiEvent.ShowSnackbar(result.exception.message ?: "Approval failed"))
                        ApproveOrderState(error = result.exception.message, isLoading = false)
                    }
                    is ResultState.Success -> {
                        // Patch the isApproved flag on the cached order — no GET request.
                        val current = _getAllOrderState.value.success
                        if (current != null) {
                            val updated = current.copy(
                                orders = current.orders.updateItem(orderId, { it.order_id }) { order ->
                                    order.copy(_isApproved = isApproved)
                                }
                            )
                            _getAllOrderState.value = GetAllOrdersState(success = updated, isLoading = false)
                        }
                        Log.d("ViewModel", "API Success: Order $orderId approved=$isApproved")
                        _uiEvent.trySend(UiEvent.ShowSnackbar(if (isApproved) "Order approved" else "Order approval revoked"))
                        ApproveOrderState(success = result.data, isLoading = false)
                    }
                }
                _isApproveState.value = _isApproveState.value.toMutableMap().apply {
                    this[orderId] = newState
                }
            }
        }
    }




    // Clear approval state for testing/refreshing
    fun clearApprovalState(userId: String? = null) {
        _isApproved.value = if (userId != null) {
            _isApproved.value.toMutableMap().apply {
                remove(userId)
            }
        } else {
            emptyMap()
        }
    }
    fun resetDeleteUserState(){
        _deleteUserState.value = DeleteUserState()  // deletestate will update after successfully deleted

        // force a refresh of user list by clearing success state
        _getAllUserState.value = GetAllUserState(isLoading = false, success = null, error = null)

    }
    fun clearAddProductState(){
        _addProductState.value = AddProductState()

    }
    fun clearUpdateProductState(){
        _updateProductState.value = UpdateProductState()
    }
    fun clearGetSpecificProductState(){
        _getSpecificProductState.value = GetSpecificProductState()
    }
    fun clearDeleteProductState(){
        _deleteProductState.value = DeleteProductState()
    }
    fun clearGetAllProductState(){
        _getAllProduct.value = GetAllProductState()
    }
    fun clearGetUsersOrdersState(){
        _getUsersOrdersState.value = GetUsersOrderState()  // reset the state to initial
    }
    fun clearGetOrderByIdState() {
        _getOrderByIdState.value = GetOrderByIdState()  // reset the state to initial
    }
    fun clearUpdateOrderSteate(){
        _updateOrderState.value = emptyMap()  // reset the state to initial
    }

    fun clearGetAllOrdersState(){
        _getAllOrderState.value = GetAllOrdersState()  // reset the state to initial
    }

    fun clearLoginState(){
        _loginAdminState.value = LoginAdminState()
    }

    fun clearVerifyOtpState(){
        _verifyOtpState.value = VerifyOtpState()
    }


}