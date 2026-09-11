package com.crosspaste.module

import com.crosspaste.dragdrop.DragData
import com.crosspaste.dragdrop.DragDropProvider
import com.crosspaste.dragdrop.DragDropService
import com.crosspaste.dragdrop.DropTarget
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DragDropServiceMacos : DragDropService {

    private val logger: KLogger = KotlinLogging.logger {}

    init {
        // Load native library on macOS if needed
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun startDrag(
        position: androidx.compose.ui.input.pointer.PointerInputChange,
        data: DragData,
        provider: DragDropProvider,
    ) {
        val dragImage = provider.getDragImage()
        val dragData = provider.getDragData()

        runBlocking(Dispatchers.Default) {
            launch {
                try {
                    // Start drag with native macOS APIs via Swift
                    // This would call into MacAppUtils.startDrag()
                    logger.info { "Starting drag on macOS with data: $dragData" }
                } catch (e: Exception) {
                    logger.error(e) { "Failed to start drag on macOS" }
                }
            }
        }
    }

    override fun registerDropTarget(target: DropTarget) {
        // Register drop target with native macOS APIs
        logger.info { "Registered drop target on macOS" }
    }

    override fun unregisterDropTarget(target: DropTarget) {
        // Unregister drop target
        logger.info { "Unregistered drop target on macOS" }
    }
}

class DragDropServiceWindows : DragDropService {

    private val logger: KLogger = KotlinLogging.logger {}

    @OptIn(DelicateCoroutinesApi::class)
    override fun startDrag(
        position: androidx.compose.ui.input.pointer.PointerInputChange,
        data: DragData,
        provider: DragDropProvider,
    ) {
        val dragImage = provider.getDragImage()
        val dragData = provider.getDragData()

        runBlocking(Dispatchers.Default) {
            launch {
                try {
                    // Start drag with native Windows APIs via JNA
                    logger.info { "Starting drag on Windows with data: $dragData" }
                } catch (e: Exception) {
                    logger.error(e) { "Failed to start drag on Windows" }
                }
            }
        }
    }

    override fun registerDropTarget(target: DropTarget) {
        // Register drop target with native Windows APIs
        logger.info { "Registered drop target on Windows" }
    }

    override fun unregisterDropTarget(target: DropTarget) {
        // Unregister drop target
        logger.info { "Unregistered drop target on Windows" }
    }
}

class DragDropServiceLinux : DragDropService {

    private val logger: KLogger = KotlinLogging.logger {}

    @OptIn(DelicateCoroutinesApi::class)
    override fun startDrag(
        position: androidx.compose.ui.input.pointer.PointerInputChange,
        data: DragData,
        provider: DragDropProvider,
    ) {
        val dragImage = provider.getDragImage()
        val dragData = provider.getDragData()

        runBlocking(Dispatchers.Default) {
            launch {
                try {
                    // Start drag with native Linux X11 APIs
                    logger.info { "Starting drag on Linux with data: $dragData" }
                } catch (e: Exception) {
                    logger.error(e) { "Failed to start drag on Linux" }
                }
            }
        }
    }

    override fun registerDropTarget(target: DropTarget) {
        // Register drop target with native Linux X11 APIs
        logger.info { "Registered drop target on Linux" }
    }

    override fun unregisterDropTarget(target: DropTarget) {
        // Unregister drop target
        logger.info { "Unregistered drop target on Linux" }
    }
}
