package com.saurabh.mediadminapp.ui.screens.nav

import kotlinx.serialization.Serializable
import java.util.Objects

sealed class Routes{
    @Serializable
    object HomeRoutes

    @Serializable
    class UserDetailsRoutes(
        val user_id: String
    )

    @Serializable
    class UserSettingsRoutes(
        val user_id : String
    )
}
