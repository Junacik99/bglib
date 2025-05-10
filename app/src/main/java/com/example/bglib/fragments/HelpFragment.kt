package com.example.bglib.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment

// Default help content
@Composable
fun DefaultHelp(){
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)){
        Text(
            text = "Help Instructions",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "1. This is the first instruction.",
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "2. This is the second instruction.",
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "3. This is the third instruction.",
            modifier = Modifier.padding(bottom = 8.dp)
        )
        // TODO ... add more instructions as needed
    }
}

/************************************************
 * A fragment for displaying over CameraX       *
 ***********************************************/
class HelpFragment (val helpContent: @Composable () -> Unit = { DefaultHelp() }) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                helpContent()
            }
        }
    }
}