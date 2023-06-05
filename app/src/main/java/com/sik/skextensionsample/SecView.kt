package com.sik.skextensionsample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.sik.sikroute.BaseView

class SecView : BaseView() {
    override fun initViewModel() {

    }

    @Composable
    override fun InitView(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .clickable(true) {
                    navController.popBackStack()
                }
        ) {
            Text(
                text = "${navBackStackEntry.arguments?.getString("id")},${
                    navBackStackEntry.arguments?.getString(
                        "name"
                    )
                }"
            )
        }
    }
}