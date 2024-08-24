package com.example.footballfieldtracker.ui


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.footballfieldtracker.data.model.User
import com.example.footballfieldtracker.services.NearbyFieldsDetectionController
import com.example.footballfieldtracker.ui.layout.drawer.DrawerContent
import com.example.footballfieldtracker.ui.layout.drawer.menus
import com.example.footballfieldtracker.ui.layout.screens.fieldcreen.FieldScreen
import com.example.footballfieldtracker.ui.layout.screens.fieldsscreen.FieldsScreen
import com.example.footballfieldtracker.ui.layout.screens.leadboardscreen.LeadboardScreen
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
    Leadboard,
    Fields,
    Field
}

// TODO: STAVI OVO SVE U JEDAN COMPOSABLE
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FootballApp(
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
    userViewModel: UserViewModel,
    markerViewModel: MarkerViewModel,
    fieldViewModel: FieldViewModel,
    defaultnearbyfieldcontroller: NearbyFieldsDetectionController
) {

    // Ovo koristim da proverim da li je korisnik ulogovan kada opet otvori aplikaciju
    // Todo: kad imas vreme refaktorisi ovo, neka ga dole sta ce ti ovde
    val currentUser by userViewModel.currentUser.collectAsState()
    val isLoading by userViewModel.loading.collectAsState()


    if (isLoading) {
        // Show a loading indicator while checking the user status
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        FootballFieldApp(
            loginViewModel = loginViewModel,
            registerViewModel = registerViewModel,
            userViewModel = userViewModel,
            currentUser = currentUser,
            markerViewModel = markerViewModel,
            fieldViewModel = fieldViewModel,
            defaultnearbyfieldcontroller = defaultnearbyfieldcontroller
        )
    }


}

// TODO: u zavisnosti od starting screen, ces da pokazes topbar
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FootballFieldApp(
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
    userViewModel: UserViewModel,
    currentUser: User?,
    markerViewModel: MarkerViewModel,
    fieldViewModel: FieldViewModel,
    defaultnearbyfieldcontroller: NearbyFieldsDetectionController
) {
    val navController: NavHostController = rememberNavController()
    val drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    val startDestination = if (currentUser != null) Screens.GoogleMap.name else Screens.Login.name

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    menus = menus,
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
                    drawerState,
                    loginViewModel,
                    fieldViewModel,
                    navController
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
                        LoginScreen(navController = navController, loginViewModel = loginViewModel)
                    }
                    composable(Screens.GoogleMap.name) {
                        MapScreen(
                            navController = navController,
                            userViewModel = userViewModel,
                            markerViewModel = markerViewModel,
                            fieldViewModel = fieldViewModel,
                            defaultnearbyfieldcontroller = defaultnearbyfieldcontroller)
                    }

                    composable(Screens.Leadboard.name) {
                        LeadboardScreen(navController = navController, userViewModel = userViewModel)
                    }

                    composable(Screens.Fields.name) {
                        // ispravi, ne moze username!!!!!
                        // i ispravi da uzmes zadva dva iz Grad/Drzava
                        FieldsScreen(
                            navController = navController,
                            userViewModel = userViewModel,
                            markerViewModel = markerViewModel,
                            fieldViewModel = fieldViewModel)
                    }

                    composable(Screens.Field.name) {
                        FieldScreen(
                            navController = navController,
                            userViewModel = userViewModel,
                            fieldViewModel = fieldViewModel
                        )
                    }
                }
            }
        )
    }
}























