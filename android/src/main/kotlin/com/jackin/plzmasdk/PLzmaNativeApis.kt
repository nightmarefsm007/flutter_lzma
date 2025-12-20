package com.jackin.plzmasdk

class PLzmaNativeApis {

    external fun compress(filePaths: Array<String>, targetPath: String): Boolean

    external fun compressDir(sourcePath: String, targetPath: String): Boolean

    external fun extract(archivePath: String, targetPath: String): Boolean

    external fun getFfmpegVersion(): String

    companion object {
        init {
            // Load FFmpeg libraries in correct order
            System.loadLibrary("avutil")
            System.loadLibrary("swresample")
            System.loadLibrary("avcodec")
            System.loadLibrary("avformat")
            System.loadLibrary("swscale")
            System.loadLibrary("avfilter")
            
            // Load plzmasdk library
            System.loadLibrary("plzmasdk")
        }

        val instance: PLzmaNativeApis by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { PLzmaNativeApis() }
    }
}