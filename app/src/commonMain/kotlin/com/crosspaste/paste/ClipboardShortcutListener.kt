package com.crosspaste.paste

import com.crosspaste.utils.getPlatformUtils
import java.awt.event.KeyEvent

class ClipboardShortcutListener(
    private val clipboardListener: ClipboardListener,
) {
    fun onKeyEvent(keyEvent: KeyEvent): Boolean {
        val platform = getPlatformUtils().platform
        val keyCode = keyEvent.keyCode
        val isCtrlPressed = keyEvent.isControlDown
        val isCmdPressed = keyEvent.isMetaDown

        val isPaste =
            when {
                platform.isMacos() -> isCmdPressed && keyCode == KeyEvent.VK_V
                else -> isCtrlPressed && keyCode == KeyEvent.VK_V
            }

        val isCopy =
            when {
                platform.isMacos() -> isCmdPressed && keyCode == KeyEvent.VK_C
                else -> isCtrlPressed && keyCode == KeyEvent.VK_C
            }

        val isCut =
            when {
                platform.isMacos() -> isCmdPressed && keyCode == KeyEvent.VK_X
                else -> isCtrlPressed && keyCode == KeyEvent.VK_X
            }

        return when {
            isPaste -> {
                clipboardListener.onPaste()
                true
            }
            isCopy -> {
                clipboardListener.onCopy()
                true
            }
            isCut -> {
                clipboardListener.onCut()
                true
            }
            else -> false
        }
    }
}
