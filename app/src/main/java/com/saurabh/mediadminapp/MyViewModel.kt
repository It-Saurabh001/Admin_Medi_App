package com.saurabh.mediadminapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.mediadminapp.common.ResultState
import com.saurabh.mediadminapp.repository.Repository
import com.saurabh.mediadminapp.utils.DeleteUserState
import com.saurabh.mediadminapp.utils.GetAllUserState
import com.saurabh.mediadminapp.utils.IsApprovedUserState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyViewModel: ViewModel (){
    private val repository = Repository()

    private var _getAllUserState = MutableStateFlow(GetAllUserState())
    val getAllUserState = _getAllUserState.asStateFlow()

    private var _isApproved = MutableStateFlow<Map<String, IsApprovedUserState>>(emptyMap())
    val isApprovedUser = _isApproved.asStateFlow()

    private var _deleteUserState = MutableStateFlow(DeleteUserState())
    val deleteUserState = _deleteUserState.asStateFlow()


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


}