package com.sik.siksensors.fingerprints.fingerprintsImpl

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import com.sik.sikcore.SIKCore
import com.sik.sikandroid.activity.ActivityTracker
import com.sik.sikcore.extension.toJson
import com.sik.sikandroid.permission.PermissionUtils
import com.sik.sikcore.thread.ThreadUtils
import com.sik.siksensors.SensorErrorEnum
import com.sik.siksensors.fingerprints.FingerPrintsConfig
import com.sik.siksensors.fingerprints.FingerPrintsException
import com.sik.siksensors.fingerprints.IFingerPrintsAuth
import java.util.concurrent.Executor
import java.util.logging.Logger


/**
 * 新指纹认证
 */
@RequiresApi(Build.VERSION_CODES.Q)
class NewFingerPrintsPrintsAuth<T : FingerPrintsConfig>(private val fingerConfig: T) :
    IFingerPrintsAuth {
    private val logger = Logger.getLogger(NewFingerPrintsPrintsAuth::class.java.toString())

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

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun authenticateFingerprint(auth: (SensorErrorEnum) -> Unit) {
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
                                    logger.info("onAuthenticationError:${errorCode},${errString}")
                                }

                                override fun onAuthenticationHelp(
                                    helpCode: Int,
                                    helpString: CharSequence?
                                ) {
                                    super.onAuthenticationHelp(helpCode, helpString)
                                    logger.info("onAuthenticationHelp:${helpCode},${helpString}")
                                }

                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                                    super.onAuthenticationSucceeded(result)
                                    logger.info("onAuthenticationSucceeded:${result.toJson()}")
                                    auth(SensorErrorEnum.AUTHENTICATION_SUCCESS)
                                }

                                @SuppressLint("MissingPermission")
                                override fun onAuthenticationFailed() {
                                    super.onAuthenticationFailed()
                                    logger.info("onAuthenticationFailed:验证失败")
                                    auth(SensorErrorEnum.AUTHENTICATION_FAILED)
                                }
                            })
                    } else {
                        auth(SensorErrorEnum.NO_PERMISSION)
                    }
                }
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                auth(SensorErrorEnum.NO_HARDWARE)
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                auth(SensorErrorEnum.HW_UNAVAILABLE)
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                auth(SensorErrorEnum.NO_ENROLLED_FINGERPRINTS)
            }
        }
    }

    private fun checkFingerConfig() {
        if (fingerConfig.title.isEmpty()) {
            throw FingerPrintsException("标题不能为空")
        }
        if (fingerConfig.negativeButtonTxt.isEmpty()) {
            throw FingerPrintsException("取消按钮文本不能为空")
        }
    }
}