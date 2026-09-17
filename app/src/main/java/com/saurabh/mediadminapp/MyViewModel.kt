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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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


    init {
        Log.d("PERF_TRACE", "MyViewModel init START [Thread: ${Thread.currentThread().name}]")
        checkLoginStatus()
        Log.d("PERF_TRACE", "MyViewModel init END [Thread: ${Thread.currentThread().name}]")
    }
    fun checkLoginStatus(){
        Log.d("PERF_TRACE", "checkLoginStatus START [Thread: ${Thread.currentThread().name}]")
        viewModelScope.launch (Dispatchers.IO){
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

    fun setAdminLoggedOut() {
        tokenManager.clearTokens()
        _isAdminLoggedIn.value = false
        _loggedInAdminId.value = null
        Log.d("TAG", "setAdminLoggedIn: Admin logged out, tokens cleared")
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


    fun deleteUser(userId: String){
        // prevent form duplicate operation
        if(_deleteUserState.value.success != null && !_deleteUserState.value.isLoading && _deleteUserState.value.error == null ) return  //
        /*
        * "If the delete operation is successful (success != null),and it's not loading (!isLoading),and there’s no error (error == null),
            then return immediately."*/

        viewModelScope.launch(Dispatchers.IO) {
            _deleteUserState.value = DeleteUserState(isLoading = true)  // for the first time is user is not deleted the loading start
            repository.deleteUser(userId).collect {user->

                when(user){
                    is ResultState.Loading ->{
                        _deleteUserState.value = DeleteUserState(isLoading = true)
                    }
                    is ResultState.Error ->{
                        _deleteUserState.value = DeleteUserState(error = user.exception.message)
                    }
                    is ResultState.Success ->{
                        // after successfull deletion clear the user list state to ensure a fresh list for homescreen
                        _getAllUserState.value = GetAllUserState(isLoading = true)
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(300)
                            getAllUsers()
                        }
                        _deleteUserState.value = DeleteUserState(success = user.data, isLoading = false)
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
        if (_deleteOrderState.value.success != null && !_deleteOrderState.value.isLoading && _deleteOrderState.value.error == null) return

        viewModelScope.launch(Dispatchers.IO) {
            _deleteOrderState.value = DeleteOrderState(isLoading = true)
            repository.deleteOrder(orderId).collect { order ->
                when (order) {
                    is ResultState.Loading -> {
                        _deleteOrderState.value = DeleteOrderState(isLoading = true)
                    }
                    is ResultState.Error -> {
                        _deleteOrderState.value = DeleteOrderState(error = order.exception.message)
                    }
                    is ResultState.Success -> {
                        _deleteOrderState.value = DeleteOrderState(success = order.data, isLoading = false)
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

    fun deleteProduct(productId: String){
        if(_deleteProductState.value.success != null && !_deleteProductState.value.isLoading && _deleteProductState.value.error == null ) return

        viewModelScope.launch(Dispatchers.IO) {
            _deleteProductState.value = DeleteProductState(isLoading = true)
            repository.deleteProduct(productId).collect {
                when(it){
                    is ResultState.Loading ->{
                        _deleteProductState.value = DeleteProductState(isLoading = true)
                    }
                    is ResultState.Error -> {
                        _deleteProductState.value = DeleteProductState(error = it.exception.message)
                    }
                    is ResultState.Success ->{
                        _deleteProductState.value = DeleteProductState(success = it.data, isLoading = false)
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
        password : String?=null,
        isApproved: Boolean? = null,
        block : Boolean?=null,
        address : String?=null,
        email: String? = null,
        phonenumber: String? = null,
        pincode: String? = null
    ){
        if(_updateUserState.value.success != null && !_updateUserState.value.isLoading && _updateUserState.value.error == null ) return

        viewModelScope.launch(Dispatchers.IO) {
            _updateUserState.value = UpdateUserState(isLoading = true)
            repository.updateUser(userId,name,password,isApproved,block,address,email,phonenumber,pincode
            ).collect {
                when (it) {
                    is ResultState.Loading -> {
                        _updateUserState.value = UpdateUserState(isLoading = true)
                    }

                    is ResultState.Error -> {
                        _updateUserState.value = UpdateUserState(error = it.exception.message)
                    }

                    is ResultState.Success -> {
                        _updateUserState.value =
                            UpdateUserState(success = it.data, isLoading = false)
                    }
                }
            }
        }
    }


    fun updateOrder( orderId: String,isApproved: Boolean? = null,quantity: Int? = null,price: Float? = null
    ) {
        val isApprovedInt = isApproved?.let { if(it) 1 else 0 }  // convert Boolean to Int for API compatibility
//        if (_updateOrderState.value.success != null && !_updateOrderState.value.isLoading && _updateOrderState.value.error == null) return
        _updateOrderState.value = _updateOrderState.value.toMutableMap().apply {
            this[orderId] = UpdateOrderState(isLoading = true)  // set loading state for the specific order
        }
        viewModelScope.launch(Dispatchers.IO) {
//            _updateOrderState.value = UpdateOrderState(isLoading = true)
            repository.updateOrder(orderId, isApprovedInt, quantity, price).collect { order ->
                val newState = when (order) {
                    is ResultState.Loading -> {
                        UpdateOrderState(isLoading = true)
                    }
                    is ResultState.Error -> {
                        UpdateOrderState(error = order.exception.message)
                    }
                    is ResultState.Success -> {
                        viewModelScope.launch {
                            clearGetAllOrdersState()
                            kotlinx.coroutines.delay(300)
                            _updateOrderState.value = emptyMap()
                            _getAllOrderState.value = GetAllOrdersState(isLoading = false, success = null, error = null)
                            getAllOrders()  // refresh the order list after update
                        }
                        UpdateOrderState(success = order.data, isLoading = false)
                    }
                }
                _updateOrderState.value = _updateOrderState.value.toMutableMap().apply {
                    this[orderId] = newState  // update the specific order state
                }
            }
        }
    }

    fun isApprovedUser(userId: String, isApproveds: Boolean){
        _isApproved.value = _isApproved.value.toMutableMap().apply {
            this[userId] = IsApprovedUserState(isLoading = true)
        }
        viewModelScope.launch (Dispatchers.IO){
            repository.isApprovedUser(userId,isApproveds).collect {
                val newState = when(it){
                    is ResultState.Loading->{
                        IsApprovedUserState(isLoading = true)
                    }
                    is ResultState.Error -> {
                        IsApprovedUserState(error = it.exception.message, isLoading = false)
                    }
                    is ResultState.Success -> {
                        // force to clear the user list state to ensure the fresh list
//                        _getAllUserState.value = GetAllUserState(isLoading = true)
                        viewModelScope.launch(Dispatchers.Main) {
                            kotlinx.coroutines.delay(300)
                            _isApproved.value = emptyMap()
                            _getAllUserState.value = GetAllUserState(isLoading = false, success = null, error = null)
                            getAllUsers()
                        }
                        IsApprovedUserState(success = it.data, isLoading = false)
                    }
                }
                _isApproved.value = _isApproved.value.toMutableMap().apply {
                    this[userId] = newState
                }
            }
        }
    }
    fun isApproveOrder(orderId: String, isApproved: Boolean) {
        val isApprovedInt = if (isApproved) 1 else 0  // convert Boolean to Int for API compatibility
        Log.d("ViewModel", "API Call: Order $orderId, Setting approved to: $isApproved")
        _isApproveState.value = _isApproveState.value.toMutableMap().apply {
            this[orderId] = ApproveOrderState(isLoading = true)
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.approveOrder(orderId, isApproved).collect {
                val newState = when (it) {
                    is ResultState.Loading -> {
                        ApproveOrderState(isLoading = true)
                    }
                    is ResultState.Error -> {
                        ApproveOrderState(error = it.exception.message, isLoading = false)
                    }

                    is ResultState.Success -> {
                        // force to clear the user list state to ensure the fresh list
                        viewModelScope.launch ( Dispatchers.Main ){
                            delay(100)

                            _isApproveState.value = emptyMap()
                            _getAllOrderState.value = GetAllOrdersState(isLoading = false, success = null, error = null)
                            getAllOrders()
                            Log.d("OrderDebug", "Orders = ${_getAllOrderState.value.success}")
                            Log.d("ViewModel", "API Success: Order $orderId updated successfully")
                        }
                        ApproveOrderState(success = it.data, isLoading = false)

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