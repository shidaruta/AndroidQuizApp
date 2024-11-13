package com.example.quizapp.pages

import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.quizapp.AuthViewModel
import com.example.quizapp.QuizViewModel
import com.example.quizapp.R
import com.example.quizapp.Routes
import com.google.android.gms.cast.framework.media.ImagePicker
import java.util.jar.Manifest

@Composable
fun ProfileScreen(navController: NavController, authViewModel: AuthViewModel, quizViewModel: QuizViewModel) {

    var expanded by remember { mutableStateOf(false) }
    val showProfileDialog = remember { mutableStateOf(false) }
    val showImagePicker = remember { mutableStateOf(false) }
    val showUsernameInput = remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) } // Holds the selected image URI


    val context = LocalContext.current // Get the current context

    var newUsername by remember { mutableStateOf("") } // New username input
    val pink = colorResource(R.color.pink)
    val yellow = colorResource(id = R.color.yellow)
    val username by authViewModel.username.observeAsState("")
    val quizAttempts by quizViewModel.quizAttempts.observeAsState(emptyList()) // Observe quiz attempts
    val userId by quizViewModel.userId.observeAsState("")

    val profilePictureUrl by authViewModel.profilePictureUrl.observeAsState("")

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            authViewModel.fetchUsername(userId)
            authViewModel.fetchProfilePictureUrl(userId)
            quizViewModel.fetchQuizAttempts(userId)
            quizViewModel.fetchUniqueCompletedQuizCount(userId)
            Log.d("ProfilePictureUrl", "Fetched URL: ${authViewModel.profilePictureUrl.value}")

        }
    }

    val lightgray = colorResource(id = R.color.lightgray)
    val showLogoutDialog = remember { mutableStateOf(false) }

    val uniqueQuizCount by quizViewModel.completedUniqueQuizCount.observeAsState(0)



    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                imageUri = it
                Log.d("ProfileUpload", "Selected image URI: $imageUri")  // Add this log
                context.contentResolver.openInputStream(it)?.use {
                    Log.d("ProfileUpload", "Image input stream is valid and accessible")
                } ?: run {
                    Log.e("ProfileUpload", "Image input stream is null, possibly an invalid Uri")
                    return@let // Exit early if the Uri is not valid
                }
                imageUri = it

                // Upload the image to Firebase Storage
                authViewModel.uploadProfilePicture(
                    userId = userId, // Replace with appropriate user ID
                    imageUri = it,
                    onSuccess = { downloadUrl ->
                        Toast.makeText(context, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = {exception->
                        Log.e("ProfileUpload", "Failed to upload profile picture: ${exception.message}", exception)

                        Toast.makeText(context, "Failed to upload profile picture:${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }?: run{
                Log.e("ProfileUpload", "Image URI is null")
            }

        }
    )
    // Outer Alignment Column
    Column(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        // Top bar
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painter = painterResource(R.drawable.backbutton), contentDescription = null,
                modifier = Modifier
                    .size(45.dp)
                    .clickable { navController.navigate(Routes.mainScreen) })
            Box(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(8.dp)),


            ) {
                Image(
                    painter = painterResource(R.drawable.setting1), contentDescription = null,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable { expanded = true }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White),
                ) {
                    DropdownMenuItem(
                        modifier = Modifier.background(Color.White, RoundedCornerShape(8.dp)),
                        text = { Text(text = "Edit profile", color = Color.Black) },
                        onClick = {
                        showProfileDialog.value = true
                        expanded = false
                    })
                    DropdownMenuItem(
                        modifier = Modifier.background(Color.White, RoundedCornerShape(8.dp)),
                        text = { Text(text = "Logout", color = Color.Black) },
                        onClick = {
                        showLogoutDialog.value = true // Trigger logout dialog
                        expanded = false
                    })
                }
            }
        }
//USer profile
        // Profile Picture
        if (profilePictureUrl.isNotEmpty()) {
            Image(
                painter = rememberImagePainter(data = profilePictureUrl),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
            )
        } else {
            Image(
                painter = painterResource(R.drawable.profile),
                contentDescription = "Default Profile Picture",
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = username, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(15.dp))

        Card(
            modifier = Modifier
                .padding(horizontal = 66.dp)
                .clip(RoundedCornerShape(26.dp)),
            colors = CardDefaults.cardColors(containerColor = pink),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Quizzes \n Completed",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = "$uniqueQuizCount",
                    fontWeight = FontWeight.Bold,
                    color = yellow,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = "History", textAlign = TextAlign.Start, fontWeight = FontWeight.Bold)

            if (quizAttempts.isEmpty()) {
                // Show a message when there are no attempts
                Text(
                    text = "No Attempts Yet",
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Gray, // You can change the color if you want
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn {
                    items(quizAttempts) { attempt ->

                        ElevatedCard(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .fillMaxWidth()
                                .height(80.dp)
                                .shadow(8.dp, RoundedCornerShape(CornerSize(16.dp))),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = attempt.quizTitle, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${attempt.gradePercentage}%",
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                                Text(text = attempt.completionTime, fontWeight = FontWeight.Light)
                            }
                        }
                    }
                }
            }
        }
        if (showLogoutDialog.value) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog.value = false },
                title = { Text(text = "Logout Confirmation") },
                text = { Text("Are you sure you want to logout?") },
                confirmButton = {
                    Button(
                        onClick = {
                            authViewModel.signout()
                            navController.navigate(Routes.loginScreen)
                            showLogoutDialog.value = false
                        }
                    ) {
                        Text("Logout")
                    }
                },
                dismissButton = {
                    Button(colors = ButtonDefaults.buttonColors(containerColor = lightgray),
                        onClick = { showLogoutDialog.value = false }
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = Color.White,
                textContentColor = Color.Black,
                titleContentColor = Color.Black
            )
        }
        if (showProfileDialog.value) {
            AlertDialog(
                onDismissRequest = { showProfileDialog.value = false },
                title = { Text("Edit Profile") },
                text = {
                    Column {
                        Button(onClick = {
                            showProfileDialog.value = false
                            imagePickerLauncher.launch("image/*")
                        }) {
                            Text("Change Profile Picture")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            showProfileDialog.value = false
                            showUsernameInput.value = true // Open Username input dialog
                        }) {
                            Text("    Change Username   ")
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showProfileDialog.value = false }) {
                        Text("Close")
                    }
                },
                containerColor = Color.White,
                textContentColor = Color.Black,
                titleContentColor = Color.Black
            )
        }

        if (showUsernameInput.value) {
            AlertDialog(
                onDismissRequest = { showUsernameInput.value = false },
                title = { Text("Change Username") },
                text = {
                    Column {
                        // TextField for new username input
                        TextField(
                            value = newUsername, // Use newUsername directly
                            onValueChange = { newUsername = it }, // Update without .value
                            label = { Text("New Username") },
                            placeholder = { Text("Enter new username") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        // Validate username input
                        if (newUsername.isNotBlank()) { // Use newUsername directly
                            // Call the updateUsername function from AuthViewModel
                            authViewModel.updateUsername(userId, newUsername) // Use newUsername directly
                            showUsernameInput.value = false // Close dialog after saving
                        } else {
                            // Optionally, show a toast or some feedback for invalid input
                            Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    Button(onClick = { showUsernameInput.value = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

    }
}

