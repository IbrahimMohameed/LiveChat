package com.example.livechatt.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.livechatt.CheckSignedIn
import com.example.livechatt.CommonProgressBar
import com.example.livechatt.DestinationScreen
import com.example.livechatt.LCViewModel
import com.example.livechatt.NavigateTo
import com.example.livechatt.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen( navController: NavController , lcViewModel: LCViewModel) {
    CheckSignedIn(vm = lcViewModel, navController = navController)
    Box(modifier = Modifier.fillMaxSize()){
        Column (modifier = Modifier
            .fillMaxSize()
            .wrapContentHeight()
            .verticalScroll(
                rememberScrollState()
            ) , horizontalAlignment = Alignment.CenterHorizontally ,
            verticalArrangement = Arrangement.Center
        ){
            val nameState = remember {
                mutableStateOf(TextFieldValue())
            }
            val numberState = remember {
                mutableStateOf(TextFieldValue())
            }
            val emailState = remember {
                mutableStateOf(TextFieldValue())
            }
            val passwordState = remember {
                mutableStateOf(TextFieldValue())
            }
            val focus = LocalFocusManager.current
            Image(painter = painterResource(id = R.drawable.whatsapp),
                contentDescription = null ,
                modifier = Modifier
                    .width(200.dp)
                    .padding(top = 16.dp)
                    .padding(8.dp)
            )
            Text(text = "Sign Up" ,
                fontSize = 30.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(8.dp)
                    .padding(top = 8.dp)
                )
            OutlinedTextField(value = nameState.value,
                onValueChange = {
                    nameState.value = it
                },
                label = {
                    Text(text = "Name",
                        modifier = Modifier.padding(8.dp)
                        )
                },
                modifier = Modifier.padding(8.dp)
            )
            OutlinedTextField(value = numberState.value,
                onValueChange = {
                    numberState.value = it
                },
                label = {
                    Text(text = "Number",
                        modifier = Modifier.padding(8.dp)
                    )
                },
                modifier = Modifier.padding(8.dp)
            )
            OutlinedTextField(value = emailState.value,
                onValueChange = {
                    emailState.value = it
                },
                label = {
                    Text(text = "Email",
                        modifier = Modifier.padding(8.dp)
                    )
                },
                modifier = Modifier.padding(8.dp)
            )
            OutlinedTextField(value = passwordState.value,
                onValueChange = {
                    passwordState.value = it
                },
                label = {
                    Text(text = "Password",
                        modifier = Modifier.padding(8.dp)
                    )
                },
                modifier = Modifier.padding(8.dp)
            )
            Button(
                onClick = {
                             lcViewModel.signUp(
                                 name = nameState.value.text ,
                                 number = numberState.value.text,
                                 email = emailState.value.text,
                                 password = passwordState.value.text)
            },
                modifier = Modifier
                    .padding(8.dp)
                    .padding(top = 8.dp),
                ) {
                Text(text = " Sign Up ")
            }
            Text(text = "Already a User ? Go to Login - > ",
                modifier = Modifier
                    .padding(8.dp)
                    .padding(top = 8.dp)
                    .clickable {
                        NavigateTo(navController, DestinationScreen.login.route)
                    }
                )
        }
    }
    if (lcViewModel.inProgress.value){
        CommonProgressBar()
    }
}
