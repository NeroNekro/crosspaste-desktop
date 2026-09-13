package com.crosspaste.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Dock
import com.crosspaste.config.DesktopConfigManager
import com.crosspaste.platform.Platform
import com.crosspaste.ui.LocalThemeExtState
import com.crosspaste.ui.base.IconData
import com.crosspaste.ui.base.SectionHeader
import com.crosspaste.ui.theme.AppUISize.medium
import com.crosspaste.ui.theme.AppUISize.tiny
import com.crosspaste.ui.theme.AppUISize.xxxxLarge
import org.koin.compose.koinInject

@Composable
fun AppearanceSettingsContentView() {
    val configManager = koinInject<DesktopConfigManager>()
    val platform = koinInject<Platform>()
    val themeExt = LocalThemeExtState.current
    val config by configManager.config.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(tiny),
    ) {
        item {
            SettingSectionCard {
                ThemeSettingItem()
                HorizontalDivider(modifier = Modifier.padding(start = xxxxLarge))
                FontSettingItemView()
            }
        }

        if (platform.isMacos()) {
            item {
                SectionHeader("dock", topPadding = medium)
            }

            item {
                SettingSectionCard {
                    SettingListSwitchItem(
                        title = "show_dock_icon",
                        subtitle = "show_dock_icon_description",
                        icon = IconData(MaterialSymbols.Rounded.Dock, themeExt.cyanIconColor),
                        checked = config.showDockIcon,
                        onCheckedChange = { showDockIcon ->
                            configManager.updateConfig("showDockIcon", showDockIcon)
                        },
                    )
                }
            }
        }
    }
}
