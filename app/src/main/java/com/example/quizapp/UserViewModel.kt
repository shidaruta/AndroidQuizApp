package com.example.quizapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserViewModel : ViewModel() {
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val databaseReference = FirebaseDatabase.getInstance("https://quizapp-b4181-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users")

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        Log.d("UserViewModel", "Adding ValueEventListener")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("UserViewModel", "onDataChange called")
                val userList = mutableListOf<User>()
                for (userSnapshot in snapshot.children) {
                    Log.d("UserViewModel", "Snapshot key: ${userSnapshot.key}")
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null) {
                        Log.d("UserViewModel", "User found: $user")
                        userList.add(user)
                    } else {
                        Log.e("UserViewModel", "User is null")
                    }
                }
                _users.value = userList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserViewModel", "Error fetching users: ${error.message}")
            }
        })
    }
}