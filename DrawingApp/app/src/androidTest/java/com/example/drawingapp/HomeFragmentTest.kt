package com.example.drawingapp

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class HomeFragmentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysCorrectInitialState() {
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

        composeTestRule.waitUntil(3000) {
            composeTestRule
                .onAllNodesWithText("My Drawings")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("My Drawings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start New Drawing").assertIsDisplayed()
        composeTestRule.onNodeWithText("No drawings found.").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysDrawings_whenProvided() {
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

        composeTestRule.waitUntil(3000) {
            composeTestRule
                .onAllNodesWithText("My Drawings")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("My Drawings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start New Drawing").assertIsDisplayed()
        composeTestRule.onNodeWithText("Saved Drawings:").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Drawing 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Drawing 2").assertIsDisplayed()
    }

    @Test
    fun startDrawingButton_triggersCallback() {
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

        composeTestRule.waitUntil(3000) {
            composeTestRule
                .onAllNodesWithText("Start New Drawing")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Start New Drawing").performClick()
        assertTrue(buttonClicked)
    }
}