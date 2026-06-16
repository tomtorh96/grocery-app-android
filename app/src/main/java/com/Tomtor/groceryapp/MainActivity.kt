package com.Tomtor.groceryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.Tomtor.groceryapp.ui.screens.NavGraph
import com.Tomtor.groceryapp.ui.theme.GroceryAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GroceryAppTheme {
                NavGraph()
            }
        }
    }
}