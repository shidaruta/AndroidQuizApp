package com.example.quizapp

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AuthViewModel: ViewModel() {

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState : LiveData<AuthState> = _authState
    private val database = FirebaseDatabase.getInstance("https://quizapp-b4181-default-rtdb.asia-southeast1.firebasedatabase.app").getReference()

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _profilePictureUrl = MutableLiveData<String>()
    val profilePictureUrl: LiveData<String> get() = _profilePictureUrl

    init {
        checkAuthStatus()
    }

    fun uploadProfilePicture(
        userId: String,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.d("ProfileUpload", "Starting uploadProfilePicture for userId: $userId with Uri: $imageUri")

        val storageRef = FirebaseStorage.getInstance().reference.child("profileImages/$userId.jpg")
        val uploadTask = storageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            Log.d("ProfileUpload", "Image uploaded successfully")

            storageRef.downloadUrl.addOnSuccessListener { uri ->
                Log.d("ProfileUpload", "Download URL: $uri")

                val profileUpdates = mapOf("profilePictureUrl" to uri.toString())
                database.child("users").child(userId).updateChildren(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("ProfileUpload", "Profile picture URL updated in database")
                            onSuccess(uri.toString())

                            // Update LiveData immediately
                            _profilePictureUrl.value = uri.toString()
                        } else {
                            Log.e("ProfileUpload", "Failed to update profile picture URL in the database: ${task.exception}")
                            onFailure(task.exception ?: Exception("Failed to update profile picture URL in the database"))
                        }
                    }
            }.addOnFailureListener { exception ->
                Log.e("ProfileUpload", "Failed to get download URL: ${exception.message}")
                onFailure(exception)
            }
        }.addOnFailureListener { exception ->
            Log.e("ProfileUpload", "Failed to upload image: ${exception.message}")
            onFailure(exception)
        }
    }

    fun fetchProfilePictureUrl(uid: String) {
        database.child("users").child(uid).child("profilePictureUrl").get()
            .addOnSuccessListener { dataSnapshot ->
                val fetchedProfilePictureUrl = dataSnapshot.getValue(String::class.java) ?: ""
                Log.d("ProfileFetch", "Fetched profile picture URL: $fetchedProfilePictureUrl")
                _profilePictureUrl.value = fetchedProfilePictureUrl
            }
            .addOnFailureListener {
                Log.e("ProfileFetch", "Failed to fetch profile picture URL")
                _profilePictureUrl.value = ""
            }
    }

    fun updateUsername(userId: String, newUsername: String) {

        database.child("users").child(userId).child("username").setValue(newUsername)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _username.value = newUsername // Update the LiveData to trigger UI refresh
                    Log.d("Profile", "Username updated successfully${newUsername}.")
                } else {
                    Log.e("Profile", "Failed to update username.", task.exception)
                }
            }
    }

    fun checkAuthStatus() {
        val user = auth.currentUser
        if (user == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
            fetchUsername(user.uid)
            fetchProfilePictureUrl(user.uid)
        }
    }

    fun fetchUsername(uid: String) {
        database.child("users").child(uid).child("username").get()
            .addOnSuccessListener { dataSnapshot ->
                // Ensure fetchedUsername is non-null by providing a default value
                val fetchedUsername = dataSnapshot.getValue(String::class.java) ?: "Unknown User"
                _username.value = fetchedUsername
            }
            .addOnFailureListener {
                _username.value = "Unknown User"
            }
    }

    fun login(email: String, password: String, quizViewModel: QuizViewModel) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated

                    val user = auth.currentUser
                    user?.let {
                        val userId = it.uid

                        // Fetch username or any other user-specific data
                        fetchUsername(userId)
                        fetchProfilePictureUrl(userId)


                        // Use QuizViewModel to fetch necessary data
                        quizViewModel.fetchQuizzes()
                        quizViewModel.fetchUserId() // Fetch and observe user ID
                        quizViewModel.fetchQuizAttempts(userId) // Fetch quiz attempts for the user
                        quizViewModel.fetchUniqueCompletedQuizCount(userId) // Fetch count of unique quizzes completed

                        // You can include more quiz-related data fetching methods if needed
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Login failed")
                }
            }
    }


    fun signup(email: String, username: String, password: String, quizViewModel: QuizViewModel) {
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            _authState.value = AuthState.Error("Email, username, or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading

        val defaultProfilePictureUrl = "gs://quizapp-b4181.appspot.com/profile.png"

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let { firebaseUser ->  // Renamed 'it' to 'firebaseUser'
                        // Create user data to store in Realtime Database
                        val userData = mapOf(
                            "email" to email,
                            "username" to username,
                            "uid" to firebaseUser.uid,
                            "profilePictureUrl" to defaultProfilePictureUrl // Add default profile picture URL
                        )

                        // Store the new user's data in the Realtime Database under "users" node
                        database.child("users").child(firebaseUser.uid).setValue(userData)
                            .addOnSuccessListener {
                                _authState.value = AuthState.Authenticated

                                fetchUsername(firebaseUser.uid)
                                fetchProfilePictureUrl(firebaseUser.uid)
                                // Use QuizViewModel to fetch necessary data
                                quizViewModel.fetchQuizzes()
                                quizViewModel.fetchUserId() // Fetch and observe user ID
                                quizViewModel.fetchQuizAttempts(firebaseUser.uid) // Fetch quiz attempts for the user
                                quizViewModel.fetchUniqueCompletedQuizCount(firebaseUser.uid) //
                            }
                            .addOnFailureListener { exception ->
                                _authState.value = AuthState.Error(exception.message ?: "Failed to save user data to Realtime Database")
                            }
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Sign up failed")
                }
            }
    }


    fun signout(){
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
        _username.value = ""
        _profilePictureUrl.value = "" // Reset profile picture URL on sign out

    }

    fun clearError() {
        if(_authState.value is AuthState.Error){
            _authState.value = AuthState.Unauthenticated
        }

    }
}

sealed class AuthState{
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message : String) : AuthState()
}