package com.example.quizapp.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quizapp.AuthState
import com.example.quizapp.AuthViewModel
import com.example.quizapp.QuizViewModel
import com.example.quizapp.R
import com.example.quizapp.Routes
import com.example.quizapp.ui.theme.QuizAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen( navController: NavController, authViewModel: AuthViewModel, quizViewModel: QuizViewModel){
    val pink = colorResource(id = R.color.pink)
    val lightgray= colorResource(id = R.color.lightgray)

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState = authViewModel.authState.observeAsState()

    var isLoading  by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var authError by remember{ mutableStateOf<String?>(null)}

    LaunchedEffect(authState.value){
        when(authState.value){
            is AuthState.Authenticated -> {
                isLoading = false
                navController.navigate(Routes.loginScreen)
                authViewModel.clearError()
            }
            is AuthState.Error -> {
                isLoading = false
                authError = (authState.value as AuthState.Error).message
                authViewModel.clearError()
            }
            else -> Unit
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(pink)
    ) {
       Column(
           verticalArrangement = Arrangement.Bottom,
           horizontalAlignment = Alignment.Start,
           modifier = Modifier
               .background(pink)
               .height(300.dp)
               .fillMaxWidth()
               .padding(start = 15.dp, bottom = 30.dp)
       ) {
           Text(text = "DRIWA",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
           )
           Text(
               text = "Welcome to Driwa",
               fontSize = 16.sp,
               color = Color.White
           )
       }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight(1f)
                .clip(RoundedCornerShape(topStart = 55.dp, topEnd = 55.dp))
                .fillMaxWidth()
                .background(Color.White)
        ) {

            Text(text = "Register",
                fontWeight = FontWeight.Bold,
                color = pink,
                fontSize = 25.sp,
                modifier = Modifier.padding(top = 30.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            TextField(
                value = email,
                shape = RoundedCornerShape(30.dp),
                onValueChange = {newValue->
                    email = newValue
                    emailError = null
                                },
                label = { Text(text = "Email") },

                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    containerColor = lightgray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black
                )
            )
            emailError?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                value = username,
                shape = RoundedCornerShape(30.dp),
                onValueChange = {newValue->
                    username = newValue
                    usernameError = null
                },
                label = { Text(text = "Username") },

                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    containerColor = lightgray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black
                )
            )
            usernameError?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                value = password,
                shape = RoundedCornerShape(30.dp),
                onValueChange = { newValue ->
                    password = newValue
                    passwordError = null
                },
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    containerColor = lightgray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black,
                    focusedTextColor = Color.Black,
                    cursorColor = Color.Black
                ),
                label = { Text(text = "Password") }
            )
            passwordError?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            Button(

                onClick = {
                    if(email.isEmpty()){
                        emailError = "Email cannot be empty"
                    }
                    if(username.isEmpty()){
                        usernameError = "Username cannot be empty"
                    }
                    if(password.isEmpty()){
                        passwordError = "Password cannot be empty"
                    }
                    if(email.isNotEmpty() && password.isNotEmpty()){
                        isLoading = true
                        authViewModel.signup(username =username,email= email, password =  password, quizViewModel = quizViewModel)
                    }

                },
                modifier = Modifier
                    .width(280.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = pink,
                    contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {

                Text(text = "Create account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp))

            }

            Spacer(modifier = Modifier.height(20.dp))

            authError?.let{
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))


            val annotatedText = buildAnnotatedString {
                append("Already have an account? ")
                pushStringAnnotation(tag = "SIGN_UP", annotation = "sign_up")
                withStyle(
                    style = SpanStyle(
                        color = Color.Blue,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Login")
                }
                pop()
            }

            ClickableText(
                text = annotatedText,

                onClick = { offset ->
                    annotatedText.getStringAnnotations(tag = "SIGN_UP", start = offset, end = offset)
                        .firstOrNull()?.let {
                            navController.navigate(Routes.loginScreen)
                        }
                },
                style = androidx.compose.ui.text.TextStyle(fontSize = 16.sp)
            )
        }
    }
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}
