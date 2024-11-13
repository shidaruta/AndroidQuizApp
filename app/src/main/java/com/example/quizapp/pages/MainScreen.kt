package com.example.quizapp.pages

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.quizapp.AuthState
import com.example.quizapp.AuthViewModel
import com.example.quizapp.Quiz
import com.example.quizapp.QuizViewModel
import com.example.quizapp.R
import com.example.quizapp.Routes
import java.util.Locale

@Composable
fun MainScreen(navController: NavController, authViewModel: AuthViewModel, quizViewModel: QuizViewModel){
    val quizzes by quizViewModel.quizzes.observeAsState(emptyList())
    val authState = authViewModel.authState.observeAsState()
    val pink = colorResource(id = R.color.pink)
    var showDialog by remember { mutableStateOf(false) }
    var selectedQuiz by remember { mutableStateOf<Quiz?>(null) }

    val profilePictureUrl by authViewModel.profilePictureUrl.observeAsState("") // Observing the profile picture URL from AuthViewModel

    val isLoading by quizViewModel.isLoading.observeAsState(initial = true)  // Observe loading state

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate(Routes.loginScreen)
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(modifier = Modifier
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.End,

        ) {
            //Profile image
            if (profilePictureUrl.isNotEmpty()) {
                Image(
                    painter = rememberImagePainter(data = profilePictureUrl),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable {
                            navController.navigate(Routes.profileScreen)
                        }
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.profile),
                    contentDescription = "Default Profile Picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable {
                            navController.navigate(Routes.profileScreen)
                        }
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ){
        Image(
            painter = painterResource(id = R.drawable.quiz),
            contentDescription = "Quiz picture",
            contentScale = ContentScale.Inside,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Card(
            modifier = Modifier
                .shadow(2.dp, RoundedCornerShape(16.dp)),
            ) {
            Column(
                modifier = Modifier
                    .background(pink)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "DRIWA APP", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Let's do some quiz to enhance your knowledge", color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 10.dp))
                }
            }
        }

        Text("All items", textAlign = TextAlign.Start, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))

        if (isLoading) {
            // Show loading indicator when quizzes are being fetched
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text("Retrieving quizzes...", modifier = Modifier.padding(top = 16.dp))
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) {
                if (quizzes.isEmpty()) {
                    item {
                        Text(text = "No quizzes available.", modifier = Modifier.padding(16.dp))
                    }
                } else {
                    items(quizzes) { quiz ->
                        QuizItemUI(quiz = quiz) {
                            selectedQuiz = quiz
                            showDialog = true
                        }
                    }
                }
            }
        }
        if (showDialog && selectedQuiz != null) {
            StartQuizDialog(
                onConfirmStart = {
                    showDialog = false
                    selectedQuiz?.let {
                        navController.navigate("question_screen/${it.id}")
                    }
                },
                onDismiss = { showDialog = false }
            )
        }
    }
}

@Composable
fun QuizItemUI( quiz: Quiz,onQuizClick: () -> Unit){
    ElevatedCard (
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .fillMaxWidth()
            .height(80.dp)
            .shadow(8.dp, RoundedCornerShape(CornerSize(16.dp)))
            .clickable(onClick = onQuizClick)
        ,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ){
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
            ,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(.65f)
                    .align(Alignment.CenterVertically)
            ) {
                Text(text = quiz.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = quiz.subTitle, fontSize = 14.sp, color = Color.Black )
            }

            val totalDuration = quiz.duration
            if (totalDuration >= 0) {
                val minutes = totalDuration / 60
                val seconds = totalDuration % 60

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth(.4f)
                        .align(Alignment.CenterVertically)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.time),
                        contentDescription = null,
                        modifier = Modifier.size(25.dp)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }else{
                Text(
                    text = "Invalid Duration",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
fun StartQuizDialog(onConfirmStart: () -> Unit, onDismiss: () -> Unit) {
    val lightgray = colorResource(id = R.color.lightgray)

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Start Quiz?") },
        text = { Text("Are you sure you want to start the quiz?") },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = lightgray
                ),onClick = { onConfirmStart() }) {
                Text("Start")
            }
        },

        dismissButton = {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = lightgray
                ),
                onClick = { onDismiss() }) {
                Text("Cancel")
            }
        },
        containerColor = Color.White,
        textContentColor = Color.Black,
        titleContentColor = Color.Black
    )
}