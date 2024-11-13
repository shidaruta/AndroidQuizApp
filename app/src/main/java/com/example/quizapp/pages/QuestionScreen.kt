package com.example.quizapp.pages

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quizapp.QuizViewModel
import com.example.quizapp.R
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun QuestionScreen(
    navController: NavController,
    quizId: String?,
    quizViewModel:QuizViewModel
){
    val quiz = quizViewModel.quizzes.value?.find { it.id == quizId }
    quizViewModel.fetchQuestionsForQuiz(quizId ?: "")
    val questions by quizViewModel.questions.observeAsState(emptyList())
    val currentQuestionIndex = remember { mutableStateOf(0) }

    val pink = colorResource(id = R.color.pink)
    val timerDuration = quiz?.duration?: 300
    var timeLeft by remember { mutableIntStateOf(timerDuration)}

    var selectedOption by remember { mutableStateOf<String?>(null) }

    var score by remember { mutableStateOf(0) }

    fun checkAnswer(isCorrect: Boolean) {
        if (isCorrect) {
            score++
        }
    }

    val totalQuestions = questions.size

    LaunchedEffect(key1 = true) {
        while (timeLeft > 0){
            delay(1000L)
            timeLeft -= 1
        }
        val gradePercentage = if (questions.isNotEmpty()) {
            ((score * 100) / questions.size) // Keep everything as Int and avoid Double
        } else 0


        // Log quiz completion before navigating
        quizViewModel.setQuizData(
            quizId = quizId ?: "",
            quizTitle = quiz?.title ?: "",
            gradePercentage = gradePercentage
        )
        quizViewModel.logQuizCompletion()

        // Navigate to the grade screen
        navController.navigate("grades_screen/$gradePercentage")
    }

    val minutes = TimeUnit.SECONDS.toMinutes(timeLeft.toLong())
    val seconds = timeLeft % 60
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 26.dp, vertical = 16.dp),
        
    ) {
        if (questions.isNotEmpty()) {
            val currentQuestion = questions[currentQuestionIndex.value]
            val correctAnswer = currentQuestion.correctAnswer

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween

            ){
                Text(text = "Question ${currentQuestionIndex.value+1}/10", fontWeight = FontWeight.Bold)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.time),
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                    )
                    Text(text = formattedTime, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
                ){
                if (quiz != null) {
                    Text(
                        text = quiz.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
                    .height(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = pink
                ),

                ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = currentQuestion.text,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(pink)
                            .padding(20.dp),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Column{
                currentQuestion.options.forEachIndexed{ index,option ->
                    OptionCard(text = option,
                        isCorrect =(index ==correctAnswer) ,
                        isSelected =option==selectedOption,
                        isDisabled = selectedOption != null,
                        onClick = {
                            if (selectedOption == null) {
                                selectedOption = option
                                checkAnswer(index == correctAnswer)
                            }
                        })
                }
            }
        }else{ Text(text = "loading questions...", modifier = Modifier.padding(16.dp))}

        Spacer(modifier = Modifier.height(20.dp))
        if (selectedOption != null) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp),
                horizontalArrangement = Arrangement.End
            ){
                Button(
                    onClick = {
                              if(currentQuestionIndex.value < totalQuestions-1){
                                  currentQuestionIndex.value ++
                                  selectedOption = null
                              }else{
                                  val gradePercentage = ((score.toDouble() / totalQuestions.toFloat()) * 100).toInt()
                                  quizViewModel.setQuizData(
                                      quizId = quizId ?: "",
                                      quizTitle = quiz?.title ?: "",
                                      gradePercentage = gradePercentage
                                  )
                                  quizViewModel.logQuizCompletion()
                                  navController.navigate("grades_screen/$gradePercentage")
                              }
                              },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = pink,
                        contentColor = Color.White
                    ),
                ) {
                    Text(text = "Next")
                }
            }
        }
    }
}

@Composable
fun OptionCard(
    text: String,
    isCorrect: Boolean,
    isSelected: Boolean,
    isDisabled: Boolean,
    onClick:()-> Unit
){
    val lightGray = colorResource(id = R.color.lightgray)
    val green = colorResource(id = R.color.green)
    val red = colorResource(id = R.color.red)
    val disabledGray = Color.Gray

    val backgroundColor = when{
        isDisabled && !isSelected -> disabledGray
        isSelected && isCorrect -> green
        isSelected && !isCorrect -> red
        else -> lightGray
    }

    val modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp, horizontal = 15.dp)
        .clickable(enabled = !isDisabled && !isSelected) { onClick() }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}