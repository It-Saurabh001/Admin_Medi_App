package com.saurabh.mediadminapp.repository

import android.util.Log
import androidx.compose.material3.ExposedDropdownMenuBox
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
import com.saurabh.mediadminapp.network.response.RefreshTokenResponse
import com.saurabh.mediadminapp.network.response.UpdateAdminResponse
import com.saurabh.mediadminapp.network.response.UpdateOrderResponse
import com.saurabh.mediadminapp.network.response.UpdateProductResponse
import com.saurabh.mediadminapp.network.response.UpdateUserResponse
import com.saurabh.mediadminapp.network.response.VerifyOtpResponse
import com.saurabh.mediadminapp.utils.utilityFunctions.toTextRequestBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

class Repository @Inject constructor(@param:MainApiService private val apiServices: ApiServices) {

    init {
        Log.d("PERF_TRACE", "Repository instantiated (First Repository access) [Thread: ${Thread.currentThread().name}]")
    }

    // ----------------------------
    // REFRESH TOKEN
    // ----------------------------
    suspend fun refreshToken(refreshToken: String): Flow<ResultState<RefreshTokenResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.refreshToken(refreshToken)
            handleResponse("refreshToken", response, this)
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("AdminRepository", "refreshToken exception: ${e.message}")
        }
    }

    // ----------------------------
    // CREATE ADMIN
    // ----------------------------
    suspend fun createAdmin(
        name: String,
        password: String,
        email: String,
        phoneNumber: String
    ): Flow<ResultState<CreateAdminResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.createAdmin(name, password, email, phoneNumber)
            handleResponse("createAdmin", response, this)
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("AdminRepository", "createAdmin exception: ${e.message}")
        }
    }

    // ----------------------------
    // ADMIN LOGIN
    // ----------------------------
    suspend fun loginAdmin(
        email: String,
        password: String
    ): Flow<ResultState<AdminLoginResponse>> = flow {
        Log.d("PERF_TRACE", "Repository loginAdmin START [Thread: ${Thread.currentThread().name}]")
        emit(ResultState.Loading)
        try {
            val response = apiServices.loginAdmin(email, password)
            handleResponse("loginAdmin", response, this)
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("AdminRepository", "loginAdmin exception: ${e.message}")
        }
        Log.d("PERF_TRACE", "Repository loginAdmin END [Thread: ${Thread.currentThread().name}]")
    }

    // ----------------------------
    // VERIFY ADMIN OTP
    // ----------------------------
    suspend fun verifyAdminOtp(
        adminId: String,
        otp: String
    ): Flow<ResultState<VerifyOtpResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.verifyAdminOtp(adminId, otp)
            handleResponse("verifyAdminOtp", response, this)
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("AdminRepository", "verifyAdminOtp exception: ${e.message}")
        }
    }

    // ----------------------------
    // PASSWORD RESET
    // ----------------------------
    suspend fun requestAdminPasswordReset(email: String): Flow<ResultState<PasswordResetResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.requestAdminPasswordReset(email)
            handleResponse("requestAdminPasswordReset", response, this)
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("AdminRepository", "requestAdminPasswordReset exception: ${e.message}")
        }
    }

    suspend fun resetAdminPasswordWithOtp(
        adminId: String,
        otp: String,
        newPassword: String
    ): Flow<ResultState<PasswordResetOtpResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.resetAdminPasswordWithOtp(adminId, otp, newPassword)
            handleResponse("resetAdminPasswordWithOtp", response, this)
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("AdminRepository", "resetAdminPasswordWithOtp exception: ${e.message}")
        }
    }

    // ----------------------------
    // ADMIN MANAGEMENT
    // ----------------------------
    suspend fun getAllAdmins(): Flow<ResultState<GetAllAdminResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.getAllAdmins()
            handleResponse("getAllAdmins", response, this)
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("AdminRepository", "getAllAdmins exception: ${e.message}")
        }
    }

    suspend fun updateAdmin(
        adminId: String,
        name: String? = null,
        password: String? = null,
        email: String? = null,
        phoneNumber: String? = null,
        role: String? = "admin"
    ): Flow<ResultState<UpdateAdminResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.updateAdmin(adminId, name, password, email, phoneNumber, role)
            handleResponse("updateAdmin", response, this)
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("AdminRepository", "updateAdmin exception: ${e.message}")
        }
    }

    suspend fun deleteAdmin(adminId: String): Flow<ResultState<DeleteAdminResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.deleteAdmin(adminId)
            handleResponse("deleteAdmin", response, this)
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("AdminRepository", "deleteAdmin exception: ${e.message}")
        }
    }

    // ----------------------------
    // Helper Function
    // ----------------------------
    private suspend fun <T> handleResponse(
        tag: String,
        response: Response<T>,
        emitter: kotlinx.coroutines.flow.FlowCollector<ResultState<T>>
    ) {
        if (response.isSuccessful && response.body() != null) {
            emitter.emit(ResultState.Success(response.body()!!))
            Log.d("AdminRepository", "$tag success: ${response.body()}")
        } else {
            val error = response.errorBody()?.string() ?: "Unknown error"
            emitter.emit(ResultState.Error(Exception(error)))
            Log.e("AdminRepository", "$tag error: $error")
        }
    }

    suspend fun getSpecificUser(
        userId: String
    ): Flow<ResultState<GetSpecificUserResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.getSpecificUser(userId)
            if (response.isSuccessful && response.body() != null) {
                emit(ResultState.Success(response.body()!!))
                Log.d("UserRepository", "getSpecificUser success: ${response.body()}")
            } else {
                val error = response.errorBody()?.string() ?: "Unknown error"
                emit(ResultState.Error(Exception(error)))
                Log.e("UserRepository", "getSpecificUser error: $error")
            }
        } catch (e: Exception) {
            emit(ResultState.Error(e))
            Log.e("UserRepository", "getSpecificUser exception: ${e.message}")
        }
    }

    suspend fun getAllUsers(): Flow<ResultState<GetAllUserResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.getAllUsers()
            if (response.isSuccessful  && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "getSpecificUser repository: ${ResultState.Success(response.body()!!)}")
            }
            else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
                Log.d("TAG", "getSpecificUser: repository error : ${emit(ResultState.Error(Exception(response.errorBody().toString())))}")
            }
        }
        catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }

    suspend fun updateUser(userId: String, name: String? = null, password : String?=null, isApproved: Boolean? = null, block : Boolean?=null, address : String?=null, email: String? = null, phonenumber: String? = null, pincode: String? = null, role: String? = "user"): Flow<ResultState<UpdateUserResponse>> = flow {
        emit(ResultState.Loading)
        try {
            // get response from api
            val response = apiServices.updateUser(userId, name, password, isApproved, block, address, email, phonenumber, pincode, role = role)
            if(response.isSuccessful && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "updateUser: repository : ${ResultState.Success(response.body())}")
            }else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
                Log.d("TAG", "updateUser: repository error : ${emit(ResultState.Error(Exception(response.body().toString())))}")
            }
        }
        catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }

    suspend fun isApprovedUser(user_id : String, isApproved: Boolean): Flow<ResultState<IsApproveUserResponse>> = flow {
        emit(ResultState.Loading)
        try{
            val response = apiServices.isApprovedUser(user_id,isApproved)
            if(response.isSuccessful && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "isApprovedUser: repository : ${ResultState.Success(response.body())}")
            }
            else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
                Log.d("TAG", "isApprovedUser: repository error : ${emit(ResultState.Error(Exception(response.errorBody().toString())))}")
            }
        }
        catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }
    suspend fun deleteUser(user_id: String): Flow<ResultState<DeleteUserResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.deleteUser(user_id)
            if(response.isSuccessful && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "deleteUser: repository : ${ResultState.Success(response.body())}")
            }
            else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
                Log.d("TAG", "deleteUser: repository error : ${emit(ResultState.Error(Exception(response.body().toString())))}")
            }
        }
        catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }

    suspend fun getAllProduct(): Flow<ResultState<GetAllProductResponse>> = flow{
        emit(ResultState.Loading)
        try {
            val response = apiServices.getAppProducts()
            if(response.isSuccessful && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "getAllProduct: repository : ${ResultState.Success(response.body())}")
            }else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
                Log.d("TAG", "getAllProduct: repository error : ${emit(ResultState.Error(Exception(response.errorBody().toString())))}")
            }
        }catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }

    suspend fun getAddProduct(name: String, price: Double, category: String, stock: Int, image: MultipartBody.Part? = null): Flow<ResultState<GetAddProductResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val nameBody = name.toTextRequestBody()
            val priceBody = price.toString().toTextRequestBody()
            val categoryBody = category.toTextRequestBody()
            val stockBody = stock.toString().toTextRequestBody()
            val response = apiServices.addProduct(nameBody, priceBody, categoryBody, stockBody, image)
            if(response.isSuccessful && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "getAddProduct: repository : ${ResultState.Success(response.body())}")
            }else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
                Log.d("TAG", "getAddProduct: repository error : ${response.errorBody()?.string()}")
            }
        }
        catch (e: Exception){
            emit(ResultState.Error(e))
        }
    }
    suspend fun updateProduct(productId: String, name: String? = null, price: Double? = null, category: String? = null, stock: Int? = null, image: MultipartBody.Part? = null): Flow<ResultState<UpdateProductResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val productIdBody = productId.toTextRequestBody()
            val nameBody = name?.toTextRequestBody()
            val priceBody = price?.toString()?.toTextRequestBody()
            val categoryBody = category?.toTextRequestBody()
            val stockBody = stock?.toString()?.toTextRequestBody()
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