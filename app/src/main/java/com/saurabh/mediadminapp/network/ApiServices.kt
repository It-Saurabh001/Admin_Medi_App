package com.saurabh.mediadminapp.network

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
import com.saurabh.mediadminapp.network.response.GetAllUserResponse
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
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Multipart
import retrofit2.http.Part

interface ApiServices {
    // need to define end points
    // ----------------------------
    // 🔹 ADMIN AUTHENTICATION
    // ----------------------------

    @FormUrlEncoded
    @POST("admin/refreshToken")   //  Use /admin/refreshToken for consistency
    suspend fun refreshToken(
        @Field("refresh_token") refreshToken: String
    ): Response<RefreshTokenResponse>



    @FormUrlEncoded
    @POST("admin/create")
    suspend fun createAdmin(
        @Field("name") name: String,
        @Field("password") password: String,
        @Field("email") email: String,
        @Field("phoneNumber") phoneNumber: String
    ): Response<CreateAdminResponse>

    @FormUrlEncoded
    @POST("admin/login")
    suspend fun loginAdmin(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<AdminLoginResponse>

    @FormUrlEncoded
    @POST("admin/verifyOtp")
    suspend fun verifyAdminOtp(
        @Field("admin_id") adminId: String,
        @Field("otp") otp: String
    ): Response<VerifyOtpResponse>   // Verifies OTP and returns JWT tokens



    // Delete Admin (POST)
    @FormUrlEncoded
    @POST("admin/delete")
    suspend fun deleteAdmin(
        @Field("admin_id") adminId: String
    ): Response<DeleteAdminResponse>

    @FormUrlEncoded
    @PATCH("admin/update")
    suspend fun updateAdmin(
        @Field("admin_id") adminId: String,
        @Field("name") name: String? = null,
        @Field("password") password: String? = null,
        @Field("email") email: String? = null,
        @Field("phone_number") phoneNumber: String? = null,
        @Field("role") role: String? = "admin"
    ): Response<UpdateAdminResponse>


    // ----------------------------
    // PASSWORD RESET
    // ----------------------------


    @FormUrlEncoded
    @POST("admin/requestAdminPasswordReset")
    suspend fun requestAdminPasswordReset(
        @Field("email") email: String
    ): Response<PasswordResetResponse>

    @FormUrlEncoded
    @POST("admin/resetAdminPasswordWithOtp")
    suspend fun resetAdminPasswordWithOtp(
        @Field("admin_id") adminId: String,
        @Field("otp") otp: String,
        @Field("new_password") newPassword: String
    ): Response<PasswordResetOtpResponse>


    // ----------------------------
    // ADMIN/USER MANAGEMENT
    // ----------------------------


    @GET("admin/getAllAdmins")
    suspend fun getAllAdmins(): Response<GetAllAdminResponse>

    @GET("admin/getAllUsers")
    suspend fun getAllUsers(): Response<GetAllUserResponse>


    @FormUrlEncoded
    @POST("admin/user/getSpecificUser")
    suspend fun getSpecificUser(
        @Field("user_id") userId: String
    ): Response<GetSpecificUserResponse>

    @FormUrlEncoded
    @PATCH("admin/approveUser")
    suspend fun isApprovedUser(
        @Field("user_id") userId: String,
        @Field("isApproved") isApproved: Boolean
    ): Response<IsApproveUserResponse>

    @FormUrlEncoded
    @POST("admin/deleteUser")
    suspend fun deleteUser(
        @Field("user_id") userId: String
    ): Response<DeleteUserResponse>

    @FormUrlEncoded
    @PATCH("admin/updateUser")
    suspend fun updateUser(
        @Field("user_id") userId: String,
        @Field("name") name: String? = null,
        @Field("password") password : String?=null,
        @Field("isApproved") isApproved: Boolean? = null,
        @Field("block") block : Boolean?=null,
        @Field("address") address : String?=null,
        @Field("email") email: String? = null,
        @Field("phone_number") phonenumber: String? = null,
        @Field("pin_code") pincode: String? = null,
        @Field("role") role: String? = "user"
    ) : Response<UpdateUserResponse>

    // ----------------------------
    // PRODUCT MANAGEMENT
    // ----------------------------

    @GET("admin/user/getAllProducts")
    suspend fun getAppProducts(): Response<GetAllProductResponse>


    @Multipart
    @POST("admin/addProduct")
    suspend fun addProduct(
        @Part("name") name: RequestBody,
        @Part("price") price: RequestBody,
        @Part("category") category: RequestBody,
        @Part("stock") stock: RequestBody,
        @Part image: MultipartBody.Part? = null
    ) : Response<GetAddProductResponse>

    @FormUrlEncoded
    @POST("admin/user/getSpecificProduct")
    suspend fun getSpecificProduct(
        @Field("Product_id") productId: String
    ): Response<GetSpecificProductResponse>

    @Multipart
    @PATCH("admin/updateProduct")
    suspend fun updateProduct(
        @Part("Product_id") productId: RequestBody,
        @Part("name") name: RequestBody? = null,
        @Part("price") price: RequestBody? = null,
        @Part("category") category: RequestBody? = null,
        @Part("stock") stock: RequestBody? = null,
        @Part image: MultipartBody.Part? = null
    ) : Response<UpdateProductResponse>

    @FormUrlEncoded
    @POST("admin/deleteProduct")
    suspend fun deleteProduct(
        @Field("Product_id") productId: String
    ): Response<DeleteProductResponse>

    // ----------------------------
    // ORDER MANAGEMENT
    // ----------------------------

    @GET("admin/getAllOrders")
    suspend fun getAllOrders(): Response<GetAllOrdersResponse>

    @FormUrlEncoded
    @POST("admin/user/getOrdersByUserId")
    suspend fun getUserOrders(
        @Field("user_id") userId: String
    ): Response<GetUsersOrdersResponse>

    @FormUrlEncoded
    @POST("admin/user/orderById")
    suspend fun getOrderById(
        @Field("Order_id") orderId: String
    ): Response<GetOrderByIdResponse>

    @FormUrlEncoded
    @PATCH("admin/updateOrder")
    suspend fun updateOrder(
        @Field("Order_id") orderId: String,
        @Field("isApproved") isApproved: Int? = null,
        @Field("quantity") quantity: Int?= null,
        @Field("price") price: Float?=null,
        @Field("total_amount") total_amount: Float?=null,
        @Field("product_name") product_name: String?=null,
        @Field("message") message: String?=null,
        @Field("sold") sold: Int? = null
    ): Response<UpdateOrderResponse>

    @FormUrlEncoded
    @PATCH("admin/approveOrder")
    suspend fun approveOrder(
        @Field("Order_id") orderId: String,
        @Field("isApproved") isApproved: Boolean

    ): Response<ApproveOrderResponse>

    @FormUrlEncoded
    @POST("admin/deleteOrder")
    suspend fun deleteOrder(
        @Field("Order_id") orderId: String
    ): Response<DeleteOrderResponse>


    // ----------------------------
    // SELL HISTORY
    // ----------------------------

    @FormUrlEncoded
    @POST("admin/recordSell")
    suspend fun recordSellHistory(
        @Field("Order_id") orderId: String
    ) : Response<GetRecordSellHistoryResoponse>

    @GET("admin/getSellHistory")
    suspend fun getSellHistory() : Response<GetSellHistoryResponse>

    @FormUrlEncoded
    @POST("/admin/user/getSellHistoryByUserId")
    suspend fun getusersellhistory(
        @Field("user_id") userId: String
    ) : Response<GetUserSellHistoryResponse>

    @FormUrlEncoded
    @POST("admin/getproductsellhistory")
    suspend fun getProductSellHistory(
        @Field("Product_id") productId: String
    ) : Response<GetProductSellHistoryResponse>

    @FormUrlEncoded
    @POST("admin/deleteSellHistory")
    suspend fun deleteSellHistory(
        @Field("Sell_id") sellId: String
    ) : Response<GetDeleteSellHistoryResponse>
}



