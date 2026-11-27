package com.saurabh.mediadminapp.data.v1.repository

import com.saurabh.mediadminapp.data.v1.core.ApiResult
import com.saurabh.mediadminapp.data.v1.core.PageRequest
import com.saurabh.mediadminapp.data.v1.dto.Admin
import com.saurabh.mediadminapp.data.v1.dto.AdminCreateRequest
import com.saurabh.mediadminapp.data.v1.dto.AdminUpdateRequest
import com.saurabh.mediadminapp.data.v1.dto.Device
import com.saurabh.mediadminapp.data.v1.dto.DeviceRegisterRequest
import com.saurabh.mediadminapp.data.v1.dto.Order
import com.saurabh.mediadminapp.data.v1.dto.User

interface UserRepository {
    /** GET users (paginated). Admin. */
    suspend fun list(filter: UserFilter = UserFilter(), page: PageRequest = PageRequest()): ApiResult<List<User>>

    /** GET users/{user_id}. Admin (any user) / customer (own id only, else NOT_FOUND). */
    suspend fun get(userId: String): ApiResult<User>

    /** DELETE users/{user_id} (soft delete). Admin. */
    suspend fun delete(userId: String): ApiResult<User>

    /** POST users/{user_id}/approve. Admin. */
    suspend fun approve(userId: String): ApiResult<User>

    /** POST users/{user_id}/block. Admin. */
    suspend fun block(userId: String): ApiResult<User>

    /** POST users/{user_id}/unblock. Admin. */
    suspend fun unblock(userId: String): ApiResult<User>

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
