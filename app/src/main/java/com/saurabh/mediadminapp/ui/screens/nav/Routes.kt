package com.saurabh.mediadminapp.ui.screens.nav

import androidx.navigation.NavHostController
import com.saurabh.mediadminapp.MyViewModel
import kotlinx.serialization.Serializable

//@Serializable
sealed class Routes{
    @Serializable
    object SplashRoutes {
        const val route = "SplashRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object SignUpRoutes {
        const val route = "SignUpRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object SignInRoutes {
        const val route = "SignInRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object ForgotPasswordRoutes {
        const val route = "ForgotPasswordRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object ResetPasswordRoutes {
        const val route = "ResetPasswordRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object UpgradeRequiredRoutes {
        const val route = "UpgradeRequiredRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object AccountBlockedRoutes {
        const val route = "AccountBlockedRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object ProfileRoutes {
        const val route = "ProfileRoutes"
        operator fun invoke() = route
    }
    @Serializable
    object HomeRoutes{
        const val route = "HomeRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object SettingsRoutes {
        const val route = "SettingsRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object AboutRoutes {
        const val route = "AboutRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object ReportsRoutes {
        const val route = "ReportsRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object TopProductsRoutes {
        const val route = "TopProductsRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object AdminListRoutes {
        const val route = "AdminListRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object AdminRegisterRoutes {
        const val route = "AdminRegisterRoutes"
        operator fun invoke() = route
    }

    @Serializable
    object BarcodeScannerRoutes {
        const val route = "BarcodeScannerRoutes"
        operator fun invoke() = route
    }



    @Serializable
    class UserDetailsRoutes(val userId: String
    ){
        companion object {
            const val route = "user_details/{userId}"
            operator fun invoke(userId: String) = "user_details/$userId"
        }

    }

    @Serializable
    class UserSettingsRoutes(
        val userId : String
    ){
        companion object {
        const val route = "user_setting/{userId}"
        operator fun invoke(userId: String) = "user_setting/$userId"
        }
    }

    @Serializable
    object ProductRoutes{
        const val route = "productroutes"
        operator fun invoke() = route
    }


    @Serializable
    object HistoryRoutes{
        const val route = "HistoryRoutes"
        operator fun invoke() = route
    }


    @Serializable
    object OrdersRoutes{
        const val route = "ordersRoutes"
        operator fun invoke() = route
    }
@Serializable
    object  OrderDetailsScreen1{
        const val route = " OrderDetailsScreen1"
        operator fun invoke() = route
    }


    @Serializable
    class UpdateUserDetailsRoutes(val userId: String){
        companion object {
            const val route = "update_user_details/{userId}"
            operator fun invoke(userId: String) = "update_user_details/$userId"
        }
    }

@Serializable
class BatchListRoutes(val productId: String){
    companion object {
        const val route = "batchListRoutes/{productId}"
        operator fun invoke(productId: String) = "batchListRoutes/$productId"
    }
}

@Serializable
class BatchDetailRoutes(val batchId: String){
    companion object {
        const val route = "batchDetailRoutes/{batchId}"
        operator fun invoke(batchId: String) = "batchDetailRoutes/$batchId"
    }
}

@Serializable
class StockPurchaseRoutes(val productId: String){
    companion object {
        const val route = "stockPurchaseRoutes/{productId}"
        operator fun invoke(productId: String) = "stockPurchaseRoutes/$productId"
    }
}

@Serializable
class StockAdjustRoutes(val productId: String, val batchId: String? = null){
    companion object {
        const val route = "stockAdjustRoutes/{productId}"
        operator fun invoke(productId: String) = "stockAdjustRoutes/$productId"
    }
}

@Serializable
class SpecificProductRoutes(val productId: String){
    companion object {
        const val route = "specificProductRoutes/{productId}"
        operator fun invoke(productId: String) = "specificProductRoutes/$productId"
    }
}
@Serializable
object AddProductRoutes{
    const val route = "AddProductRoutes"
    operator fun invoke(viewModel: MyViewModel, navController: NavHostController) = route
}

@Serializable
class UpdateProductRoutes(val productId: String){
    companion object {
        const val route = "updateProductRoutes/{productId}"
        operator fun invoke(productId: String) = "updateProductRoutes/$productId"
    }
}


@Serializable
class EachUserOrderRoutes(val userId: String){
    companion object {
        const val route = "orderDetailRoutes/{userId}"
        operator fun invoke(userId: String) = "orderDetailRoutes/$userId"
    }
}

@Serializable
class SpecificOrderRoutes(val orderId: String){
    companion object {
        const val route = "specificOrderRoutes/{orderId}"
        operator fun invoke(orderId: String) = "specificOrderRoutes/$orderId"
    }
}
    @Serializable
    data class VerifyOtpRoutes(val userId: String?)

    @Serializable
    data class VerifyEmailRoutes(val email: String)




}
