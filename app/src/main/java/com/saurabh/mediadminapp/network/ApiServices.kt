package com.saurabh.mediadminapp.network

import com.saurabh.mediadminapp.network.response.ApproveOrderResponse
import com.saurabh.mediadminapp.network.response.DeleteOrderResponse
import com.saurabh.mediadminapp.network.response.DeleteProductResponse
import com.saurabh.mediadminapp.network.response.DeleteUserResponse
import com.saurabh.mediadminapp.network.response.GetAddProductResponse
import com.saurabh.mediadminapp.network.response.GetAllOrdersResponse
import com.saurabh.mediadminapp.network.response.GetAllProductResponse
import com.saurabh.mediadminapp.network.response.GetAllUserResponse
import com.saurabh.mediadminapp.network.response.GetOrderByIdResponse
import com.saurabh.mediadminapp.network.response.GetProductSellHistory
import com.saurabh.mediadminapp.network.response.GetSellHistoryResponse
import com.saurabh.mediadminapp.network.response.GetSpecificProductResponse
import com.saurabh.mediadminapp.network.response.GetUsersOrdersResponse
import com.saurabh.mediadminapp.network.response.IsApproveUserResponse
import com.saurabh.mediadminapp.network.response.UpdateOrderResponse
import com.saurabh.mediadminapp.network.response.UpdateProductResponse
import com.saurabh.mediadminapp.network.response.UpdateUserResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface ApiServices {
    // need to define end points
//    @FormUrlEncoded
    @GET("getAllUsers")
    suspend fun getAllUser(): Response<GetAllUserResponse>

    @FormUrlEncoded
    @PATCH("approveUser")
    suspend fun isApprovedUser(
        @Field("user_id") userId: String,
        @Field("isApproved") isApproved: Boolean
    ): Response<IsApproveUserResponse>

    @FormUrlEncoded
    @POST("deleteUser")
    suspend fun deleteUser(
        @Field("user_id") userId: String
    ): Response<DeleteUserResponse>

    @FormUrlEncoded
    @PATCH("updateUser")
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
    ) : Response<UpdateUserResponse>

// Products
    @GET("getAppProducts")
    suspend fun getAppProducts(): Response<GetAllProductResponse>


    @FormUrlEncoded
    @POST("addProduct")
    suspend fun addProduct(
        @Field("name") name: String,
        @Field("price") price: Double,
        @Field("category") category: String,
        @Field("stock") stock: Int
    ) : Response<GetAddProductResponse>

    @FormUrlEncoded
    @POST("getSpecificProduct")
    suspend fun getSpecificProduct(
        @Field("Product_id") productId: String
    ): Response<GetSpecificProductResponse>

    @FormUrlEncoded
    @PATCH("updateProduct")
    suspend fun updateProduct(
        @Field("Product_id") ProductId: String,
        @Field("name") name: String? = null,
        @Field("price") price: Double? = null,
        @Field("category") category: String? = null,
        @Field("stock") stock: Int? = null
    ) : Response<UpdateProductResponse>

    @FormUrlEncoded
    @POST("deleteProduct")
    suspend fun deleteProduct(
        @Field("Product_id") productId: String
    ): Response<DeleteProductResponse>

// Orders

    // create order in user section
    @GET("getAllOrders")
    suspend fun getAllOrders(): Response<GetAllOrdersResponse>

    @FormUrlEncoded
    @POST("getUserOrders")
    suspend fun getUserOrders(
        @Field("user_id") userId: String
    ): Response<GetUsersOrdersResponse>

    @FormUrlEncoded
    @POST("getOrderById")
    suspend fun getOrderById(
        @Field("Order_id") orderId: String
    ): Response<GetOrderByIdResponse>

    @FormUrlEncoded
    @PATCH("updateOrder")
    suspend fun updateOrder(
        @Field("Order_id") orderId: String,
        @Field("isApproved") isApproved: Boolean? = null,
        @Field("quantity") quantity: Int?= null,
        @Field("price") price: Float?=null,
        @Field("total_amount") total_amount: Float?=null,
        @Field("product_name") product_name: String?=null,
        @Field("message") message: String?=null
    ): Response<UpdateOrderResponse>

    @FormUrlEncoded
    @PATCH("approveOrder")
    suspend fun approveOrder(
        @Field("Order_id") orderId: String,
        @Field("isApproved") isApproved: Boolean

    ): Response<ApproveOrderResponse>

    @FormUrlEncoded
    @POST("deleteOrder")
    suspend fun deleteOrder(
        @Field("Order_id") orderId: String
    ): Response<DeleteOrderResponse>


// sell history
    @GET("getSellHistory")
    suspend fun getSellHistory() : Response<GetSellHistoryResponse>
// user sell history in user section

    @FormUrlEncoded
    @POST("getproductsellhistory")
    suspend fun getProductSellHistory(
        @Field("Product_id") productId: String

    ) : Response<GetProductSellHistory>





}



