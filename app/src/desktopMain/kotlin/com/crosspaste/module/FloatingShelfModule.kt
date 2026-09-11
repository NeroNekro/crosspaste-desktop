package com.crosspaste.module

import com.crosspaste.ui.floating.FloatingShelfConfig
import org.koin.core.module.Module
import org.koin.dsl.module

fun floatingShelfModule(): Module =
    module {
        factory<FloatingShelfConfig> {
            // Config loading moved to DesktopUiModule
            FloatingShelfConfig()
        }
    }
