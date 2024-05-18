package com.example.livechatt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.livechatt.screens.ChatListScreen
import com.example.livechatt.screens.LoginScreen
import com.example.livechatt.screens.ProfileScreen
import com.example.livechatt.screens.SignUpScreen
import com.example.livechatt.screens.SingleChatScreen
import com.example.livechatt.screens.SingleStatusScreen
import com.example.livechatt.screens.StatusListScreen
import com.example.livechatt.ui.theme.LiveChattTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class DestinationScreen(var route: String) {
    object login : DestinationScreen("login")
    object signUp : DestinationScreen("signUp")
    object profile : DestinationScreen("profile")
    object chatList : DestinationScreen("chatList")
    object statusList : DestinationScreen("statusList")
    object singleChat : DestinationScreen("singleChat/{chatId}") {
        fun createRoute(id: String) = "singleChat/$id"
    }

    object singleStatus : DestinationScreen("singleStatus/{statusId}") {
        fun createRoute(userId: String) = "singleStatus/$userId"
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LiveChattTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatAppNavigation()
                }
            }
        }
    }

    @Composable
    fun ChatAppNavigation() {
        val navController = rememberNavController()
        val vm = hiltViewModel<LCViewModel>()
        NavHost(navController = navController, startDestination = DestinationScreen.signUp.route) {
            composable(DestinationScreen.signUp.route) {
                SignUpScreen(navController, vm)
            }
            composable(DestinationScreen.login.route) {
                LoginScreen(navController, vm)
            }
            composable(DestinationScreen.chatList.route) {
                ChatListScreen(navController, vm)
            }
            composable(DestinationScreen.singleChat.route) {
                val chatId = it.arguments?.getString("chatId")
                chatId?.let {
                    SingleChatScreen(
                        navController = navController,
                        lcViewModel = vm,
                        chatId = chatId
                    )
                }
            }
            composable(DestinationScreen.statusList.route) {
                StatusListScreen(navController, vm)
            }
            composable(DestinationScreen.singleStatus.route) {
                val userId = it.arguments?.getString("userId")
                userId?.let {
                    SingleStatusScreen(navController, vm, it)
                }

            }
            composable(DestinationScreen.profile.route) {
                ProfileScreen(navController, vm)
            }
        }
    }
}




