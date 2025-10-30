package com.fasttimes.ui.profile

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

const val profileManagementRoute = "profile_management"

fun NavController.navigateToProfileManagement() {
    this.navigate(profileManagementRoute)
}

fun NavGraphBuilder.profileManagementScreen(onBackClick: () -> Unit) {
    navigation(
        startDestination = "profile_management_main",
        route = profileManagementRoute
    ) {
        composable("profile_management_main") {
            ProfileManagementRoute(onBackClick = onBackClick)
        }
    }
}
