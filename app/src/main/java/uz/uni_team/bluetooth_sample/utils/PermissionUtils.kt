package uz.uni_team.bluetooth_sample.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener

fun Context.isPermissionEnabled(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Context.isPermissionsEnabled(permissions: List<String>): Boolean {
    return permissions.all { isPermissionEnabled(it) }
}

fun Context.checkPermission(permission: String, onGranted: () -> Unit, onDenied: () -> Unit) {
    Dexter.withContext(this).withPermission(permission).withListener(
        object : PermissionListener {
            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                onGranted.invoke()
            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                onDenied.invoke()
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?, p1: PermissionToken?
            ) {
                p1?.continuePermissionRequest()
            }
        },
    ).check()
}

fun Context.checkPermissions(
    permission: List<String>,
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    Dexter.withContext(this).withPermissions(permission).withListener(
            object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if (p0?.areAllPermissionsGranted() == true) {
                        onGranted.invoke()
                    } else {
                        onDenied.invoke()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?, p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

            },
        ).check()
}