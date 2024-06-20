package com.sik.siksensors.fingerImpl

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.CancellationSignal
import com.sik.sikcore.SIKCore
import com.sik.sikcore.log.LogUtils
import com.sik.sikcore.permission.PermissionUtils
import com.sik.siksensors.FingerErrorEnum
import com.sik.siksensors.IFingerAuth

/**
 * 旧指纹认证
 */
class OldFingerAuth : IFingerAuth {
    private val logger = LogUtils.getLogger(OldFingerAuth::class)

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
    override fun authenticateFingerprint(auth: (FingerErrorEnum) -> Unit) {
        if (fingerprintManager?.isHardwareDetected == true) {
            checkPermission(auth)
        } else {
            auth(FingerErrorEnum.NO_HARDWARE)
        }
    }

    /**
     * 检查权限
     */
    private fun checkPermission(auth: (FingerErrorEnum) -> Unit) {
        PermissionUtils.checkAndRequestPermissions(arrayOf(Manifest.permission.USE_FINGERPRINT)) {
            if (it) {
                if (fingerprintManager?.hasEnrolledFingerprints() == false) {
                    auth(FingerErrorEnum.NO_ENROLLED_FINGERPRINTS)
                } else {
                    if (keyguardManager?.isKeyguardSecure == true) {
                        authenticateFingerprintOld(auth)
                    } else {
                        auth(FingerErrorEnum.NO_KEYGUARD_SECURE)
                    }
                }
            } else {
                auth(FingerErrorEnum.NO_PERMISSION)
            }
        }
    }

    /**
     * 认证指纹-旧
     */
    private fun authenticateFingerprintOld(auth: (FingerErrorEnum) -> Unit) {
        cancellationSignal = CancellationSignal()
        fingerprintManager?.authenticate(
            null,
            cancellationSignal,
            0,
            object : FingerprintManager.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    logger.i("onAuthenticationError:${errorCode},${errString}")
                }

                override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
                    logger.i("onAuthenticationError:${helpCode},${helpString}")

                }

                override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
                    auth(FingerErrorEnum.AUTHENTICATION_SUCCESS)
                }

                override fun onAuthenticationFailed() {
                    auth(FingerErrorEnum.AUTHENTICATION_FAILED)
                }
            },
            null
        )
    }
}