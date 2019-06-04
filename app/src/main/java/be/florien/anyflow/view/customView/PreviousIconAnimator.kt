package be.florien.anyflow.view.customView

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import be.florien.anyflow.R


class PreviousIconAnimator(context: Context) : IconAnimator(context) {
    private var enabledColor = ContextCompat.getColor(context, R.color.toolbarItem)
    private var disabledColor = ContextCompat.getColor(context, R.color.disabled)

    override fun getStartAnimation(newState: Int): AnimatedVectorDrawableCompat? = null

    override fun getEndAnimation(newState: Int): AnimatedVectorDrawableCompat? = when (newState) {
        oldState -> null
        PreviousIconAnimator.STATE_PREVIOUS_NO_PREVIOUS -> getAnimatedIcon(R.drawable.ic_start_to_previous)
        PreviousIconAnimator.STATE_PREVIOUS_START -> getAnimatedIcon(R.drawable.ic_previous_to_start)
        else -> getAnimatedIcon(R.drawable.ic_start_to_previous)
    }

    override fun getFixedIcon(newState: Int): Drawable {
        iconColor = if (newState == PreviousIconAnimator.STATE_PREVIOUS_NO_PREVIOUS) {
            disabledColor
        } else {
            enabledColor
        }
        return when (newState) {
            PreviousIconAnimator.STATE_PREVIOUS_NO_PREVIOUS -> getIcon(R.drawable.ic_previous)
            PreviousIconAnimator.STATE_PREVIOUS_PREVIOUS -> getIcon(R.drawable.ic_previous)
            else -> getIcon(R.drawable.ic_start)
        }
    }

    companion object {
        const val STATE_PREVIOUS_NO_PREVIOUS = 0
        const val STATE_PREVIOUS_PREVIOUS = 1
        const val STATE_PREVIOUS_START = 2
    }
}