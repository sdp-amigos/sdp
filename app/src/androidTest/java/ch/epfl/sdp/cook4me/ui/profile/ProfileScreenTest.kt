package ch.epfl.sdp.cook4me.ui.profile

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfl.sdp.cook4me.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var auth: FirebaseAuth
    private lateinit var context: Context
    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        Firebase.auth.useEmulator("10.0.2.2", 9099)
        auth = FirebaseAuth.getInstance()
        runBlocking {
            auth.createUserWithEmailAndPassword("harry.potter@epfl.ch", "123456").await()
            auth.signInWithEmailAndPassword("harry.potter@epfl.ch", "123456").await()
        }
    }

    @After
    fun cleanUp() {
        runBlocking {
            auth.signInWithEmailAndPassword("harry.potter@epfl.ch", "123456").await()
            auth.currentUser?.delete()
        }
    }
    @Test
    fun testChagedProfileDataAreDisplayed() {
        val profileCreationViewModel = ProfileCreationViewModel()

        val imageTag = composeTestRule.activity.getString(R.string.tag_defaultProfileImage)
        val bioText = "Hello chicas e chicos"
        val favDishText = "empanadas"
        val usernameText = "Emanuel"
        val allergiesText = "nada"
        val userImage = "".toUri()

        profileCreationViewModel.addBio(bioText)
        profileCreationViewModel.addUsername(favDishText)
        profileCreationViewModel.addAllergies(usernameText)
        profileCreationViewModel.addFavoriteDish(allergiesText)
        profileCreationViewModel.addUserImage(userImage)
        profileCreationViewModel.onSubmit()

        composeTestRule.setContent {
            ProfileScreen(profileCreationViewModel = profileCreationViewModel)
        }

        composeTestRule.onNodeWithText(bioText).assertIsDisplayed()
        composeTestRule.onNodeWithText(favDishText).assertIsDisplayed()
        composeTestRule.onNodeWithText(usernameText).assertIsDisplayed()
        composeTestRule.onNodeWithText(allergiesText).assertIsDisplayed()
        composeTestRule.onNodeWithTag(imageTag).assertIsDisplayed()
    }
}
