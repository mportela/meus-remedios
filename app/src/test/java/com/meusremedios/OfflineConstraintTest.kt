package com.meusremedios

import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifica que o manifest mergeado da aplicação NÃO declara a permissão
 * android.permission.INTERNET — restrição inegociável do app (100% offline).
 *
 * Se este teste falhar, significa que alguma dependência ou mudança adicionou
 * a permissão de rede acidentalmente.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class OfflineConstraintTest {

    @Test
    fun manifest_doesNotDeclareInternetPermission() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS,
        )
        val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()

        assertFalse(
            "INTERNET permission was found in the manifest — the app MUST remain 100% offline.\n" +
                "Declared permissions: $permissions",
            permissions.contains("android.permission.INTERNET"),
        )
    }
}
