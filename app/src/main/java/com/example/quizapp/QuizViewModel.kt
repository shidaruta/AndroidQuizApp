package com.example.quizapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class User(
    val name: String = "",
    val age: Int = 0
)

data class Question(
    val text: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: Int = 0
)
data class Quiz(
    val id: String = "",
    val title: String = "",
    val subTitle: String = "",
    val duration: Int = 0,
    val questions: Map<String, Question> = emptyMap()
    )
data class QuizAttempt(
    val quizId: String = "",
    val quizTitle: String = "",
    val gradePercentage: Int = 0,
    val completionTime: String = ""
)
class QuizViewModel : ViewModel() {

    private val _quizzes = MutableLiveData<List<Quiz>>(emptyList())
    val quizzes: LiveData<List<Quiz>> = _quizzes

    private val database = FirebaseDatabase.getInstance("https://quizapp-b4181-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("quizzes")
    private val generalDb = FirebaseDatabase.getInstance("https://quizapp-b4181-default-rtdb.asia-southeast1.firebasedatabase.app").getReference()

    private val _questions = MutableLiveData<List<Question>>(emptyList())
    val questions: LiveData<List<Question>> get() = _questions

    private val _quizAttempts = MutableLiveData<List<QuizAttempt>>(emptyList())
    val quizAttempts: LiveData<List<QuizAttempt>> get() = _quizAttempts

    private val _userId = MutableLiveData<String>()
    val userId: LiveData<String> = _userId

    private val _quizId = MutableLiveData<String>()
    private val _quizTitle = MutableLiveData<String>()
    private val _gradePercentage = MutableLiveData<Int?>()

    val isLoading = MutableLiveData<Boolean>(false)

    private val _completedUniqueQuizCount = MutableLiveData<Int>()
    val completedUniqueQuizCount: LiveData<Int> = _completedUniqueQuizCount


    init {
        fetchUserId()
        fetchQuizzes()

        _userId.observeForever { userId ->
            if (userId != null) {
                fetchQuizAttempts(userId)
                fetchUniqueCompletedQuizCount(userId)
            }
        }
    }

    fun resetData() {
        // Reset LiveData properties
        _quizAttempts.value = emptyList()
        _userId.value = ""
        _quizId.value = ""
        _quizTitle.value = ""
        _gradePercentage.value = 0
        _completedUniqueQuizCount.value = 0

        Log.d("QuizViewModel", "All data reset")
    }

    fun fetchUserId() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            _userId.value = it.uid  // Set the userId LiveData value
            Log.d("QuizViewModel", "Fetched userId: ${it.uid}")
        } ?: run {
            Log.e("QuizViewModel", "No user is signed in")
        }
    }

    // Fetch quiz attempts for the current user
    fun fetchQuizAttempts(userId: String) {
        generalDb.child("users").child(userId).child("quizAttempts")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        _quizAttempts.value = emptyList()
                        return
                    }

                    val attempts = mutableListOf<QuizAttempt>()

                    for (attemptSnapshot in snapshot.children) {
                        val quizId = attemptSnapshot.child("quizId").getValue(String::class.java)
                        val quizTitle = attemptSnapshot.child("quizTitle").getValue(String::class.java)
                        val gradePercentage = attemptSnapshot.child("gradePercentage").getValue(Int::class.java)
                        val timestamp = attemptSnapshot.child("timestamp").getValue(String::class.java)

                        if (quizId != null && quizTitle != null && gradePercentage != null && timestamp != null) {
                            val attempt = QuizAttempt(
                                quizId = quizId,
                                quizTitle = quizTitle,
                                gradePercentage = gradePercentage,
                                completionTime = timestamp
                            )
                            attempts.add(attempt)
                        } else {
                            Log.w("QuizViewModel", "One or more fields in quiz attempt are null")
                        }
                    }

                    _quizAttempts.value = attempts
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("QuizViewModel", "Error fetching quiz attempts: ${error.message}")
                }
            })
    }

    // Set quiz data to be logged after the quiz is completed
    fun setQuizData(quizId: String, quizTitle: String, gradePercentage: Int) {
        _quizId.value = quizId
        _quizTitle.value = quizTitle
        _gradePercentage.value = gradePercentage
    }

    // Log the quiz completion with a unique attempt ID
    fun logQuizCompletion() {
        val userId = _userId.value ?: return
        val quizId = _quizId.value ?: return
        val quizTitle = _quizTitle.value ?: return
        val grade = _gradePercentage.value ?: return
        Log.d("QuizViewModel", "Logging quiz completion for userId: $userId, quizId: $quizId")

        // Get the current timestamp
        val currentTime = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timestamp = dateFormat.format(Date(currentTime))

        // Create the data to log
        val completionData = mapOf(
            "quizId" to quizId,
            "quizTitle" to quizTitle,
            "gradePercentage" to grade,
            "timestamp" to timestamp
        )

        // Generate a unique key for each quiz attempt
        val attemptId = generalDb.child("users").child(userId).child("quizAttempts").push().key


        attemptId?.let {
            // Write the quiz attempt under the unique key
            generalDb.child("users").child(userId).child("quizAttempts").child(it).setValue(completionData)
                .addOnSuccessListener {
                    Log.d("QuizViewModel", "Quiz completion logged successfully for user: $userId")
                }
                .addOnFailureListener { error ->
                    Log.e("QuizViewModel", "Failed to log quiz completion for user: $userId, error: ${error.message}")
                }
        }
    }

    fun fetchQuestionsForQuiz(quizId: String) {
        val quizQuestions = FirebaseDatabase.getInstance("https://quizapp-b4181-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("quizzes").child(quizId).child("questions")
        quizQuestions.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val questionsList = mutableListOf<Question>()
                for (questionSnapshot in snapshot.children) {
                    val question = questionSnapshot.getValue(Question::class.java)
                    question?.let { questionsList.add(it)
                    }
                    Log.w("QuizViewModel", "Question at ${questionSnapshot.key} is null or cannot be converted")
                }
                _questions.value = questionsList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("QuizViewModel", "Error fetching questions: ${error.message}")
            }
        })
    }

    fun fetchQuizzes() {
        isLoading.value = true

        Log.d("QuizViewModel", "Initialized Firebase reference to quizzes")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val quizList = mutableListOf<Quiz>()

                    for (quizSnapshot in snapshot.children) {
                        val quiz = quizSnapshot.getValue(Quiz::class.java)
                        if (quiz != null) {
                            quizList.add(quiz)
                        } else {
                            Log.w("QuizViewModel", "Quiz at ${quizSnapshot.key} is null or cannot be converted")
                        }
                    }
                    _quizzes.value = quizList
                    isLoading.value = false

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("QuizViewModel", "Error fetching quizzes: ${error.message}")
                isLoading.value = false

            }
        })
    }

    fun fetchUniqueCompletedQuizCount(userId: String) {
        Log.d("QuizViewModel", "fetchUniqueCompletedQuizCount called for userId: $userId")
        val databaseRef = FirebaseDatabase.getInstance("https://quizapp-b4181-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users/$userId/quizAttempts")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val uniqueQuizzes = mutableSetOf<String>() // To store unique quiz IDs

                    // Iterate over each child (each child represents an attempt with a unique key)
                    for (quizAttemptSnapshot in snapshot.children) {
                        // Extract the quizId from the child
                        val quizId = quizAttemptSnapshot.child("quizId").getValue(String::class.java)

                        // Log for debugging
                        Log.d("QuizViewModel", "Found quizId: $quizId")

                        // Add quizId to the set if it's not null
                        quizId?.let { uniqueQuizzes.add(it) }
                    }

                    // Post the count of unique quiz IDs
                    _completedUniqueQuizCount.postValue(uniqueQuizzes.size)
                    Log.d("QuizViewModel", "Unique quizzes count: ${uniqueQuizzes.size}")
                } else {
                    Log.d("QuizViewModel", "No quiz attempts found.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("QuizViewModel", "Error fetching unique completed quizzes: ${error.message}")
            }
        })
    }


}
