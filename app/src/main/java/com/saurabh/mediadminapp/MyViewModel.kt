package com.saurabh.mediadminapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.mediadminapp.common.ResultState
import com.saurabh.mediadminapp.repository.Repository
import com.saurabh.mediadminapp.utils.ScreensState.AddProductState
import com.saurabh.mediadminapp.utils.ScreensState.ApproveOrderState
import com.saurabh.mediadminapp.utils.ScreensState.DeleteOrderState
import com.saurabh.mediadminapp.utils.ScreensState.DeleteProductState
import com.saurabh.mediadminapp.utils.ScreensState.DeleteUserState
import com.saurabh.mediadminapp.utils.ScreensState.GetAllOrdersState
import com.saurabh.mediadminapp.utils.ScreensState.GetAllProductState
import com.saurabh.mediadminapp.utils.ScreensState.GetAllUserState
import com.saurabh.mediadminapp.utils.ScreensState.GetOrderByIdState
import com.saurabh.mediadminapp.utils.ScreensState.GetSpecificProductState
import com.saurabh.mediadminapp.utils.ScreensState.GetUsersOrderState
import com.saurabh.mediadminapp.utils.ScreensState.IsApprovedUserState
import com.saurabh.mediadminapp.utils.ScreensState.UpdateOrderState
import com.saurabh.mediadminapp.utils.ScreensState.UpdateProductState
import com.saurabh.mediadminapp.utils.ScreensState.UpdateUserState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MyViewModel @Inject constructor(private val repository: Repository) : ViewModel (){
//    private val repository = Repository()

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

    private var _approveState = MutableStateFlow(ApproveOrderState())
    val approveState = _approveState.asStateFlow()

    private var _updateOrderState = MutableStateFlow(UpdateOrderState())
    val updateOrderState = _updateOrderState.asStateFlow()

    private var _getOrderByIdState = MutableStateFlow(GetOrderByIdState())
    val getOrderByIdState = _getOrderByIdState.asStateFlow()

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

    fun approveOrder(orderId: String, isApproved: Boolean) {
        if (_approveState.value.success != null && !_approveState.value.isLoading && _approveState.value.error == null) return

        viewModelScope.launch(Dispatchers.IO) {
            _approveState.value = ApproveOrderState(isLoading = true)
            repository.approveOrder(orderId, isApproved).collect { order ->
                when (order) {
                    is ResultState.Loading -> {
                        _approveState.value = ApproveOrderState(isLoading = true)
                    }
                    is ResultState.Error -> {
                        _approveState.value = ApproveOrderState(error = order.exception.message)
                    }
                    is ResultState.Success -> {
                        _approveState.value = ApproveOrderState(success = order.data, isLoading = false)
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
        if (_updateOrderState.value.success != null && !_updateOrderState.value.isLoading && _updateOrderState.value.error == null) return

        viewModelScope.launch(Dispatchers.IO) {
            _updateOrderState.value = UpdateOrderState(isLoading = true)
            repository.updateOrder(orderId, isApproved, quantity, price).collect { order ->
                when (order) {
                    is ResultState.Loading -> {
                        _updateOrderState.value = UpdateOrderState(isLoading = true)
                    }
                    is ResultState.Error -> {
                        _updateOrderState.value = UpdateOrderState(error = order.exception.message)
                    }
                    is ResultState.Success -> {
                        _updateOrderState.value = UpdateOrderState(success = order.data, isLoading = false)
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

    fun addProduct(name: String, price: Double, category: String, stock: Int){
        if(_addProductState.value.success != null && !_addProductState.value.isLoading && _addProductState.value.error == null ) return

        viewModelScope.launch(Dispatchers.IO) {
            _addProductState.value = AddProductState(isLoading = true)
            repository.getAddProduct(name, price, category, stock).collect {
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
    fun updateProduct(productId: String, name: String? = null, price: Double? = null, category: String? = null, stock: Int? = null){
        if(_updateProductState.value.success != null && !_updateProductState.value.isLoading && _updateProductState.value.error == null ) return

        viewModelScope.launch(Dispatchers.IO) {
            _updateProductState.value = UpdateProductState(isLoading = true)
            repository.updateProduct(productId,name, price, category, stock).collect {
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

    fun isApprovedUser(userId: String, isApproved: Boolean){
//        val currentState = _isApproved.value[userId]  // finding each user state approval
//        if(currentState?.success != null) return    // don't process if already success state

        _isApproved.value = _isApproved.value.toMutableMap().apply {
            this[userId] = IsApprovedUserState(isLoading = true)
        }

        viewModelScope.launch (Dispatchers.IO){
            repository.isApprovedUser(userId,isApproved).collect {
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
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(300)
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


}