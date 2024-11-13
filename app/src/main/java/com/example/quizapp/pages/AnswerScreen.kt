package com.example.quizapp.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quizapp.QuizViewModel
import com.example.quizapp.R
import com.example.quizapp.Routes

@Composable
fun AnswerScreen(
    navController: NavController,
    viewModel: QuizViewModel
                 ){
    val pink = colorResource(R.color.pink)
    val questions = viewModel.questions.observeAsState(listOf())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center

    ) {
        Card(
            modifier = Modifier
                .fillMaxHeight(.8f)
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = pink
            ),

            ) {

            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(.8f)
                        .padding(top = 10.dp)
                ) {
                    itemsIndexed(questions.value){index, question->
                        val correctAnswer = question.correctAnswer
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally

                        ) {
                            Text("Q${index+1}: ${question.text}", color = Color.White)
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(color = Color.White)) {
                                        append("Correct answer: ")
                                    }
                                    withStyle(style = SpanStyle(color = Color.Green, fontWeight = FontWeight.Bold)) {
                                        append(question.options[correctAnswer])
                                    }
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
                Button(
                    onClick = {
                        navController.navigate(Routes.mainScreen)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    )
                ) {
                    Text(text = "Done")
                }
            }
        }
    }
}