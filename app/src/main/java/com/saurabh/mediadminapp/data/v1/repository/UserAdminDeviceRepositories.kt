package com.saurabh.mediadminapp.data.v1.repository

import com.saurabh.mediadminapp.data.v1.core.ApiResult
import com.saurabh.mediadminapp.data.v1.core.PageRequest
import com.saurabh.mediadminapp.data.v1.dto.Admin
import com.saurabh.mediadminapp.data.v1.dto.AdminCreateRequest
import com.saurabh.mediadminapp.data.v1.dto.AdminUpdateRequest
import com.saurabh.mediadminapp.data.v1.dto.Device
import com.saurabh.mediadminapp.data.v1.dto.DeviceRegisterRequest
import com.saurabh.mediadminapp.data.v1.dto.Order
import com.saurabh.mediadminapp.data.v1.dto.ProfileUpdateRequest
import com.saurabh.mediadminapp.data.v1.dto.User
import com.saurabh.mediadminapp.data.v1.dto.UserCreateRequest
import com.saurabh.mediadminapp.data.v1.dto.UserUpdateRequest

interface UserRepository {
    /** GET users (paginated). Admin. */
    suspend fun list(filter: UserFilter = UserFilter(), page: PageRequest = PageRequest()): ApiResult<List<User>>

    /** GET users/{user_id}. Admin (any user) / customer (own id only, else NOT_FOUND). */
    suspend fun get(userId: String): ApiResult<User>

    /** GET users/me. Customer. */
    suspend fun me(): ApiResult<User>

    /** PATCH users/me (multipart when [image] != null); email/password change needs currentPassword. Customer. */
    suspend fun updateMe(request: ProfileUpdateRequest, image: ImagePart? = null): ApiResult<User>

    /** PATCH users/{user_id} (multipart when [image] != null). Admin. */
    suspend fun adminUpdate(userId: String, request: UserUpdateRequest, image: ImagePart? = null): ApiResult<User>

    /** DELETE users/{user_id} (soft delete). Admin. */
    suspend fun delete(userId: String): ApiResult<User>

    /** POST users/{user_id}/approve. Admin. */
    suspend fun approve(userId: String): ApiResult<User>

    /** POST users/{user_id}/block. Admin. */
    suspend fun block(userId: String): ApiResult<User>

    /** POST users/{user_id}/unblock. Admin. */
    suspend fun unblock(userId: String): ApiResult<User>

    /** POST users: public signup (multipart when [image] != null); account starts unapproved. Customer. */
    suspend fun signup(request: UserCreateRequest, image: ImagePart? = null): ApiResult<User>

    /** GET users/{user_id}/orders. Admin (any user) / customer (own id only). */
    suspend fun ordersOf(userId: String, filter: OrderFilter = OrderFilter(), page: PageRequest = PageRequest()): ApiResult<List<Order>>
}

interface AdminRepository {
    /** GET admins (paginated; q matches name/email; role = admin|disabled). Admin. */
    suspend fun list(q: String? = null, role: String? = null, page: PageRequest = PageRequest()): ApiResult<List<Admin>>

    /** GET admins/{admin_id}. Admin. */
    suspend fun get(adminId: String): ApiResult<Admin>

    /** POST admins (multipart when [image] != null). Admin. */
    suspend fun create(request: AdminCreateRequest, image: ImagePart? = null): ApiResult<Admin>

    /** PATCH admins/{admin_id} (multipart when [image] != null); role="disabled" demotes. Admin. */
    suspend fun update(adminId: String, request: AdminUpdateRequest, image: ImagePart? = null): ApiResult<Admin>

    /** DELETE admins/{admin_id} (soft delete; last active admin -> CONFLICT). Admin. */
    suspend fun delete(adminId: String): ApiResult<Admin>
}

interface DeviceRepository {
    /** POST devices: register/update this device's push token. Both. */
    suspend fun register(request: DeviceRegisterRequest): ApiResult<Device>

    /** DELETE devices/{device_id}: unregister (call before logout). Both. */
    suspend fun unregister(deviceId: String): ApiResult<Device>
}
