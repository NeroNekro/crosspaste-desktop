package com.crosspaste.paste

interface ClipboardListener {
    fun onPaste()

    fun onCopy()

    fun onCut()
}
