package com.saurabh.mediadminapp.repository

import android.util.Log
import androidx.compose.material3.ExposedDropdownMenuBox
import com.saurabh.mediadminapp.network.response.GetAllUserResponse
import com.saurabh.mediadminapp.common.ResultState
import com.saurabh.mediadminapp.network.ApiProvider
import com.saurabh.mediadminapp.network.ApiServices
import com.saurabh.mediadminapp.network.response.ApproveOrderResponse
import com.saurabh.mediadminapp.network.response.DeleteOrderResponse
import com.saurabh.mediadminapp.network.response.DeleteProductResponse
import com.saurabh.mediadminapp.network.response.DeleteUserResponse
import com.saurabh.mediadminapp.network.response.GetAddProductResponse
import com.saurabh.mediadminapp.network.response.GetAllOrdersResponse
import com.saurabh.mediadminapp.network.response.GetAllProductResponse
import com.saurabh.mediadminapp.network.response.GetSpecificProductResponse
import com.saurabh.mediadminapp.network.response.GetUsersOrdersResponse
import com.saurabh.mediadminapp.network.response.IsApproveUserResponse
import com.saurabh.mediadminapp.network.response.UpdateOrderResponse
import com.saurabh.mediadminapp.network.response.UpdateProductResponse
import com.saurabh.mediadminapp.network.response.UpdateUserResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class Repository @Inject constructor(private val apiServices: ApiServices) {

    suspend fun getAllUsers(): Flow<ResultState<GetAllUserResponse>> = flow {
        emit(ResultState.Loading)

        try {
//            val response = ApiProvider.providerApiServices().getAllUser()
            val response = apiServices.getAllUser()
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

    suspend fun getAddProduct(name: String, price: Double, category: String, stock: Int):Flow<ResultState<GetAddProductResponse>> = flow{
        emit(ResultState.Loading)

        try {
            val response = apiServices.addProduct(name, price, category, stock)
            if(response.isSuccessful && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "getAddProduct: repository : ${ResultState.Success(response.body())}")
            }else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
                Log.d("TAG", "getAddProduct: repository error : ${emit(ResultState.Error(Exception(response.errorBody().toString())))}")
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
    suspend fun updateProduct(productId: String, name: String? = null, price: Double? = null, category: String? = null, stock: Int? = null): Flow<ResultState<UpdateProductResponse>> = flow {
        emit(ResultState.Loading)
        try {
            val response = apiServices.updateProduct(productId, name, price, category, stock)
            if(response.isSuccessful && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "updateProduct: repository : ${ResultState.Success(response.body())}")
            }else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
                Log.d("TAG", "updateProduct: repository error : ${emit(ResultState.Error(Exception(response.errorBody().toString())))}")
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
    suspend fun updateOrder(orderId: String, isApproved: Boolean? = null, quantity: Int?= null, price: Float?=null, total_amount: Float?=null, product_name: String?=null, message: String?=null): Flow<ResultState<UpdateOrderResponse>> = flow {
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

    suspend fun updateUser(userId: String, name: String? = null, password : String?=null, isApproved: Boolean? = null, block : Boolean?=null, address : String?=null, email: String? = null, phonenumber: String? = null, pincode: String? = null): Flow<ResultState<UpdateUserResponse>> = flow {
        emit(ResultState.Loading)
        try {
            // get response from api
//          val response = ApiProvider.providerApiServices().updateUser(userId, name, password, isApproved, block, address, email, phonenumber, pincode)
            val response = apiServices.updateUser(userId, name, password, isApproved, block, address, email, phonenumber, pincode)
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
//            val response = ApiProvider.providerApiServices().isApprovedUser(user_id,isApproved)
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
//            val response = ApiProvider.providerApiServices().deleteUser(user_id)
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

}