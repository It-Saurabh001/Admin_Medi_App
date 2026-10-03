package com.saurabh.mediadminapp.repository

import android.util.Log
import androidx.compose.material3.ExposedDropdownMenuBox
import com.google.gson.Gson
import com.saurabh.mediadminapp.network.response.ApiErrorResponse
import com.saurabh.mediadminapp.network.response.GetAllUserResponse
import com.saurabh.mediadminapp.common.ResultState
import com.saurabh.mediadminapp.network.ApiServices
import com.saurabh.mediadminapp.network.MainApiService
import com.saurabh.mediadminapp.network.response.AdminLoginResponse
import com.saurabh.mediadminapp.network.response.ApproveOrderResponse
import com.saurabh.mediadminapp.network.response.CreateAdminResponse
import com.saurabh.mediadminapp.network.response.DeleteAdminResponse
import com.saurabh.mediadminapp.network.response.DeleteOrderResponse
import com.saurabh.mediadminapp.network.response.DeleteProductResponse
import com.saurabh.mediadminapp.network.response.DeleteUserResponse
import com.saurabh.mediadminapp.network.response.GetAddProductResponse
import com.saurabh.mediadminapp.network.response.GetAllAdminResponse
import com.saurabh.mediadminapp.network.response.GetAllOrdersResponse
import com.saurabh.mediadminapp.network.response.GetAllProductResponse
import com.saurabh.mediadminapp.network.response.GetDeleteSellHistoryResponse
import com.saurabh.mediadminapp.network.response.GetOrderByIdResponse
import com.saurabh.mediadminapp.network.response.GetProductSellHistoryResponse
import com.saurabh.mediadminapp.network.response.GetRecordSellHistoryResoponse
import com.saurabh.mediadminapp.network.response.GetSellHistoryResponse
import com.saurabh.mediadminapp.network.response.GetSpecificProductResponse
import com.saurabh.mediadminapp.network.response.GetSpecificUserResponse
import com.saurabh.mediadminapp.network.response.GetUserSellHistoryResponse
import com.saurabh.mediadminapp.network.response.GetUsersOrdersResponse
import com.saurabh.mediadminapp.network.response.IsApproveUserResponse
import com.saurabh.mediadminapp.network.response.PasswordResetOtpResponse
import com.saurabh.mediadminapp.network.response.PasswordResetResponse
import com.saurabh.mediadminapp.network.response.UpdateAdminResponse
import com.saurabh.mediadminapp.network.response.UpdateOrderResponse
import com.saurabh.mediadminapp.network.response.UpdateProductResponse
import com.saurabh.mediadminapp.network.response.UpdateUserResponse
import com.saurabh.mediadminapp.network.response.VerifyOtpResponse
import com.saurabh.mediadminapp.utils.utilityFunctions.toTextRequestBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import javax.inject.Inject

class Repository @Inject constructor(@param:MainApiService private val apiServices: ApiServices) {

    init {
        Log.d("PERF_TRACE", "Repository instantiated (First Repository access) [Thread: ${Thread.currentThread().name}]")
    }

    // ----------------------------
    // CREATE ADMIN
    // ----------------------------
    suspend fun createAdmin(
        name: String,
        password: String,
        email: String,
        phoneNumber: String
    ): Flow<ResultState<CreateAdminResponse>> = safeApiCall {
        apiServices.createAdmin(name, password, email, phoneNumber)
    }

    // ----------------------------
    // ADMIN LOGIN
    // ----------------------------
    suspend fun loginAdmin(
        email: String,
        password: String
    ): Flow<ResultState<AdminLoginResponse>> = safeApiCall {
        apiServices.loginAdmin(email, password)
    }

    // ----------------------------
    // VERIFY ADMIN OTP
    // ----------------------------
    suspend fun verifyAdminOtp(
        adminId: String,
        otp: String
    ): Flow<ResultState<VerifyOtpResponse>> = safeApiCall {
        apiServices.verifyAdminOtp(adminId, otp)
    }

    // ----------------------------
    // PASSWORD RESET
    // ----------------------------
    suspend fun requestAdminPasswordReset(email: String): Flow<ResultState<PasswordResetResponse>> = safeApiCall {
        apiServices.requestAdminPasswordReset(email)
    }

    suspend fun resetAdminPasswordWithOtp(
        adminId: String,
        otp: String,
        newPassword: String
    ): Flow<ResultState<PasswordResetOtpResponse>> = safeApiCall {
        apiServices.resetAdminPasswordWithOtp(adminId, otp, newPassword)
    }

    // ----------------------------
    // ADMIN MANAGEMENT
    // ----------------------------
    suspend fun getAllAdmins(): Flow<ResultState<GetAllAdminResponse>> = safeApiCall {
        apiServices.getAllAdmins()
    }

    suspend fun updateAdmin(
        adminId: String,
        name: String? = null,
        password: String? = null,
        email: String? = null,
        phoneNumber: String? = null
    ): Flow<ResultState<UpdateAdminResponse>> = safeApiCall {
        apiServices.updateAdmin(adminId, name, password, email, phoneNumber)
    }

    suspend fun deleteAdmin(adminId: String): Flow<ResultState<DeleteAdminResponse>> = safeApiCall {
        apiServices.deleteAdmin(adminId)
    }

    // ----------------------------
    // Helper Functions
    // ----------------------------
    private fun parseErrorMessage(rawJson: String?): String? {
        if (rawJson.isNullOrBlank()) return null
        return try {
            val errorResponse = Gson().fromJson(rawJson, ApiErrorResponse::class.java)
            errorResponse?.displayMessage ?: rawJson
        } catch (_: Exception) {
            rawJson
        }
    }

    private suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>
    ): Flow<ResultState<T>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    emit(ResultState.Success(body))
                } else {
                    emit(ResultState.Error(Exception("Empty response body with HTTP ${response.code()}")))
                }
            } else {
                val rawError = response.errorBody()?.string()
                val parsedMessage = parseErrorMessage(rawError) ?: "Request failed with HTTP ${response.code()}"
                emit(ResultState.Error(Exception(parsedMessage)))
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e))
        }
    }

    suspend fun getSpecificUser(
        userId: String
    ): Flow<ResultState<GetSpecificUserResponse>> = safeApiCall {
        apiServices.getSpecificUser(userId)
    }

    suspend fun getAllUsers(): Flow<ResultState<GetAllUserResponse>> = safeApiCall {
        apiServices.getAllUsers()
    }

    suspend fun updateUser(
        userId: String,
        name: String? = null,
        password: String? = null,
        isApproved: Boolean? = null,
        block: Boolean? = null,
        address: String? = null,
        email: String? = null,
        phonenumber: String? = null,
        pincode: String? = null,
        role: String? = "user"
    ): Flow<ResultState<UpdateUserResponse>> = safeApiCall {
        apiServices.updateUser(userId, name, password, isApproved, block, address, email, phonenumber, pincode, role = role)
    }

    suspend fun isApprovedUser(user_id: String, isApproved: Boolean): Flow<ResultState<IsApproveUserResponse>> = safeApiCall {
        apiServices.isApprovedUser(user_id, isApproved)
    }

    suspend fun deleteUser(user_id: String): Flow<ResultState<DeleteUserResponse>> = safeApiCall {
        apiServices.deleteUser(user_id)
    }

    suspend fun getAllProduct(): Flow<ResultState<GetAllProductResponse>> = safeApiCall {
        apiServices.getAppProducts()
    }

    suspend fun getAddProduct(name: String, price: Double, category: String, stock: Int, image: MultipartBody.Part? = null): Flow<ResultState<GetAddProductResponse>> = safeApiCall {
        val nameBody = name.toTextRequestBody()
        val priceBody = price.toString().toTextRequestBody()
        val categoryBody = category.toTextRequestBody()
        val stockBody = stock.toString().toTextRequestBody()
        apiServices.addProduct(nameBody, priceBody, categoryBody, stockBody, image)
    }
    suspend fun updateProduct(productId: String, name: String? = null, price: Double? = null, category: String? = null, stock: Int? = null, image: MultipartBody.Part? = null): Flow<ResultState<UpdateProductResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val productIdBody = productId.toRequestBody("text/plain".toMediaTypeOrNull())
            val nameBody = name?.toRequestBody("text/plain".toMediaTypeOrNull())
            val priceBody = price?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val categoryBody = category?.toRequestBody("text/plain".toMediaTypeOrNull())
            val stockBody = stock?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val response = apiServices.updateProduct(productIdBody, nameBody, priceBody, categoryBody, stockBody, image)
            if(response.isSuccessful && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "updateProduct: repository : ${ResultState.Success(response.body())}")
            }else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
                Log.d("TAG", "updateProduct: repository error : ${response.errorBody()?.string()}")
            }
        }catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }

    suspend fun deleteProduct(productId: String): Flow<ResultState<DeleteProductResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.deleteProduct(productId)
            if(response.isSuccessful && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "deleteProduct: repository : $${ResultState.Success(response.body())}")
            }
            else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
            }
        }
        catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }


    suspend fun getSpecificProduct(productId: String): Flow<ResultState<GetSpecificProductResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.getSpecificProduct(productId)
            if(response.isSuccessful && response.body() != null) {
                emit(ResultState.Success(response.body()!!))
                Log.d(
                    "TAG",
                    "getSpecificProduct: repository : ${ResultState.Success(response.body())}"
                )
            } else {
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
                Log.d("TAG", "getSpecificProduct: repository error : ${emit(ResultState.Error(Exception(response.body().toString())))}")
            }
        }
        catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }

    suspend fun getAllOrders(): Flow<ResultState<GetAllOrdersResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.getAllOrders()
            if(response.isSuccessful && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "getAllOrders: repository : ${ResultState.Success(response.body())}")
            }else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
                Log.d("TAG", "getAllOrders: repository error : ${emit(ResultState.Error(Exception(response.errorBody().toString())))}")
            }
        }catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }

    suspend fun getUserOrders(userId: String): Flow<ResultState<GetUsersOrdersResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.getUserOrders(userId)
            if(response.isSuccessful && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "getUserOrders: repository : ${ResultState.Success(response.body())}")
            }else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
                Log.d("TAG", "getUserOrders: repository error : ${emit(ResultState.Error(Exception(response.errorBody().toString())))}")
            }
        }catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }

    suspend fun getOrdersById(orderId: String): Flow<ResultState<GetOrderByIdResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.getOrderById(orderId)
            if(response.isSuccessful && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "getUserOrdersById: repository : ${ResultState.Success(response.body())}")
            }else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
                Log.d("TAG", "getUserOrdersById: repository error : ${emit(ResultState.Error(Exception(response.errorBody().toString())))}")
            }
        }catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }

    suspend fun updateOrder(orderId: String, isApproved: Int? = null, quantity: Int?= null, price: Float?=null, total_amount: Float?=null, product_name: String?=null, message: String?=null): Flow<ResultState<UpdateOrderResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.updateOrder(orderId, isApproved, quantity, price, total_amount, product_name, message)
            if(response.isSuccessful && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "updateOrder: repository : ${ResultState.Success(response.body())}")
            }else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
                Log.d("TAG", "updateOrder: repository error : ${emit(ResultState.Error(Exception(response.errorBody().toString())))}")
            }
        }catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }

    suspend fun deleteOrder(orderId: String): Flow<ResultState<DeleteOrderResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.deleteOrder(orderId)
            if(response.isSuccessful && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "deleteOrder: repository : ${ResultState.Success(response.body())}")
            }else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
                Log.d("TAG", "deleteOrder: repository error : ${emit(ResultState.Error(Exception(response.errorBody().toString())))}")
            }
        }catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }

    suspend fun approveOrder(orderId: String, isApproved: Boolean): Flow<ResultState<ApproveOrderResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.approveOrder(orderId, isApproved)
            if(response.isSuccessful && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "approveOrder: repository : ${ResultState.Success(response.body())}")
            }else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
                Log.d("TAG", "approveOrder: repository error : ${emit(ResultState.Error(Exception(response.errorBody().toString())))}")
            }
        }catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }

    suspend fun getAllSellHistory(): Flow<ResultState<GetSellHistoryResponse>> = flow {
        emit(ResultState.Loading)

        try {
            val response = apiServices.getSellHistory()
            if(response.isSuccessful && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "getAllSellHistory: repository : ${ResultState.Success(response.body())}")
            }else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
                Log.d("TAG", "getAllSellHistory: repository error : ${emit(ResultState.Error(Exception(response.errorBody().toString())))}")
            }
        }catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }
    suspend fun recordSellHistory(orderId: String): Flow<ResultState<GetRecordSellHistoryResoponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.recordSellHistory(orderId)
            if(response.isSuccessful && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "recordSellHistory: repository : ${ResultState.Success(response.body())}")
            }else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
                Log.d("TAG", "recordSellHistory: repository error : ${emit(ResultState.Error(Exception(response.errorBody().toString())))}")
            }
        }catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }

    suspend fun getUserSellHistory(userId: String): Flow<ResultState<GetUserSellHistoryResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.getusersellhistory(userId)
            if(response.isSuccessful && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "getUserSellHistory: repository : ${ResultState.Success(response.body())}")
            }else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
                Log.d("TAG", "getUserSellHistory: repository error : ${emit(ResultState.Error(Exception(response.errorBody().toString())))}")
            }
        }catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }

    suspend fun getProductSellHistory(productId: String): Flow<ResultState<GetProductSellHistoryResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.getProductSellHistory(productId)
            if(response.isSuccessful && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "getProductSellHistory: repository : ${ResultState.Success(response.body())}")
            }else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
                Log.d("TAG", "getProductSellHistory: repository error : ${emit(ResultState.Error(Exception(response.errorBody().toString())))}")
            }
        }catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }

    suspend fun deleteSellHistory(sellId: String): Flow<ResultState<GetDeleteSellHistoryResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.deleteSellHistory(sellId)
            if(response.isSuccessful && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "deleteSellHistory: repository : ${ResultState.Success(response.body())}")
            }else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
                Log.d("TAG", "deleteSellHistory: repository error : ${emit(ResultState.Error(Exception(response.errorBody().toString())))}")
            }
        }catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }

}