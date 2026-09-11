package com.crosspaste.dragdrop

import com.crosspaste.platform.Platform

interface DragDropProviderFactory {
    fun createDragDropService(platform: Platform): DragDropService
}
