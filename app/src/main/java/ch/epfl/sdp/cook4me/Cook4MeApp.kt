package ch.epfl.sdp.cook4me

import AddProfileInfoScreen
import SignUpScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import androidx.navigation.navArgument
import ch.epfl.sdp.cook4me.permissions.ComposePermissionStatusProvider
import ch.epfl.sdp.cook4me.permissions.PermissionStatusProvider
import ch.epfl.sdp.cook4me.persistence.model.Post
import ch.epfl.sdp.cook4me.ui.chat.ChannelScreen
import ch.epfl.sdp.cook4me.ui.detailedevent.DetailedEventScreen
import ch.epfl.sdp.cook4me.ui.eventform.CreateEventScreen
import ch.epfl.sdp.cook4me.ui.login.LoginScreen
import ch.epfl.sdp.cook4me.ui.map.MapPermissionWrapper
import ch.epfl.sdp.cook4me.ui.navigation.BottomNavigationBar
import ch.epfl.sdp.cook4me.ui.navigation.mainScreens
import ch.epfl.sdp.cook4me.ui.overview.OverviewScreen
import ch.epfl.sdp.cook4me.ui.profile.EditProfileScreen
import ch.epfl.sdp.cook4me.ui.profile.PostDetails
import ch.epfl.sdp.cook4me.ui.profile.ProfileScreen
import ch.epfl.sdp.cook4me.ui.recipeFeed.RecipeFeed
import ch.epfl.sdp.cook4me.ui.recipeform.CreateRecipeScreen
import ch.epfl.sdp.cook4me.ui.signUp.SignUpViewModel
import ch.epfl.sdp.cook4me.ui.tupperwareform.CreateTupperwarePermissionWrapper
import ch.epfl.sdp.cook4me.ui.tupperwareswipe.TupperwareSwipeScreen
import com.google.firebase.auth.FirebaseAuth

/**
 * enum values that represent the screens in the app
 */
private enum class Screen {
    Login,
    CreateTupperwareScreen,
    TupperwareSwipeScreen,
    OverviewScreen,
    ProfileScreen,
    CreateRecipeScreen,
    EditProfileScreen,
    Event,
    CreateEventScreen,
    DetailedEventScreen,
    SignUpScreen,
    PostDetails,
    ChatScreen,
    SignUpUserInfos,
    RecipeFeed
}

sealed class ScreenWithArgs(val name: String) {
    object DetailedEventScreen : ScreenWithArgs("detailed_event_screen/{eventId}") {
        fun createRoute(eventId: String) = "detailed_event_screen/$eventId"
    }
}

sealed class BottomNavScreen(val route: String, val icon: ImageVector, val title: String) {
    object Tupperwares :
        BottomNavScreen(Screen.TupperwareSwipeScreen.name, Icons.Filled.Home, "Tups")

    object Events : BottomNavScreen(Screen.Event.name, Icons.Filled.Star, "Events")
    object Recipes : BottomNavScreen(Screen.RecipeFeed.name, Icons.Filled.List, "Recipes")
    object Profile : BottomNavScreen(Screen.ProfileScreen.name, Icons.Filled.Person, "Profile")
    object Menu : BottomNavScreen(Screen.OverviewScreen.name, Icons.Filled.Menu, "Menu")
    object Chat : BottomNavScreen(Screen.ChatScreen.name, Icons.Filled.Chat, "Chat")
}

@Composable
fun Cook4MeApp(
    permissionStatusProvider: PermissionStatusProvider = ComposePermissionStatusProvider(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CAMERA
        )
    )
) {
    // initialize the view model for the sign up screen
    val singUpViewModel = remember { SignUpViewModel() }
    // initialize the auth object for authentication matters
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val isAuthenticated = remember { mutableStateOf(auth.currentUser != null) }

    // Navigation controller
    val navController = rememberNavController()

    val startScreen: String = if (isAuthenticated.value) {
        BottomNavScreen.Recipes.route
    } else {
        Screen.Login.name
    }

    val screensWithBottomBar = mainScreens.map { it.route }
    val shouldShowBottomBar = navController
        .currentBackStackEntryAsState().value?.destination?.route in screensWithBottomBar

    val navGraph = navController.createGraph(startDestination = startScreen) {
        composable(BottomNavScreen.Tupperwares.route) {
            TupperwareSwipeScreen(
                onCreateNewTupperware = { navController.navigate(Screen.CreateTupperwareScreen.name) }
            )
        }
        composable(BottomNavScreen.Events.route) {
            MapPermissionWrapper(
                permissionStatusProvider = permissionStatusProvider,
                onCreateNewEventClick = { navController.navigate(Screen.CreateEventScreen.name) },
                onDetailedEventClick = { eventId ->
                    navController.navigate(ScreenWithArgs.DetailedEventScreen.createRoute(eventId))
                },
            )
        }
        composable(BottomNavScreen.Profile.route) { ProfileScreen() }
        composable(BottomNavScreen.Menu.route) {
            OverviewScreen(
                onMapClick = { navController.navigate(Screen.Event.name) },
                onProfileClick = { navController.navigate(Screen.ProfileScreen.name) },
                onEditProfileClick = { navController.navigate(Screen.EditProfileScreen.name) },
                onAddTupperwareClick = {
                    navController.navigate(Screen.CreateTupperwareScreen.name)
                },
                onSwipeTupperwareClick = {
                    navController.navigate(BottomNavScreen.Tupperwares.route)
                },
                onAddEventClick = { navController.navigate(Screen.CreateEventScreen.name) },
                onAddSignUpClick = { navController.navigate(Screen.SignUpScreen.name) },
                onPostClick = { navController.navigate(Screen.PostDetails.name) },
                onDetailedEventClick = { navController.navigate(Screen.DetailedEventScreen.name) },
                onAddRecipeClick = { navController.navigate(Screen.CreateRecipeScreen.name) },
                signOutNavigation = { navController.navigate(Screen.Login.name) },
                onRecipeFeedClick = { navController.navigate(Screen.RecipeFeed.name) }
            )
        }
        composable(route = Screen.EditProfileScreen.name) {
            EditProfileScreen(
                onCancelListener = { navController.navigate(Screen.OverviewScreen.name) },
                onSuccessListener = { navController.navigate(Screen.OverviewScreen.name) },
            )
        }
        composable(route = Screen.CreateTupperwareScreen.name) {
            CreateTupperwarePermissionWrapper(
                permissionStatusProvider = permissionStatusProvider,
                onCancel = {
                    navController.navigate(Screen.TupperwareSwipeScreen.name)
                },
                onSuccessfulSubmit = {
                    navController.navigate(Screen.TupperwareSwipeScreen.name)
                }
            )
        }
        composable(route = Screen.CreateEventScreen.name) {
            CreateEventScreen(
                onCancelClick = {
                    navController.navigate(
                        Screen.Event.name
                    )
                }
            )
        }
        // the uid of event is predefined on firestore. this is just for show.
        composable(
            route = ScreenWithArgs.DetailedEventScreen.name,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            DetailedEventScreen(backStackEntry.arguments?.getString("eventId").orEmpty())
        }
        composable(route = Screen.SignUpScreen.name) {
            SignUpScreen(
                onSuccessfulSignUp = { navController.navigate(Screen.SignUpUserInfos.name) },
                viewModel = singUpViewModel,
            )
        }
        composable(route = Screen.SignUpUserInfos.name) {
            AddProfileInfoScreen(
                viewModel = singUpViewModel,
                onSuccessfulSignUp = {
                    navController.navigate(
                        startScreen
                    )
                },
                onSignUpFailure = { navController.navigate(Screen.SignUpScreen.name) }
            )
        }
        composable(route = Screen.CreateRecipeScreen.name) {
            CreateRecipeScreen(
                onSuccessfulSubmit = { navController.navigateUp() },
                onCancelClick = { navController.navigateUp() }
            )
        }
        composable(route = Screen.PostDetails.name) {
            val post = Post(1, "Tiramisu", "This is a delicious triamisu or so")
            PostDetails(data = post, painter = painterResource(R.drawable.tiramisu))
        }
        composable(route = Screen.RecipeFeed.name) {
            RecipeFeed(
                onCreateNewRecipe = { navController.navigate(Screen.CreateRecipeScreen.name) }
            )
        }
        composable(route = Screen.Login.name) {
            LoginScreen(
                onSuccessfulLogin = {
                    isAuthenticated.value = true
                    navController.navigate(startScreen) {
                        // This popUp blocks the user being able to go back once logged in
                        popUpTo(Screen.Login.name) { inclusive = true }
                    }
                }
            )
        }
        composable(route = Screen.ChatScreen.name) {
            ChannelScreen(
                onBackListener = { navController.navigate(Screen.RecipeFeed.name) },
            )
        }
    }
    if (isAuthenticated.value) {
        Scaffold(
            bottomBar = {
                if (shouldShowBottomBar) {
                    BottomNavigationBar(navController)
                }
            }
        ) { scaffoldPadding ->
            NavHost(
                navController = navController,
                graph = navGraph,
                modifier = Modifier.padding(scaffoldPadding)
            )
        }
    } else {
        NavHost(navController = navController, graph = navGraph)
    }
}
