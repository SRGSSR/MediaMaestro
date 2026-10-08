/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import android.Manifest
import android.app.Application
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.CINNAMON_BUN])
class LocalNetworkPermissionTest {
    private lateinit var context: Application

    @BeforeTest
    fun before() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.BAKLAVA])
    fun `permission is not handled before Android 17`() {
        context.setupLocalNetworkPermission(targetSdk = Build.VERSION_CODES.BAKLAVA, declarePermission = true)

        assertNull(LocalNetworkPermission.permission)
        assertFalse(LocalNetworkPermission.isHandled(context))
        assertFalse(LocalNetworkPermission.isMissing(context))
    }

    @Test
    fun `permission is not handled when targeting Android 16`() {
        context.setupLocalNetworkPermission(targetSdk = Build.VERSION_CODES.BAKLAVA, declarePermission = true)

        assertEquals(Manifest.permission.ACCESS_LOCAL_NETWORK, LocalNetworkPermission.permission)
        assertFalse(LocalNetworkPermission.isHandled(context))
        assertFalse(LocalNetworkPermission.isMissing(context))
    }

    @Test
    fun `permission is not handled when not declared in the manifest`() {
        context.setupLocalNetworkPermission(targetSdk = Build.VERSION_CODES.CINNAMON_BUN, declarePermission = false)

        assertFalse(LocalNetworkPermission.isHandled(context))
        assertFalse(LocalNetworkPermission.isMissing(context))
    }

    @Test
    fun `permission is missing when declared but not granted`() {
        context.setupLocalNetworkPermission(targetSdk = Build.VERSION_CODES.CINNAMON_BUN, declarePermission = true)

        assertTrue(LocalNetworkPermission.isHandled(context))
        assertTrue(LocalNetworkPermission.isMissing(context))
    }

    @Test
    fun `permission is not missing when granted`() {
        context.setupLocalNetworkPermission(targetSdk = Build.VERSION_CODES.CINNAMON_BUN, declarePermission = true)
        shadowOf(context).grantPermissions(Manifest.permission.ACCESS_LOCAL_NETWORK)

        assertTrue(LocalNetworkPermission.isHandled(context))
        assertFalse(LocalNetworkPermission.isMissing(context))
    }

    @Test
    fun `settings intent opens the application details`() {
        val intent = LocalNetworkPermission.createSettingsIntent(context)

        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent.action)
        assertEquals("package:${context.packageName}", intent.dataString)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }
}

/**
 * Configure the application's target SDK, and whether it declares the local network permission in its manifest.
 */
internal fun Application.setupLocalNetworkPermission(targetSdk: Int, declarePermission: Boolean) {
    applicationInfo.targetSdkVersion = targetSdk

    shadowOf(packageManager).getInternalMutablePackageInfo(packageName).requestedPermissions =
        if (declarePermission) arrayOf("android.permission.ACCESS_LOCAL_NETWORK") else emptyArray()
}
