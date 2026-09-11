package com.crosspaste.ui.floating

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import java.awt.GraphicsEnvironment
import java.awt.Toolkit

object FloatingIconPosition {
    fun getIconPosition(iconSizeDp: Dp, paddingDp: Dp): IntOffset {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val configuration = ge.defaultScreenDevice.defaultConfiguration
        val bounds = configuration.bounds
        val insets = Toolkit.getDefaultToolkit().getScreenInsets(configuration)
        
        val usableWidth = bounds.width - insets.left - insets.right
        val usableHeight = bounds.height - insets.top - insets.bottom
        
        val iconSizePx = iconSizeDp.toPx()
        val paddingPx = paddingDp.toPx()
        
        val x = (usableWidth - iconSizePx - paddingPx).toInt()
        val y = (usableHeight - iconSizePx - paddingPx).toInt()
        
        return IntOffset(x, y)
    }
    
    private fun Dp.toPx(): Float {
        val metrics = java.awt.Toolkit.getDefaultToolkit().screenResolution
        return this.value * metrics * 0.75f
    }
}
