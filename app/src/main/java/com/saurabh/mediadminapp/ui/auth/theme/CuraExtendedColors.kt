package com.saurabh.mediadminapp.ui.auth.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CuraExtendedColors(
    val success: Color,
    val successContainer: Color,
    val onSuccess: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarning: Color,
    val info: Color,
    val infoContainer: Color,
    val onInfo: Color,
)

val LocalCuraExtendedColors = staticCompositionLocalOf {
    CuraExtendedColors(
        success = CuraSuccess,
        successContainer = CuraSuccessContainer,
        onSuccess = Color.White,
        warning = CuraWarning,
        warningContainer = CuraWarningContainer,
        onWarning = Color.White,
        info = CuraInfo,
        infoContainer = CuraInfoContainer,
        onInfo = Color.White,
    )
}
