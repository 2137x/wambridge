package io.github.trvny.wambridge.mobile

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RadioUiRoutingContractTest {
    private fun source(name: String): String =
        File("src/main/java/io/github/trvny/wambridge/mobile/$name").readText()

    @Test
    fun radioDeepLinksAllOpenTheMainRadioTab() {
        val service = source("RadioService.kt")
        val media = source("RadioMediaSession.kt")
        val widget = source("WamBridgeWidget.kt")

        for (text in listOf(service, media, widget)) {
            assertTrue(text.contains("MainNavigation.intent"))
            assertTrue(text.contains("MainDestination.RADIO"))
            assertFalse(text.contains("RadioStationsActivity::class.java"))
        }
    }

    @Test
    fun idlePlayRoutesToRadioInsteadOfLegacyTuneInScreen() {
        val controls = source("SpeakerControls.kt")
        val main = source("MainActivity.kt")
        val widget = source("WamBridgeWidget.kt")

        assertTrue(controls.contains("Destination.RADIO"))
        assertFalse(controls.contains("Destination.TUNEIN"))
        assertTrue(main.contains("SpeakerControls.Destination.RADIO"))
        assertTrue(main.contains("showDestination(MainDestination.RADIO)"))
        assertTrue(widget.contains("SpeakerControls.Destination.RADIO"))
    }

    @Test
    fun stationManagerIsNotASecondPlaybackScreen() {
        val manager = source("RadioStationsActivity.kt")
        val createStart = manager.indexOf("override fun onCreate")
        val resultStart = manager.indexOf("override fun onActivityResult", createStart)
        val ui = manager.substring(createStart, resultStart)

        assertTrue(ui.contains("\"Station manager\""))
        assertTrue(ui.contains("Playback lives in the Radio tab"))
        assertFalse(ui.contains("sectionTitle(this, \"Playback\")"))
        assertFalse(ui.contains("\"Play default\""))
        assertFalse(ui.contains("\"Play last\""))
        assertFalse(ui.contains("\"Stop radio\""))
    }
}
