package io.github.trvny.wambridge.mobile

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * One visual source of truth for playable entries shown by Radio and the legacy
 * TuneIn browser. Radio owns navigation; these helpers only render an entry.
 */
internal fun tuneInPresetCard(
    context: Context,
    preset: SamsungTuneIn.Preset,
    badge: String? = null,
    enabled: Boolean = true,
    onPlay: () -> Unit,
): View = radioEntryCard(
    context = context,
    title = preset.title,
    detail = listOfNotNull(
        preset.description?.takeIf(String::isNotBlank),
        preset.mediaId?.takeIf(String::isNotBlank),
    ).joinToString(" · "),
    artworkUrl = preset.thumbnail ?: tuneInArtworkUrl(preset.mediaId),
    badge = badge,
    enabled = enabled,
    onPlay = onPlay,
)

internal fun savedRadioStationCard(
    context: Context,
    station: MobileRadioStation,
    enabled: Boolean = true,
    onPlay: () -> Unit,
): View = radioEntryCard(
    context = context,
    title = station.alias,
    detail = radioStationSourceSummary(station),
    artworkUrl = tuneInArtworkUrl(station.tuneInId),
    enabled = enabled,
    onPlay = onPlay,
)

private fun radioEntryCard(
    context: Context,
    title: String,
    detail: String,
    artworkUrl: String?,
    badge: String? = null,
    enabled: Boolean,
    onPlay: () -> Unit,
): View {
    val row = MobileUi.row(context).apply {
        val padding = MobileUi.dp(context, 10)
        setPadding(padding, padding, padding, padding)
        background = MobileUi.rounded(
            context,
            fill = context.getColor(R.color.wam_surface),
            stroke = context.getColor(R.color.wam_border),
            radiusDp = 18,
        )
        isEnabled = enabled
        alpha = if (enabled) 1f else 0.55f
        setOnClickListener { if (enabled) onPlay() }
    }
    row.layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
    ).apply { bottomMargin = MobileUi.dp(context, 8) }

    val logo = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        setImageResource(R.mipmap.ic_launcher)
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = MobileUi.dp(context, 10).toFloat()
            setColor(Color.argb(14, 0, 0, 0))
        }
        clipToOutline = true
        contentDescription = "$title artwork"
    }
    row.addView(
        logo,
        LinearLayout.LayoutParams(
            MobileUi.dp(context, 58),
            MobileUi.dp(context, 58),
        ).apply { marginEnd = MobileUi.dp(context, 12) },
    )
    ArtworkLoader.load(context, logo, artworkUrl)

    val copy = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        badge?.let { value ->
            addView(TextView(context).apply {
                text = value
                textSize = 11f
                setTextColor(context.getColor(R.color.wam_accent))
                typeface = Typeface.DEFAULT_BOLD
                maxLines = 1
            })
        }
        addView(TextView(context).apply {
            text = title
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(context.getColor(R.color.wam_text))
            maxLines = 2
        })
        if (detail.isNotBlank()) {
            addView(TextView(context).apply {
                text = detail
                textSize = 12f
                setTextColor(context.getColor(R.color.wam_muted))
                maxLines = 2
            })
        }
    }
    row.addView(copy, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
    row.addView(
        MobileUi.button(context, "Play", MobileUi.ButtonKind.PRIMARY) {
            if (enabled) onPlay()
        }.apply { isEnabled = enabled },
    )
    return row
}
