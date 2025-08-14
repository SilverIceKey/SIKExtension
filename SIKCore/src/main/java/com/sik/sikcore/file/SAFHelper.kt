package com.sik.sikcore.file

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Storage Access Framework 助手：
 * - Context.moveFileToDir(treeUri, file): 把本地 File “移动”到指定目录 Uri（treeUri）下
 * - Context.uriToFile(uri): 把任意 Uri 落地为可直接访问的临时 File
 *
 * 依赖：
 * implementation("androidx.documentfile:documentfile:1.0.1")
 */
object SAFHelper {

    /**
     * 把 [file] 移动到 [treeUri] 指向的目录下。
     * 这里只能“复制+删除源文件”，因为 File → SAF 不支持直接 rename/move。
     *
     * 规则：
     * - 目标文件名用 file.name；若同名存在，自动重命名为 "name (1).ext"。
     * - MIME 基于扩展名简单判断。
     *
     * @return 新文件的 Uri，失败返回 null
     */
    fun Context.moveFileToDir(treeUri: Uri, file: File): Uri? {
        // 尝试持久化读写权限（若已授权会静默成功）
        takePersistedRW(treeUri)

        val parent = DocumentFile.fromTreeUri(this, treeUri) ?: return null
        if (!parent.isDirectory) return null

        val displayName = uniqueName(parent, file.name)
        val mime = guessMimeFromName(file.name)

        // 目标占位
        val target = parent.createFile(mime, displayName) ?: return null

        runCatching {
            copyStreams(FileInputStream(file), contentResolver.openOutputStream(target.uri, "w")!!)
        }.onFailure {
            // 复制失败，清理占位文件
            runCatching { target.delete() }
            return null
        }

        // 复制成功后删除源文件；若删除失败，算“搬运未完成”，回滚目标以避免重复
        if (!file.delete()) {
            runCatching { target.delete() }
            return null
        }

        return target.uri
    }

    /**
     * 把 [file] 复制到 [treeUri] 指向的目录下。
     *
     * 规则：
     * - 目标文件名用 file.name；若同名存在，自动重命名为 "name (1).ext"。
     * - MIME 基于扩展名简单判断。
     *
     * @return 新文件的 Uri，失败返回 null
     */
    fun Context.copyFileToDir(treeUri: Uri, file: File): Uri? {
        // 尝试持久化读写权限（若已授权会静默成功）
        takePersistedRW(treeUri)

        val parent = DocumentFile.fromTreeUri(this, treeUri) ?: return null
        if (!parent.isDirectory) return null

        val displayName = uniqueName(parent, file.name)
        val mime = guessMimeFromName(file.name)

        // 目标占位
        val target = parent.createFile(mime, displayName) ?: return null

        runCatching {
            copyStreams(
                FileInputStream(file),
                contentResolver.openOutputStream(target.uri, "w")!!
            )
        }.onFailure {
            // 复制失败，清理占位文件
            runCatching { target.delete() }
            return null
        }
        return target.uri
    }

    /**
     * 将任意 [uri] 转换为可直接访问的临时 [File]。
     * - file:// 直接返回对应 File
     * - content:// / 其他：复制到 app 的 cacheDir/uri2file 下并返回
     */
    fun Context.uriToFile(uri: Uri): File? {
        when (uri.scheme?.lowercase()) {
            "file" -> {
                val f = File(uri.path ?: return null)
                return if (f.exists()) f else null
            }
        }

        val (nameGuess, _) = queryDisplayNameAndSize(uri)
        val safeName = (nameGuess ?: "tmp_${System.currentTimeMillis()}").replace('/', '_')
        val outDir = File(cacheDir, "uri2file").apply { mkdirs() }
        // 尽量不覆盖已有文件
        val outFile = uniqueLocalName(outDir, safeName)

        runCatching {
            contentResolver.openInputStream(uri).use { input ->
                if (input == null) throw IllegalStateException("openInputStream null for $uri")
                FileOutputStream(outFile).use { output ->
                    copyStreams(input, output)
                }
            }
        }.onFailure {
            // 失败时清理中间文件
            runCatching { outFile.delete() }
            return null
        }

        return outFile
    }

    // ———————————— Helpers ————————————

    private fun Context.takePersistedRW(treeUri: Uri) {
        val cr = contentResolver
        val haveRead = cr.persistedUriPermissions.any { it.uri == treeUri && it.isReadPermission }
        val haveWrite = cr.persistedUriPermissions.any { it.uri == treeUri && it.isWritePermission }
        if (!haveRead || !haveWrite) {
            runCatching {
                cr.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
        }
    }

    /** 查 display name 与 size（有些 Provider 不给 size，做兜底即可） */
    private fun Context.queryDisplayNameAndSize(uri: Uri): Pair<String?, Long?> {
        var name: String? = null
        var size: Long? = null
        runCatching {
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
                ?.use { c: Cursor ->
                    if (c.moveToFirst()) {
                        name = c.getString(0)
                        if (!c.isNull(1)) size = c.getLong(1)
                    }
                }
        }
        return name to size
    }

    private fun guessMimeFromName(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.endsWith(".png") -> "image/png"
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
            lower.endsWith(".gif") -> "image/gif"
            lower.endsWith(".webp") -> "image/webp"
            lower.endsWith(".pdf") -> "application/pdf"
            lower.endsWith(".txt") -> "text/plain"
            lower.endsWith(".csv") -> "text/csv"
            lower.endsWith(".json") -> "application/json"
            lower.endsWith(".zip") -> "application/zip"
            else -> "application/octet-stream"
        }
    }

    /** 给 DocumentFile 目录生成不冲突的文件名 */
    private fun uniqueName(parent: DocumentFile, desired: String): String {
        if (parent.findFile(desired) == null) return desired
        val dot = desired.lastIndexOf('.')
        val base = if (dot <= 0) desired else desired.substring(0, dot)
        val ext = if (dot <= 0) "" else desired.substring(dot) // 包含点
        var i = 1
        while (true) {
            val candidate = "$base ($i)$ext"
            if (parent.findFile(candidate) == null) return candidate
            i++
        }
    }

    /** 给本地目录生成不冲突的文件名 */
    private fun uniqueLocalName(dir: File, desired: String): File {
        val f0 = File(dir, desired)
        if (!f0.exists()) return f0
        val dot = desired.lastIndexOf('.')
        val base = if (dot <= 0) desired else desired.substring(0, dot)
        val ext = if (dot <= 0) "" else desired.substring(dot)
        var i = 1
        while (true) {
            val candidate = File(dir, "$base ($i)$ext")
            if (!candidate.exists()) return candidate
            i++
        }
    }

    private fun copyStreams(input: InputStream, output: OutputStream, bufferSize: Int = 256 * 1024) {
        val buf = ByteArray(bufferSize)
        while (true) {
            val n = input.read(buf)
            if (n == -1) break
            output.write(buf, 0, n)
        }
        output.flush()
    }
}