package com.example.footballfieldtracker.ui



import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.footballfieldtracker.ui.layout.drawer.DrawerContent
import com.example.footballfieldtracker.ui.layout.drawer.menus
import com.example.footballfieldtracker.ui.layout.screens.LoginScreen
import com.example.footballfieldtracker.ui.layout.screens.MapScreen
import com.example.footballfieldtracker.ui.layout.screens.RegisterScreen
import com.example.footballfieldtracker.ui.layout.topappbar.CustomAppBar
import com.example.footballfieldtracker.ui.viewmodels.LoginViewModel
import com.example.footballfieldtracker.ui.viewmodels.RegisterViewModel
import com.example.footballfieldtracker.ui.viewmodels.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


enum class Screens {
    Login,
    Register,
    GoogleMap
}

// TODO: u zavisnosti od starting screen, ces da pokazes topbar
@Composable
fun FootballFieldApp(
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
    userViewModel: UserViewModel
) {
    val currentUser by userViewModel.currentUser.collectAsState()

    currentUser?.username?.let { Log.d("CurrentUser", it) }

    val startDestination = if (currentUser != null) Screens.GoogleMap.name else Screens.Login.name

    val navController: NavHostController = rememberNavController()
    val drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope: CoroutineScope = rememberCoroutineScope()


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
                    navController)
            },
            content = { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    Modifier.padding(paddingValues)
                ) {
                    composable(Screens.Register.name) {
                        RegisterScreen(navController = navController, registerViewModel = registerViewModel)
                    }
                    composable(Screens.Login.name) {
                        LoginScreen(navController = navController, loginViewModel = loginViewModel)
                    }
                    composable(Screens.GoogleMap.name) {
                        MapScreen(navController = navController)
                    }
                }
            }
        )
    }
}

















