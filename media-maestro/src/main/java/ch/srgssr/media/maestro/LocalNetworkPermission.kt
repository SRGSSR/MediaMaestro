/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

/**
 * Helper for the local network permission, required to discover Cast devices starting with Android 17.
 *
 * The permission is only handled if the application targets Android 17 or later, and declares
 * [Manifest.permission.ACCESS_LOCAL_NETWORK] in its manifest.
 *
 * @see <a href="https://developer.android.com/privacy-and-security/local-network-permission">Local network</a>
 */
internal object LocalNetworkPermission {
    /**
     * The local network permission, or `null` if it doesn't exist on this device.
     */
    val permission: String?
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
            Manifest.permission.ACCESS_LOCAL_NETWORK
        } else {
            null
        }

    /**
     * Check if the local network permission is handled for the application associated with [context].
     *
     * @param context The [Context] instance.
     * @return `true` if the device runs Android 17 or later, the application targets Android 17 or later, and the
     * application declares the local network permission in its manifest. `false` otherwise.
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.CINNAMON_BUN)
    fun isHandled(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN &&
            context.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.CINNAMON_BUN &&
            isDeclared(context, Manifest.permission.ACCESS_LOCAL_NETWORK)
    }

    /**
     * Check if the local network permission is missing for the application associated with [context].
     *
     * @param context The [Context] instance.
     * @return `true` if the permission is handled and not granted, `false` otherwise.
     *
     * @see isHandled
     */
    fun isMissing(context: Context): Boolean {
        if (!isHandled(context)) {
            return false
        }

        val permissionState = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_LOCAL_NETWORK)

        return permissionState != PackageManager.PERMISSION_GRANTED
    }

    /**
     * Create an [Intent] to open the settings screen of the application associated with [context], where the user can
     * grant the local network permission.
     *
     * @param context The [Context] instance.
     */
    fun createSettingsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    @RequiresApi(Build.VERSION_CODES.CINNAMON_BUN)
    private fun isDeclared(context: Context, permission: String): Boolean {
        val packageInfo = try {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()),
            )
        } catch (_: PackageManager.NameNotFoundException) {
            return false
        }

        return packageInfo.requestedPermissions?.contains(permission) == true
    }
}
