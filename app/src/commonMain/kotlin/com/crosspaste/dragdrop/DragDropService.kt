package com.crosspaste.dragdrop

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.PointerInputChange

interface DragDropService {
    fun startDrag(
        position: PointerInputChange,
        data: DragData,
        provider: DragDropProvider,
    )

    fun registerDropTarget(target: DropTarget)

    fun unregisterDropTarget(target: DropTarget)
}

data class DragData(
    val pasteDataId: Long,
    val type: DragDataType,
)

enum class DragDataType {
    CLIPBOARD,
    FILE,
    TEXT,
    IMAGE,
}

interface DragDropProvider {
    fun getDragImage(): ImageBitmap?

    fun getDragData(): DragData
}

interface DropTarget {
    fun onDragEnter(event: DragDropEvent): DragDropAction

    fun onDragOver(event: DragDropEvent): DragDropAction

    fun onDragExit(event: DragDropEvent)

    fun onDrop(event: DragDropEvent): DragDropAction
}

data class DragDropEvent(
    val position: java.awt.Point,
    val data: DragData,
    val action: DragDropAction,
)

enum class DragDropAction {
    COPY,
    MOVE,
    LINK,
    NONE,
}
