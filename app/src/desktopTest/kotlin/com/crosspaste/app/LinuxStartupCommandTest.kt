package com.crosspaste.app

import kotlin.test.Test
import kotlin.test.assertEquals

class LinuxStartupCommandTest {

    @Test
    fun `AppImage path wins over transient mounted executable`() {
        assertEquals(
            "/home/user/Applications/CrossPaste.AppImage",
            resolveLinuxStartupExecutable(
                appImagePath = "/home/user/Applications/CrossPaste.AppImage",
                installedExecutable = "/tmp/.mount_cross/bin/crosspaste",
            ),
        )
    }

    @Test
    fun `installed executable is used for Debian package`() {
        assertEquals(
            "/usr/lib/crosspaste/bin/crosspaste",
            resolveLinuxStartupExecutable(
                appImagePath = null,
                installedExecutable = "/usr/lib/crosspaste/bin/crosspaste",
            ),
        )
    }

    @Test
    fun `desktop Exec argument safely quotes spaces and metacharacters`() {
        assertEquals(
            "\"/home/A User/\\\$Cross\\\"Paste.AppImage\"",
            quoteDesktopExecArgument("/home/A User/\$Cross\"Paste.AppImage"),
        )
    }
}
