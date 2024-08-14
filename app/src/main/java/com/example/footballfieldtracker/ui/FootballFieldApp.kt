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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.footballfieldtracker.data.model.User
import com.example.footballfieldtracker.ui.layout.drawer.DrawerContent
import com.example.footballfieldtracker.ui.layout.drawer.menus
import com.example.footballfieldtracker.ui.layout.screens.LoginScreen
import com.example.footballfieldtracker.ui.layout.screens.MapScreen
import com.example.footballfieldtracker.ui.layout.screens.RegisterScreen
import com.example.footballfieldtracker.ui.layout.topappbar.CustomAppBar
import com.example.footballfieldtracker.ui.viewmodels.CurrentUserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


enum class Screens {
    Login,
    Register,
    GoogleMap
}

// TODO Nije lose da view model koji ne zahteva factory, ovde init u root component!!!!

// ovo obavezno refaktorisi ne smes is composable korutin scope
@Composable
fun ProfileApp(
    currentUserViewModel: CurrentUserViewModel
) {
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    var startingScreen by remember { mutableStateOf(Screens.Login.name) }
    var isLoading by remember { mutableStateOf(true) } // Indikator za čekanje učitavanja podataka

    LaunchedEffect(firebaseUser) {
        if (firebaseUser != null) {
            // Preuzmi podatke iz Firestore-a i postavi ih u ViewModel
            val firestore = FirebaseFirestore.getInstance()
            val userId = firebaseUser.uid
            val userDocRef = firestore.collection("users").document(userId)

            try {
                val document = userDocRef.get().await() // Koristi Kotlin coroutines za asinhrono preuzimanje podataka
                if (document != null && document.exists()) {
                    Log.d("ProfileApp", "nasao dokument")
                    val user = document.toObject(User::class.java)
                    user?.let {
                        currentUserViewModel.setCurrentUser(it)
                        // Logovanje svih podataka iz currentUser preko ViewModel
                        val currentUser = currentUserViewModel.currentUser.value
                        Log.d("ProfileApp", "User ID: ${currentUser?.id}")
                        Log.d("ProfileApp", "Email: ${currentUser?.email}")
                        Log.d("ProfileApp", "Username: ${currentUser?.username}")
                        Log.d("ProfileApp", "First Name: ${currentUser?.firstName}")
                        Log.d("ProfileApp", "Last Name: ${currentUser?.lastName}")
                        Log.d("ProfileApp", "Phone Number: ${currentUser?.phoneNumber}")
                        Log.d("ProfileApp", "Score: ${currentUser?.score}")
                        Log.d("ProfileApp", "Photo Path: ${currentUser?.photoPath}")
                        Log.d("ProfileApp", "Liked Reviews: ${currentUser?.likedReviews?.joinToString()}")
                    }
                    startingScreen = Screens.GoogleMap.name
                } else {
                    Log.d("ProfileApp", "No such document")
                }
            } catch (exception: Exception) {
                Log.w("ProfileApp", "Error getting documents: ", exception)
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    // Prikazivanje FootballFieldApp samo kada su podaci učitani
    if (!isLoading) {
        FootballFieldApp(
            currentUserViewModel = currentUserViewModel,
            startingScreen = startingScreen
        )
    }
}


// TODO: u zavisnosti od starting screen, ces da pokazes topbar
@Composable
fun FootballFieldApp(
    currentUserViewModel: CurrentUserViewModel,
    startingScreen: String,
) {

    val drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val navController: NavHostController = rememberNavController()
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
                    currentUserViewModel = currentUserViewModel,
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

















