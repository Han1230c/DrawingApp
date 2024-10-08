package com.example.drawingapp

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.drawingapp.data.Drawing
import com.example.drawingapp.ui.components.HomeScreen
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class HomeFragmentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysCorrectInitialState() {
        // Test that the home screen displays the correct initial state with no drawings
        composeTestRule.setContent {
            MaterialTheme {
                HomeScreen(
                    onStartDrawingClick = {},
                    drawings = emptyList(),
                    onDrawingClick = {},
                    onDeleteClick = {}
                )
            }
        }

        // Wait until the "My Drawings" text appears to ensure the screen has loaded
        composeTestRule.waitUntil(3000) {
            composeTestRule
                .onAllNodesWithText("My Drawings")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Check if the important UI elements are displayed correctly
        composeTestRule.onNodeWithText("My Drawings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start New Drawing").assertIsDisplayed()
        composeTestRule.onNodeWithText("No drawings found.").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysDrawings_whenProvided() {
        // Test that the home screen displays a list of drawings when provided
        val testDrawings = listOf(
            Drawing(1, "Test Drawing 1", "", ""),
            Drawing(2, "Test Drawing 2", "", "")
        )

        composeTestRule.setContent {
            MaterialTheme {
                HomeScreen(
                    onStartDrawingClick = {},
                    drawings = testDrawings,
                    onDrawingClick = {},
                    onDeleteClick = {}
                )
            }
        }

        // Wait until the "My Drawings" text appears to ensure the screen has loaded
        composeTestRule.waitUntil(3000) {
            composeTestRule
                .onAllNodesWithText("My Drawings")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Check if the important UI elements and the test drawings are displayed correctly
        composeTestRule.onNodeWithText("My Drawings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start New Drawing").assertIsDisplayed()
        composeTestRule.onNodeWithText("Saved Drawings:").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Drawing 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Drawing 2").assertIsDisplayed()
    }

    @Test
    fun startDrawingButton_triggersCallback() {
        // Test that clicking the "Start New Drawing" button triggers the callback
        var buttonClicked = false
        composeTestRule.setContent {
            MaterialTheme {
                HomeScreen(
                    onStartDrawingClick = { buttonClicked = true },
                    drawings = emptyList(),
                    onDrawingClick = {},
                    onDeleteClick = {}
                )
            }
        }

        // Wait until the "Start New Drawing" button appears to ensure the screen has loaded
        composeTestRule.waitUntil(3000) {
            composeTestRule
                .onAllNodesWithText("Start New Drawing")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Perform a click on the "Start New Drawing" button and verify the callback was triggered
        composeTestRule.onNodeWithText("Start New Drawing").performClick()
        assertTrue(buttonClicked)  // Assert that the button click updated the variable
    }
}