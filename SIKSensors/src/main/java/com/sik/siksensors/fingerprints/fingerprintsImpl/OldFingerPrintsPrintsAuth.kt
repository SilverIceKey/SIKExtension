package com.sik.siksensors.fingerprints.fingerprintsImpl

import android.Manifest
import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.CancellationSignal
import android.util.Log
import com.sik.sikandroid.permission.PermissionUtils
import com.sik.sikcore.SIKCore
import com.sik.siksensors.SensorErrorEnum
import com.sik.siksensors.fingerprints.IFingerPrintsAuth

/**
 * 旧指纹认证
 */
@SuppressLint("MissingPermission")
class OldFingerPrintsPrintsAuth : IFingerPrintsAuth {

    /**
     * 指纹管理器-旧
     */
    private val fingerprintManager by lazy {
        SIKCore.getApplication()
            .getSystemService(Context.FINGERPRINT_SERVICE) as? FingerprintManager
    }

    /**
     * 锁屏管理器
     */
    private val keyguardManager by lazy {
        SIKCore.getApplication().getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
    }

    private lateinit var cancellationSignal: CancellationSignal
    override fun authenticateFingerprint(auth: (SensorErrorEnum) -> Unit) {
        if (fingerprintManager?.isHardwareDetected == true) {
            checkPermission(auth)
        } else {
            auth(SensorErrorEnum.NO_HARDWARE)
        }
    }

    /**
     * 检查权限
     */
    private fun checkPermission(auth: (SensorErrorEnum) -> Unit) {
        PermissionUtils.checkAndRequestPermissions(arrayOf(Manifest.permission.USE_FINGERPRINT)) {
            if (it) {
                if (fingerprintManager?.hasEnrolledFingerprints() == false) {
                    auth(SensorErrorEnum.NO_ENROLLED_FINGERPRINTS)
                } else {
                    if (keyguardManager?.isKeyguardSecure == true) {
                        authenticateFingerprintOld(auth)
                    } else {
                        auth(SensorErrorEnum.NO_KEYGUARD_SECURE)
                    }
                }
            } else {
                auth(SensorErrorEnum.NO_PERMISSION)
            }
        }
    }

    /**
     * 认证指纹-旧
     */
    private fun authenticateFingerprintOld(auth: (SensorErrorEnum) -> Unit) {
        cancellationSignal = CancellationSignal()
        fingerprintManager?.authenticate(
            null, cancellationSignal, 0, object : FingerprintManager.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Log.i(
                        "OldFingerPrintsPrintsAuth",
                        "onAuthenticationError:${errorCode},${errString}"
                    )
                }

                override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
                    Log.i(
                        "OldFingerPrintsPrintsAuth",
                        "onAuthenticationError:${helpCode},${helpString}"
                    )

                }

                override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
                    auth(SensorErrorEnum.AUTHENTICATION_SUCCESS)
                }

                override fun onAuthenticationFailed() {
                    auth(SensorErrorEnum.AUTHENTICATION_FAILED)
                }
            }, null
        )
    }
}