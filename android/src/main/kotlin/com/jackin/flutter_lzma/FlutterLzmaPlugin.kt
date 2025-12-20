package com.jackin.flutter_lzma

import com.jackin.plzmasdk.PLzmaNativeApis
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/** FlutterLzmaPlugin */
class FlutterLzmaPlugin : FlutterPlugin, MethodCallHandler {
    private lateinit var channel: MethodChannel
    private val executor: ExecutorService = Executors.newCachedThreadPool()

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_lzma")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "getFfmpegVersion" -> {
                executor.execute { handleFfmpegVersion(call, result) }
            }
            "compress" -> {
                executor.execute { handleCompress(call, result) }
            }
            "compressDir" -> {
                executor.execute { handleCompressDir(call, result) }
            }
            "extract" -> {
                executor.execute { handleExtract(call, result) }
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        executor.shutdown()
    }

    private fun handleFfmpegVersion(call: MethodCall, result: Result) {
        try {
            val version = PLzmaNativeApis.instance.getFfmpegVersion()
            result.success(version)
        } catch (e: Exception) {
            result.error("FFMPEG_VERSION_FAILED", "Failed to get FFmpeg version: ${e.message}", e.localizedMessage)
        }
    }

    private fun handleCompress(call: MethodCall, result: Result) {
        val args = call.arguments<Map<String, Any>>()
        
        val filePaths = args?.get("sourceFiles") as? List<*>
        if (filePaths.isNullOrEmpty()) {
            result.error("INVALID_ARGUMENTS", "Source files list is empty or null", null)
            return
        }

        val destFile = args["destFile"] as? String
        if (destFile.isNullOrBlank()) {
            result.error("INVALID_ARGUMENTS", "Destination file path is required", null)
            return
        }

        val stringPaths = filePaths.filterIsInstance<String>()
        if (stringPaths.size != filePaths.size) {
            result.error("INVALID_ARGUMENTS", "All source file paths must be strings", null)
            return
        }

        try {
            val success = PLzmaNativeApis.instance.compress(stringPaths.toTypedArray(), destFile)
            result.success(success)
        } catch (e: Exception) {
            result.error("COMPRESSION_FAILED", "Failed to compress files: ${e.message}", e.localizedMessage)
        }
    }

    private fun handleCompressDir(call: MethodCall, result: Result) {
        val args = call.arguments<Map<String, Any>>()
        
        val sourceDir = args?.get("sourceDir") as? String
        if (sourceDir.isNullOrBlank()) {
            result.error("INVALID_ARGUMENTS", "Source directory path is required", null)
            return
        }

        val destFile = args["destFile"] as? String
        if (destFile.isNullOrBlank()) {
            result.error("INVALID_ARGUMENTS", "Destination file path is required", null)
            return
        }

        try {
            val success = PLzmaNativeApis.instance.compressDir(sourceDir, destFile)
            result.success(success)
        } catch (e: Exception) {
            result.error("COMPRESSION_FAILED", "Failed to compress directory: ${e.message}", e.localizedMessage)
        }
    }

    private fun handleExtract(call: MethodCall, result: Result) {
        val args = call.arguments<Map<String, Any>>()
        
        val sourceFile = args?.get("sourceFile") as? String
        if (sourceFile.isNullOrBlank()) {
            result.error("INVALID_ARGUMENTS", "Source file path is required", null)
            return
        }

        val destDir = args["destDir"] as? String
        if (destDir.isNullOrBlank()) {
            result.error("INVALID_ARGUMENTS", "Destination directory path is required", null)
            return
        }

        try {
            val success = PLzmaNativeApis.instance.extract(sourceFile, destDir)
            result.success(success)
        } catch (e: Exception) {
            result.error("EXTRACTION_FAILED", "Failed to extract archive: ${e.message}", e.localizedMessage)
        }
    }
}