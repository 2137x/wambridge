package io.github.trvny.wambridge.mobile

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidUxContractTest {
    @Test
    fun manifestDeclaresNotificationPermissionAndBothWidgets() {
        val manifest = File("src/main/AndroidManifest.xml").readText()

        assertTrue(manifest.contains("android.permission.POST_NOTIFICATIONS"))
        assertTrue(manifest.contains(".WamBridgeWidget"))
        assertTrue(manifest.contains(".WamBridgeControlsWidget"))
        assertTrue(manifest.contains("@xml/wam_bridge_controls_widget_info"))
    }

    @Test
    fun remoteWidgetStartsWithTheControlsLayout() {
        val info = File("src/main/res/xml/wam_bridge_controls_widget_info.xml").readText()

        assertTrue(info.contains("@layout/widget_wam_bridge_controls"))
    }

    @Test
    fun expandedWidgetShowsCachedNowPlayingArtwork() {
        val layout = File("src/main/res/layout/widget_wam_bridge_controls.xml").readText()
        val widget = File(
            "src/main/java/io/github/trvny/wambridge/mobile/WamBridgeWidget.kt",
        ).readText()

        assertTrue(layout.contains("widget_artwork"))
        assertTrue(widget.contains("ArtworkLoader.cached(snapshot.artworkUrl)"))
        assertTrue(widget.contains("setImageViewBitmap(R.id.widget_artwork"))
    }

    @Test
    fun artworkLoaderReusesSharedCacheBeforeSameUrlShortCircuit() {
        val loader = File(
            "src/main/java/io/github/trvny/wambridge/mobile/ArtworkLoader.kt",
        ).readText()

        val cacheHit = loader.indexOf("synchronized(cache) { cache.get(key) }?.let")
        val sameUrlGuard = loader.indexOf("if (key != null && view.tag == key) return")
        assertTrue(cacheHit >= 0)
        assertTrue(sameUrlGuard > cacheHit)
        assertTrue(loader.contains("if (view.tag == key) view.tag = null"))
    }

    @Test
    fun widgetAndTileStartOnlyTheRendererAndDoNotOwnDiscovery() {
        val widget = File(
            "src/main/java/io/github/trvny/wambridge/mobile/WamBridgeWidget.kt",
        ).readText()
        val tile = File(
            "src/main/java/io/github/trvny/wambridge/mobile/WamBridgeTileService.kt",
        ).readText()

        for (source in listOf(widget, tile)) {
            assertTrue(source.contains("RendererService.ACTION_START"))
            assertTrue(source.contains("SpeakerStateStore.current()"))
            assertTrue(!source.contains("WamDiscovery.discover("))
            assertTrue(!source.contains("SpeakerTarget.resolve"))
        }
    }

    @Test
    fun widgetSettingsRouteUsesTheSettingsDestination() {
        val widget = File(
            "src/main/java/io/github/trvny/wambridge/mobile/WamBridgeWidget.kt",
        ).readText()

        assertTrue(
            widget.contains(
                "MainNavigation.intent(context, MainDestination.SETTINGS)",
            ),
        )
    }
}
