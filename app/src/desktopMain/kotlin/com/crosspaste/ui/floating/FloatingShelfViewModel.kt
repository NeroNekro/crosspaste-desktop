package com.crosspaste.ui.floating

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.crosspaste.app.DesktopAppWindowManager
import com.crosspaste.config.DesktopConfigManager
import com.crosspaste.dragdrop.DragData
import com.crosspaste.dragdrop.DragDataType
import com.crosspaste.dragdrop.DragDropAction
import com.crosspaste.dragdrop.DragDropEvent
import com.crosspaste.dragdrop.DragDropProviderFactory
import com.crosspaste.dragdrop.DragDropService
import com.crosspaste.dragdrop.DropTarget
import com.crosspaste.paste.ClipboardListener
import com.crosspaste.paste.ClipboardShortcutListener
import com.crosspaste.paste.PasteboardService
import com.crosspaste.paste.item.CreatePasteItemHelper
import com.crosspaste.utils.ioDispatcher
import com.crosspaste.utils.namedScope
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.awt.datatransfer.DataFlavor
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

class FloatingShelfViewModel(
    private val appWindowManager: DesktopAppWindowManager,
    private val configManager: DesktopConfigManager,
    private val pasteboardService: PasteboardService,
    private val dragDropProviderFactory: DragDropProviderFactory,
    private val clipboardShortcutListener: ClipboardShortcutListener,
) : ClipboardListener,
    DropTarget {

    companion object {
        private const val AUTO_PASTE_DELAY_MS = 300L
    }

    private val logger: KLogger = KotlinLogging.logger {}
    private val scope = namedScope(ioDispatcher, "FloatingShelfViewModel")

    private val _dragData = MutableStateFlow<DragData?>(null)
    val dragData: StateFlow<DragData?> = _dragData.asStateFlow()

    private val _dropTargetRegistered = MutableStateFlow(false)
    val dropTargetRegistered: StateFlow<Boolean> = _dropTargetRegistered.asStateFlow()

    private var autoPasteJob: kotlinx.coroutines.Job? = null
    private var dragDropService: DragDropService? = null

    init {
        val platform =
            com.crosspaste.utils
                .getPlatformUtils()
                .platform
        dragDropService = dragDropProviderFactory.createDragDropService(platform)
        dragDropService?.registerDropTarget(this)
    }

    override fun onPaste() {
        logger.info { "Paste shortcut detected" }
        val clipboardData = readClipboardData()
        if (clipboardData != null) {
            addClipboardDataToShelf(clipboardData)
        }
    }

    override fun onCopy() {
        logger.info { "Copy shortcut detected" }
    }

    override fun onCut() {
        logger.info { "Cut shortcut detected" }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun readClipboardData(): DragData? {
        val toolkit = java.awt.Toolkit.getDefaultToolkit()
        val clipboard = toolkit.systemClipboard

        return try {
            val transferable = clipboard.getContents(null)
            if (transferable?.isDataFlavorSupported(DataFlavor.stringFlavor) == true) {
                val text = transferable.getTransferData(DataFlavor.stringFlavor) as String
                DragData(
                    pasteDataId = 0L,
                    type = DragDataType.TEXT,
                )
            } else if (transferable?.isDataFlavorSupported(DataFlavor.javaFileListFlavor) == true) {
                val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
                DragData(
                    pasteDataId = 0L,
                    type = DragDataType.FILE,
                )
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to read clipboard data" }
            null
        }
    }

    fun addClipboardDataToShelf(data: DragData) {
        autoPasteJob?.cancel()

        autoPasteJob =
            scope.launch {
                delay(AUTO_PASTE_DELAY_MS.milliseconds)
                // Auto-paste after delay
                pasteboardService.tryWritePasteboard(
                    pasteItem =
                        when (data.type) {
                            DragDataType.TEXT ->
                                CreatePasteItemHelper.createTextPasteItem(
                                    text = "Clipboard content",
                                )
                            DragDataType.FILE -> {
                                val files = readClipboardFiles()
                                CreatePasteItemHelper.createFilesPasteItem(
                                    relativePathList = files,
                                    fileInfoTreeMap = emptyMap(),
                                )
                            }
                            DragDataType.IMAGE -> null
                            else -> null
                        } ?: return@launch,
                )
            }
    }

    fun handleDrop(data: DragData) {
        autoPasteJob?.cancel()
        autoPasteJob =
            scope.launch {
                delay(AUTO_PASTE_DELAY_MS.milliseconds)
                // Paste dropped content
            }
    }

    fun showFloatingShelf() {
        appWindowManager.showFloatingShelf()
    }

    fun hideFloatingShelf() {
        appWindowManager.hideFloatingShelf()
    }

    fun updateFloatingShelfPosition(position: FloatingShelfPosition) {
        appWindowManager.updateFloatingShelfPosition(position)
    }

    override fun onDragEnter(event: DragDropEvent): DragDropAction {
        logger.info { "Drag enter detected" }
        _dragData.value = event.data
        return DragDropAction.COPY
    }

    override fun onDragOver(event: DragDropEvent): DragDropAction = DragDropAction.COPY

    override fun onDragExit(event: DragDropEvent) {
        logger.info { "Drag exit detected" }
        _dragData.value = null
    }

    override fun onDrop(event: DragDropEvent): DragDropAction {
        logger.info { "Drop detected" }
        handleDrop(event.data)
        return DragDropAction.COPY
    }

    private fun readClipboardFiles(): List<String> {
        val toolkit = java.awt.Toolkit.getDefaultToolkit()
        val clipboard = toolkit.systemClipboard

        return try {
            val transferable = clipboard.getContents(null)
            if (transferable?.isDataFlavorSupported(DataFlavor.javaFileListFlavor) == true) {
                val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>?
                files?.map { (it as File).absolutePath } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to read clipboard files" }
            emptyList()
        }
    }
}
