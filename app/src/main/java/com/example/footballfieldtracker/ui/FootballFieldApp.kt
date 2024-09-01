package com.example.footballfieldtracker.ui


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.footballfieldtracker.services.NearbyFieldsDetectionController
import com.example.footballfieldtracker.ui.layout.drawer.DrawerContent
import com.example.footballfieldtracker.ui.layout.screens.fieldcreen.FieldScreen
import com.example.footballfieldtracker.ui.layout.screens.fieldsscreen.FieldsScreen
import com.example.footballfieldtracker.ui.layout.screens.leadboardscreen.LeaderboardScreen
import com.example.footballfieldtracker.ui.layout.screens.loginscreen.LoginScreen
import com.example.footballfieldtracker.ui.layout.screens.mapscreen.MapScreen
import com.example.footballfieldtracker.ui.layout.screens.registerscreen.RegisterScreen
import com.example.footballfieldtracker.ui.layout.topappbar.CustomAppBar
import com.example.footballfieldtracker.ui.viewmodels.FieldViewModel
import com.example.footballfieldtracker.ui.viewmodels.LoginViewModel
import com.example.footballfieldtracker.ui.viewmodels.MarkerViewModel
import com.example.footballfieldtracker.ui.viewmodels.RegisterViewModel
import com.example.footballfieldtracker.ui.viewmodels.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


enum class Screens {
    Login,
    Register,
    GoogleMap,
    Leaderboard,
    Fields,
    Field
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FootballFieldApp(
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
    userViewModel: UserViewModel,
    markerViewModel: MarkerViewModel,
    fieldViewModel: FieldViewModel,
    defaultnearbyfieldcontroller: NearbyFieldsDetectionController

) {
    val currentUser by userViewModel.currentUser.collectAsState()
    var isLoading by remember { mutableStateOf(true) }

    // Ovo koristim da proverim da li je korisnik ulogovan kada opet otvori aplikaciju
    if (!userViewModel.isUserLoggedIn() || currentUser != null) {
        isLoading = false
    }


    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(4.dp))
                Text("Loading...")
            }
        }
    } else {
        val navController: NavHostController = rememberNavController()
        val drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val coroutineScope: CoroutineScope = rememberCoroutineScope()

        val startDestination =
            if (currentUser != null) Screens.GoogleMap.name else Screens.Login.name

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = false,
            drawerContent = {
                ModalDrawerSheet {
                    DrawerContent(
                        currentUser = currentUser,
                        onAction = { route ->
                            coroutineScope.launch {
                                drawerState.close() // Zatvori drawer u oba slučaja
                                route?.let {
                                    navController.navigate(it) // Navigiraj ako ruta nije null
                                }
                            }
                        }


                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    CustomAppBar(
                        drawerState = drawerState,
                        navController = navController,
                        selectedFieldName = fieldViewModel.selectedFieldState.name,
                        loginViewModel = loginViewModel,
                        removeFilter = { markerViewModel.removeFilters() },
                    )
                },
                content = { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        Modifier.padding(paddingValues)
                    ) {
                        composable(Screens.Register.name) {
                            RegisterScreen(
                                navController = navController,
                                registerViewModel = registerViewModel
                            )
                        }
                        composable(Screens.Login.name) {
                            LoginScreen(
                                navController = navController,
                                loginViewModel = loginViewModel
                            )
                        }
                        composable(Screens.GoogleMap.name) {
                            MapScreen(
                                navController = navController,
                                userViewModel = userViewModel,
                                markerViewModel = markerViewModel,
                                selectField = { fieldViewModel.setCurrentFieldState(it)},
                                defaultnearbyfieldcontroller = defaultnearbyfieldcontroller
                            )
                        }

                        composable(Screens.Leaderboard.name) {
                            LeaderboardScreen(
                                userViewModel = userViewModel
                            )
                        }

                        composable(Screens.Fields.name) {
                            FieldsScreen(
                                navController = navController,
                                userViewModel = userViewModel,
                                markerViewModel = markerViewModel,
                                selectField = { fieldViewModel.setCurrentFieldState(it)}
                            )
                        }

                        composable(Screens.Field.name) {
                            FieldScreen(
                                userViewModel = userViewModel,
                                fieldViewModel = fieldViewModel
                            )
                        }
                    }
                }
            )
        }
    }

}























