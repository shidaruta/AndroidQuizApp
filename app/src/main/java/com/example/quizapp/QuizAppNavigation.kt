package com.example.quizapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.quizapp.pages.AnswerScreen
import com.example.quizapp.pages.GradeScreen
import com.example.quizapp.pages.LoginScreen
import com.example.quizapp.pages.MainScreen
import com.example.quizapp.pages.ProfileScreen
import com.example.quizapp.pages.QuestionScreen
import com.example.quizapp.pages.RegisterScreen

@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel, quizViewModel: QuizViewModel){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.loginScreen, builder = {
        composable(Routes.loginScreen) {
            LoginScreen(navController, authViewModel, quizViewModel)
        }
        composable(Routes.registerScreen) {
            RegisterScreen(navController, authViewModel, quizViewModel)
        }
        composable(Routes.mainScreen) {
            MainScreen(navController, authViewModel, quizViewModel)
        }
        composable(Routes.profileScreen) {
            ProfileScreen(navController, authViewModel, quizViewModel)
        }
        composable(Routes.answerScreen){
            AnswerScreen(navController, quizViewModel)
        }
        composable(
            route = "question_screen/{quizId}",
            arguments = listOf(navArgument("quizId") {
                type = androidx.navigation.NavType.StringType
            })
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId")
            QuestionScreen(navController, quizId = quizId, quizViewModel)
        }
        composable(
            route = "grades_screen/{gradePercentage}",
            arguments = listOf(navArgument("gradePercentage") {
                type = androidx.navigation.NavType.FloatType
            })
        ) { backStackEntry ->
            val gradePercentage = backStackEntry.arguments?.getFloat("gradePercentage") ?: 0f
            GradeScreen(navController, gradePercentage.toDouble())
        }
    })
}