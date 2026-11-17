package com.example.varinbloknot.update

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.varinbloknot.BuildConfig
import com.example.varinbloknot.data.ContentStore
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

private const val HOUR = 3_600_000L

private fun http(url: String, accept: String? = null): HttpURLConnection =
    (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 10_000; readTimeout = 20_000
        setRequestProperty("User-Agent", "VarinBloknot")
        if (accept != null) setRequestProperty("Accept", accept)
    }

private fun httpText(url: String, accept: String? = null): String {
    val c = http(url, accept)
    try {
        if (c.responseCode != 200) error("HTTP ${c.responseCode}")
        return c.inputStream.bufferedReader().readText()
    } finally { c.disconnect() }
}

private fun sha256(f: File): String {
    val md = MessageDigest.getInstance("SHA-256")
    f.inputStream().use { s ->
        val b = ByteArray(8192)
        while (true) { val n = s.read(b); if (n < 0) break; md.update(b, 0, n) }
    }
    return md.digest().joinToString("") { "%02x".format(it) }
}

/** "0.1.10" новее "0.1.9" */
fun isNewer(a: String, b: String): Boolean {
    val x = a.trim().removePrefix("v").split('.').map { it.toIntOrNull() ?: 0 }
    val y = b.trim().removePrefix("v").split('.').map { it.toIntOrNull() ?: 0 }
    for (i in 0 until maxOf(x.size, y.size)) {
        val p = x.getOrElse(i) { 0 }
        val q = y.getOrElse(i) { 0 }
        if (p != q) return p > q
    }
    return false
}

private fun caps(ctx: Context): NetworkCapabilities? {
    val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.getNetworkCapabilities(cm.activeNetwork)
}
private fun unmetered(ctx: Context) = caps(ctx)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) == true
private fun online(ctx: Context) = caps(ctx)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

object ContentUpdater {
    private val raw = "https://raw.githubusercontent.com/${BuildConfig.REPO}/${BuildConfig.BRANCH}/content/"

    /** Возвращает (проверка была выполнена, новый набор применён). */
    fun run(ctx: Context, localVersion: Int, lastCheck: Long, now: Long, force: Boolean): Pair<Boolean, Boolean> {
        if (!online(ctx)) return false to false
        if (!force && now - lastCheck < 6 * HOUR) return false to false
        return try { true to apply(ctx, localVersion) } catch (e: Exception) { true to false }
    }

    private fun apply(ctx: Context, localVersion: Int): Boolean {
        val m = JSONObject(httpText(raw + "manifest.json"))
        val remoteVersion = m.getInt("contentVersion")
        if (remoteVersion <= localVersion) return false
        // Контенту нужна более новая программа — ждём обновления программы
        if (isNewer(m.optString("minAppVersion", "0.0.0"), BuildConfig.VERSION_NAME)) return false

        val files = m.getJSONArray("files").let { a ->
            List(a.length()) { a.getJSONObject(it).let { o -> o.getString("path") to o.getString("sha256") } }
        }
        // Не Wi-Fi: картинки не качаем, только маленькие json
        if (!unmetered(ctx) && files.any { it.first.endsWith(".webp") && !haveSame(ctx, it.first, it.second) }) return false

        val base = ContentStore.dirFor(ctx).apply { mkdirs() }
        val name = "v$remoteVersion"
        val target = File(base, name)
        val tmp = File(base, "tmp_$name").apply { deleteRecursively(); mkdirs() }
        try {
            for ((path, hash) in files) {
                val dest = File(tmp, path).apply { parentFile?.mkdirs() }
                if (!copyIfSame(ctx, path, hash, dest)) {
                    val c = http(raw + path)
                    try {
                        if (c.responseCode != 200) error("HTTP ${c.responseCode} $path")
                        c.inputStream.use { i -> dest.outputStream().use { o -> i.copyTo(o) } }
                    } finally { c.disconnect() }
                    if (sha256(dest) != hash) error("sha256 mismatch: $path")
                }
            }
            File(tmp, "manifest.json").writeText(m.toString())
            target.deleteRecursively()
            if (!tmp.renameTo(target)) error("rename failed")
            ContentStore.activate(ctx, name)
            return true
        } finally { tmp.deleteRecursively() }
    }

    private fun currentDir(ctx: Context): File? =
        File(ctx.filesDir, "content_current.txt").takeIf { it.exists() }?.readText()?.trim()
            ?.let { File(ContentStore.dirFor(ctx), it) }?.takeIf { it.isDirectory }

    private fun haveSame(ctx: Context, path: String, hash: String): Boolean {
        val cur = currentDir(ctx)?.let { File(it, path) }
        return cur != null && cur.exists() && sha256(cur) == hash
    }

    /** Берём файл из текущего набора или из APK, если он не менялся. */
    private fun copyIfSame(ctx: Context, path: String, hash: String, dest: File): Boolean {
        val cur = currentDir(ctx)?.let { File(it, path) }
        if (cur != null && cur.exists() && sha256(cur) == hash) { cur.copyTo(dest, true); return true }
        try {
            ctx.assets.open(path).use { i -> dest.outputStream().use { o -> i.copyTo(o) } }
            if (sha256(dest) == hash) return true
            dest.delete()
        } catch (e: Exception) { /* в APK такого файла нет */ }
        return false
    }
}

data class AppRelease(val version: String, val notes: String, val apkUrl: String)

object AppUpdater {
    /** Возвращает (проверка была выполнена, найденная новая версия или null). */
    fun check(ctx: Context, lastCheck: Long, now: Long, force: Boolean): Pair<Boolean, AppRelease?> {
        if (!online(ctx)) return false to null
        if (!force && now - lastCheck < 24 * HOUR) return false to null
        return try {
            val j = JSONObject(httpText("https://api.github.com/repos/${BuildConfig.REPO}/releases/latest", "application/vnd.github+json"))
            val tag = j.getString("tag_name")
            val assets = j.getJSONArray("assets")
            var apk: String? = null
            for (i in 0 until assets.length()) {
                val a = assets.getJSONObject(i)
                if (a.getString("name").endsWith(".apk")) apk = a.getString("browser_download_url")
            }
            if (apk != null && isNewer(tag, BuildConfig.VERSION_NAME)) true to AppRelease(tag.removePrefix("v"), j.optString("body"), apk)
            else true to null
        } catch (e: Exception) { true to null }
    }

    fun download(ctx: Context, r: AppRelease): File {
        val dir = File(ctx.cacheDir, "updates").apply { deleteRecursively(); mkdirs() }
        val f = File(dir, "VarinBloknot-${r.version}.apk")
        val c = http(r.apkUrl)
        try {
            if (c.responseCode != 200) error("HTTP ${c.responseCode}")
            c.inputStream.use { i -> f.outputStream().use { o -> i.copyTo(o) } }
        } finally { c.disconnect() }
        return f
    }

    /** Открыть системное окно установки (или настройку «разрешить установку из этого источника»). */
    fun install(ctx: Context, apk: File) {
        if (!ctx.packageManager.canRequestPackageInstalls()) {
            ctx.startActivity(
                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${ctx.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            return
        }
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.files", apk)
        ctx.startActivity(
            Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
