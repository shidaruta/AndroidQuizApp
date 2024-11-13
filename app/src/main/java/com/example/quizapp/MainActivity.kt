package com.example.quizapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quizapp.ui.theme.QuizAppTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authViewModel : AuthViewModel by viewModels()
        val quizViewModel : QuizViewModel by viewModels()

        setContent {
            QuizAppTheme {
                MyAppNavigation(quizViewModel = quizViewModel, authViewModel = authViewModel)
//                UserListScreen()
            }
        }
    }
}

@Composable
fun UserListScreen() {
    val userViewModel: UserViewModel = viewModel()
    val users by userViewModel.users.observeAsState(emptyList())

    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(users.size) { index ->
                val user = users[index]
                Text(
                    text = "${user.name}, Age: ${user.age}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}