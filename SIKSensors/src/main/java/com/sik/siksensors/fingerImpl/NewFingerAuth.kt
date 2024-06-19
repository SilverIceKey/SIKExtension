package com.sik.siksensors.fingerImpl

import android.Manifest
import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import com.sik.sikcore.SIKCore
import com.sik.sikcore.activity.ActivityTracker
import com.sik.sikcore.extension.toJson
import com.sik.sikcore.log.LogUtils
import com.sik.sikcore.permission.PermissionUtils
import com.sik.sikcore.thread.ThreadUtils
import com.sik.siksensors.FingerConfig
import com.sik.siksensors.FingerException
import com.sik.siksensors.IFingerAuth
import java.util.concurrent.Executor


/**
 * 新指纹认证
 */
@RequiresApi(Build.VERSION_CODES.Q)
class NewFingerAuth<T : FingerConfig>(private val fingerConfig: T) : IFingerAuth {
    private val logger = LogUtils.getLogger(NewFingerAuth::class)

    /**
     * 指纹管理器-新
     */
    private val biometricManager by lazy {
        SIKCore.getApplication().getSystemService(Context.BIOMETRIC_SERVICE) as? BiometricManager
    }

    private var biometricPrompt: BiometricPrompt? = null

    private val mainExecutor = Executor {
        ThreadUtils.mainHandler().post(it)
    }

    private lateinit var cancellationSignal: CancellationSignal

    @RequiresApi(Build.VERSION_CODES.R)
    override fun authenticateFingerprint(auth: (Boolean) -> Unit) {
        checkFingerConfig()
        when (biometricManager?.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                PermissionUtils.checkAndRequestPermissions(arrayOf(Manifest.permission.USE_BIOMETRIC)) {
                    if (it) {
                        biometricPrompt =
                            BiometricPrompt.Builder(ActivityTracker.getCurrentActivity())
                                .setTitle(fingerConfig.title)
                                .setDescription(fingerConfig.description)
                                .setNegativeButton(
                                    fingerConfig.negativeButtonTxt,
                                    mainExecutor,
                                    fingerConfig.listener
                                )
                                .build()
                        cancellationSignal = CancellationSignal()
                        biometricPrompt?.authenticate(
                            cancellationSignal,
                            mainExecutor,
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationError(
                                    errorCode: Int,
                                    errString: CharSequence?
                                ) {
                                    super.onAuthenticationError(errorCode, errString)
                                    logger.i("onAuthenticationError:${errorCode},${errString}")
                                }

                                override fun onAuthenticationHelp(
                                    helpCode: Int,
                                    helpString: CharSequence?
                                ) {
                                    super.onAuthenticationHelp(helpCode, helpString)
                                    logger.i("onAuthenticationHelp:${helpCode},${helpString}")
                                }

                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                                    super.onAuthenticationSucceeded(result)
                                    logger.i("onAuthenticationSucceeded:${result.toJson()}")
                                    auth(true)
                                }

                                override fun onAuthenticationFailed() {
                                    super.onAuthenticationFailed()
                                    logger.i("onAuthenticationFailed:验证失败")
                                    auth(false)
                                }
                            })
                    } else {
                        auth(false)
                    }
                }
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                auth(false)
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                auth(false)
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                auth(false)
            }
        }
    }

    private fun checkFingerConfig() {
        if (fingerConfig.title.isEmpty()) {
            throw FingerException("标题不能为空")
        }
        if (fingerConfig.negativeButtonTxt.isEmpty()) {
            throw FingerException("取消按钮文本不能为空")
        }
    }
}