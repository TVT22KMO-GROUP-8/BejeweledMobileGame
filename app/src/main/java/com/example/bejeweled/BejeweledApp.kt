package com.example.bejeweled

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.bejeweled.ui.navigation.BejeweledNavHost

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BejeweledApp(navController: NavHostController = rememberNavController(),
                 sharedPreferences: SharedPreferences){
    BejeweledNavHost(
        navController = navController,
        sharedPreferences = sharedPreferences
    )

}


//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun BejeweledAppBar(
//    canNavigateBack: Boolean,
//    navigateUp: () -> Unit,
//    modifier: Modifier = Modifier
//){
//    TopAppBar(
//        title = { Text(stringResource(id = R.string.app_name)) },
//        colors = TopAppBarDefaults.mediumTopAppBarColors(
//            containerColor = MaterialTheme.colorScheme.primaryContainer
//        ),
//        modifier = modifier,
//        navigationIcon = {
//            if (canNavigateBack) {
//                IconButton(onClick = navigateUp) {
//                    Icon(
//                        imageVector = Icons.Filled.ArrowBack,
//                        contentDescription = stringResource(R.string.back_button)
//                    )
//                }
//            }
//        }
//    )
//}
