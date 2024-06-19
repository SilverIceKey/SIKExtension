package com.sik.siksensors.fingerImpl

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import com.sik.sikcore.SIKCore
import com.sik.sikcore.permission.PermissionUtils
import com.sik.siksensors.IFingerAuth

/**
 * 旧指纹认证
 */
class OldFingerAuth:IFingerAuth {
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
    override fun authenticateFingerprint(auth: (Boolean) -> Unit) {
        if (fingerprintManager?.isHardwareDetected == true) {
            PermissionUtils.checkAndRequestPermissions(arrayOf(Manifest.permission.USE_FINGERPRINT)) {
                if (it) {
                    if (fingerprintManager?.hasEnrolledFingerprints() == true) {
                        auth(false)
                    } else {
                        if (keyguardManager?.isKeyguardSecure == true) {
                            authenticateFingerprintOld(auth)
                        } else {
                            auth(false)
                        }
                    }
                } else {
                    auth(false)
                }
            }
        } else {
            auth(false)
        }
    }

    /**
     * 认证指纹-旧
     */
    private fun authenticateFingerprintOld(matchCallback: (Boolean) -> Unit) {
        cancellationSignal = CancellationSignal()
        fingerprintManager!!.authenticate(
            null,
            cancellationSignal,
            0,
            object : FingerprintManager.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    matchCallback(false)
                }

                override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
                    matchCallback(false)
                }

                override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
                    matchCallback(true)
                }

                override fun onAuthenticationFailed() {
                    matchCallback(false)
                }
            },
            null
        )
    }
}