package com.example.livechatt.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.livechatt.CommonDivider
import com.example.livechatt.CommonImage
import com.example.livechatt.CommonProgressBar
import com.example.livechatt.CommonRow
import com.example.livechatt.DestinationScreen
import com.example.livechatt.LCViewModel
import com.example.livechatt.NavigateTo
import com.example.livechatt.TitleText

@Composable
fun StatusListScreen(navController: NavController, lcViewModel: LCViewModel) {
    val inProgress = lcViewModel.inProgressStatus.value
    if (inProgress) {
        CommonProgressBar()
    } else {
        val statutes = lcViewModel.status.value
        val userData = lcViewModel.userData.value
        val myStatutes = statutes.filter {
            it.user.userId == userData?.userId
        }
        val otherStatutes = statutes.filter {
            it.user.userId != userData?.userId
        }
        val launcher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    lcViewModel.uploadStatus(uri)
                }
            }

        Scaffold(
            floatingActionButton = {
                FAB {
                    launcher.launch("image/*")
                }
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    TitleText(title = "Status")
                    if (statutes.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "No Statutes Available")
                        }
                    } else {
                        if (myStatutes.isNotEmpty()) {
                            CommonRow(
                                imageUrl = myStatutes[0].user.imageUrl,
                                name = myStatutes[0].user.name
                            ) {
                                NavigateTo(
                                    navController,
                                    DestinationScreen.singleStatus.createRoute(userId = myStatutes[0].user.userId!!)
                                )
                            }
                            CommonDivider()
                            val uniqueUsers = otherStatutes.map { it.user }.toSet().toList()
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(uniqueUsers) { user ->
                                    CommonRow(imageUrl = user.imageUrl, name = user.name) {
                                        NavigateTo(
                                            navController,
                                            DestinationScreen.singleStatus.createRoute(user.userId!!)
                                        )
                                    }
                                }
                            }
                        }

                    }
                    BottomNavigationMenu(
                        selectedItem = BottomNavigationItem.STATUS_LIST,
                        navController = navController
                    )
                }
            }
        )


    }

}


@Composable
fun FAB(
    onFabClicked: () -> Unit,
) {
    FloatingActionButton(
        onClick = onFabClicked,
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = Modifier.padding(bottom = 40.dp)
    ) {
        Icon(Icons.Rounded.Edit, contentDescription = "Add Status", tint = Color.White)
    }
}