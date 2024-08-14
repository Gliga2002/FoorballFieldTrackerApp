package com.example.footballfieldtracker.ui



import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.footballfieldtracker.ui.layout.drawer.DrawerContent
import com.example.footballfieldtracker.ui.layout.drawer.menus
import com.example.footballfieldtracker.ui.layout.screens.LoginScreen
import com.example.footballfieldtracker.ui.layout.screens.MapScreen
import com.example.footballfieldtracker.ui.layout.screens.RegisterScreen
import com.example.footballfieldtracker.ui.layout.topappbar.CustomAppBar
import com.example.footballfieldtracker.ui.viewmodels.CurrentUserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// TODO ipak treba parametar da se prosledi!!!!
enum class Screens {
    Login,
    Register,
    GoogleMap
}


@Composable
fun ProfileApp(currentUserViewModel: CurrentUserViewModel) {
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val startingScreen =
        if (firebaseUser == null){
            Screens.Login.name
        }else{
            Screens.GoogleMap.name
        }
    FootballFieldApp(currentUserViewModel, startingScreen = startingScreen)
}




// TODO: u zavisnosti od starting screen, ces da pokazes topbar
@Composable
fun FootballFieldApp(
    currentUserViewModel: CurrentUserViewModel,
    startingScreen: String,
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
) {

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: Screens.Register.name

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.9f) // Set height to 80% of screen height
            ) {
                DrawerContent(
                    menus = menus,
                    onMenuClick = { route ->
                    coroutineScope.launch {
                        drawerState.close()
                    }
                    navController.navigate(route)
                },
                    onClose = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    }

                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CustomAppBar(drawerState, currentRoute)
            },
            content = { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = startingScreen,
                    Modifier.padding(paddingValues)
                ) {
                    composable(Screens.Register.name) {
                        RegisterScreen(navController = navController)
                    }
                    composable(Screens.Login.name) {
                        LoginScreen(navController = navController, currentUserViewModel)
                    }
                    composable(Screens.GoogleMap.name) {
                        MapScreen(navController = navController)
                    }
                }
            }
        )
    }
}

















