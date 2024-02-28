package com.example.metlamessanger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.metlamessanger.databinding.ActivityChatBinding
import com.example.metlamessanger.databinding.ActivityMainBinding
import com.example.metlamessanger.ui.theme.MetlaMessangerTheme
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class ChatActivity : ComponentActivity() {
    lateinit var binding: ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val database = Firebase.database
        val myRef = database.getReference("messages").child("Andrey")
        binding.bSend.setOnClickListener {
            myRef.setValue(binding.etSend.text.toString())
        }
        onChangeListener(myRef)
    }
    private fun onChangeListener(dRef: DatabaseReference){
        // dobavit child pozhe
        dRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.apply {
                    rcView.append("\n Me: "+ snapshot.value.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

}
