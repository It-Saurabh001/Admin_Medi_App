package com.saurabh.mediadminapp.ui.screens.nav

import androidx.navigation.NavHostController
import com.saurabh.mediadminapp.MyViewModel
import kotlinx.serialization.Serializable

//@Serializable
//sealed class Routes{
    @Serializable
    object HomeRoutes{
        const val route = "HomeRoutes"
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
        const val route = "OrdersRoutes"
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
class SpecificProductRoutes(val productId: String){
    companion object {
        const val route = "specificProductRoutes/{productId}"
        operator fun invoke(productId: String) = "SpecificProductRoutes/$productId"
    }
}
@Serializable
object AddProductRoutes{
    const val route = "AddProductRoutes"
    operator fun invoke(viewModel: MyViewModel, navController: NavHostController) = route
}


//}
