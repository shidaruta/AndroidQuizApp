package com.example.quizapp.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quizapp.QuizViewModel
import com.example.quizapp.R
import com.example.quizapp.Routes

@Composable
fun GradeScreen(navController: NavController, gradePercentage: Double){

    val pink = colorResource(R.color.pink)
    val gradeGreen = colorResource(id = R.color.gradeGreen)
    val yellow = colorResource(id = R.color.yellow)

    val gradeMessage = when{
        gradePercentage < 40 -> "Oh No! You might want to retake the quiz"
        gradePercentage in 40.0..70.0 -> "Good effort!"
        else -> "Great job! You did well"
    }

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
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(200.dp))
                        .size(200.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.grade),
                        contentDescription = "grade",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Column(
                    modifier = Modifier
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = gradeMessage,
                        color = yellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Achieved ${gradePercentage.toInt()}%",
                        color = gradeGreen,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold

                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Quiz Completed",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold

                    )
                }

                Spacer(modifier = Modifier.height(50.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 17.dp),
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    Button(
                        modifier = Modifier
                            .width(190.dp),
                        onClick = {
                            navController.navigate(Routes.answerScreen)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        )
                    ) {
                        Text(text = "Check correct answer")
                    }

                    Button(
                        modifier = Modifier
                            .width(190.dp),
                        onClick = {
                            navController.navigate(Routes.mainScreen)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        )
                    ) {
                        Text(text ="Return to main screen")
                    }
                }
            }
        }
    }
}
