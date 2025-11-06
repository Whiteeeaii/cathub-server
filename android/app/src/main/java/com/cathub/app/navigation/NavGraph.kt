package com.cathub.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cathub.app.ui.home.HomeScreen
import com.cathub.app.ui.profile.ProfileListScreen
import com.cathub.app.ui.profile.ProfileDetailScreen
import com.cathub.app.ui.profile.AddCatScreen
import com.cathub.app.ui.recognition.RecognitionScreen
import com.cathub.app.ui.report.ReportScreen

/**
 * 导航路由
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Recognition : Screen("recognition")
    object Report : Screen("report")
    object ProfileList : Screen("profile_list")
    object ProfileDetail : Screen("profile_detail/{catId}") {
        fun createRoute(catId: Int) = "profile_detail/$catId"
    }
    object AddCat : Screen("add_cat")
}

/**
 * 导航图
 */
@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // 主页
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToRecognition = {
                    navController.navigate(Screen.Recognition.route)
                },
                onNavigateToReport = {
                    navController.navigate(Screen.Report.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.ProfileList.route)
                }
            )
        }
        
        // 识别页
        composable(Screen.Recognition.route) {
            RecognitionScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProfile = { catId ->
                    navController.navigate(Screen.ProfileDetail.createRoute(catId))
                },
                onNavigateToAddCat = {
                    navController.navigate(Screen.AddCat.route)
                }
            )
        }
        
        // 上报页
        composable(Screen.Report.route) {
            ReportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // 档案列表
        composable(Screen.ProfileList.route) {
            ProfileListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { catId ->
                    navController.navigate(Screen.ProfileDetail.createRoute(catId))
                },
                onNavigateToAddCat = {
                    navController.navigate(Screen.AddCat.route)
                }
            )
        }
        
        // 档案详情
        composable(
            route = Screen.ProfileDetail.route,
            arguments = listOf(navArgument("catId") { type = NavType.IntType })
        ) { backStackEntry ->
            val catId = backStackEntry.arguments?.getInt("catId") ?: return@composable
            ProfileDetailScreen(
                catId = catId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // 添加猫咪
        composable(Screen.AddCat.route) {
            AddCatScreen(
                onNavigateBack = { navController.popBackStack() },
                onCatCreated = { catId ->
                    navController.popBackStack()
                    navController.navigate(Screen.ProfileDetail.createRoute(catId))
                }
            )
        }
    }
}

