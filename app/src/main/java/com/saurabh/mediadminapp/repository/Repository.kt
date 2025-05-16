package com.saurabh.mediadminapp.repository

import android.util.Log
import androidx.compose.material3.ExposedDropdownMenuBox
import com.saurabh.mediadminapp.network.response.GetAllUserResponse
import com.saurabh.mediadminapp.common.ResultState
import com.saurabh.mediadminapp.network.ApiProvider
import com.saurabh.mediadminapp.network.response.DeleteUserResponse
import com.saurabh.mediadminapp.network.response.IsApproveUserResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class Repository {

    suspend fun getAllUsers(): Flow<ResultState<GetAllUserResponse>> = flow {
        emit(ResultState.Loading)

        try {
            val response = ApiProvider.providerApiServices().getAllUser()
            if (response.isSuccessful  && response.body() != null){
                emit(ResultState.Success(response.body()!!))
                Log.d("TAG", "getSpecificUser repository: ${ResultState.Success(response.body()!!)}")

            }
            else{
                emit(ResultState.Error(Exception(response.errorBody()?.string())))
            }

        }
        catch (e: Exception){
            emit(ResultState.Error(e))

        }
    }
    suspend fun isApprovedUser(user_id : String, isApproved: Boolean): Flow<ResultState<IsApproveUserResponse>> = flow {
        emit(ResultState.Loading)
        try{
            val response = ApiProvider.providerApiServices().isApprovedUser(user_id,isApproved)
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
            val response = ApiProvider.providerApiServices().deleteUser(user_id)
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