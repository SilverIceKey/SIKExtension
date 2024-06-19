package com.sik.siksensors.fingerImpl

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.annotation.RequiresApi
import com.sik.sikcore.SIKCore
import com.sik.sikcore.permission.PermissionUtils
import com.sik.siksensors.IFingerAuth


/**
 * 新指纹认证
 */
@RequiresApi(Build.VERSION_CODES.Q)
class NewFingerAuth:IFingerAuth {

    /**
     * 指纹管理器-新
     */
    private val biometricManager by lazy {
        SIKCore.getApplication().getSystemService(Context.BIOMETRIC_SERVICE) as? BiometricManager
    }

    /**
     * 锁屏管理器
     */
    private val keyguardManager by lazy {
        SIKCore.getApplication().getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
    }

    private val biometricPrompt: BiometricPrompt? = null
    private val promptInfo: BiometricPrompt.Builder? = null

    @RequiresApi(Build.VERSION_CODES.R)
    override fun authenticateFingerprint(auth: (Boolean) -> Unit) {
        when(biometricManager?.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)){

        }
        if (biometricManager?.isHardwareDetected == true) {
            PermissionUtils.checkAndRequestPermissions(arrayOf(Manifest.permission.USE_FINGERPRINT)) {
                if (it) {
                    if (fingerprintManager?.hasEnrolledFingerprints() == true) {
                        auth(false)
                    } else {
                        if (keyguardManager?.isKeyguardSecure == true) {
                            authenticateFingerprintOld(matchCallback)
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
}