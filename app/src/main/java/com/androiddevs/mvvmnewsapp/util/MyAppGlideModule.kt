package com.androiddevs.mvvmnewsapp.util

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.PictureDrawable
import android.util.Log
import android.widget.ImageView
import androidx.annotation.Nullable
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader


@GlideModule
class MyAppGlideModule : AppGlideModule() {
    private val IMAGE_CACHE_SIZE: Long = 1024 * 1024 * 20
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // Handle memory size
        val memorySizeCalculator = MemorySizeCalculator.Builder(context)
            .setMemoryCacheScreens(2f)
            .build()
        builder.setMemoryCache(LruResourceCache(memorySizeCalculator.memoryCacheSize.toLong()))

        // bitmap pool size
        val bitMapPoolCalculator = MemorySizeCalculator.Builder(context)
            .setBitmapPoolScreens(3f)
            .build()
        builder.setBitmapPool(LruBitmapPool(bitMapPoolCalculator.bitmapPoolSize.toLong()))

        builder.setDefaultRequestOptions(
            RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565)
        )

        builder.setLogLevel(
            if (com.androiddevs.mvvmnewsapp.BuildConfig.DEBUG) {
                Log.DEBUG
            } else {
                Log.ERROR
            }
        )
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val cache = Cache(context.cacheDir, IMAGE_CACHE_SIZE)
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.HEADERS

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .connectTimeout(45, TimeUnit.SECONDS)
            .cache(cache)
            .build()
        registry.replace(
            GlideUrl::class.java, InputStream::class.java,OkHttpUrlLoader.Factory(client)
        )
        registry
            .register(SVG::class.java, PictureDrawable::class.java, SvgDrawableTranscoder())
            .append(InputStream::class.java, SVG::class.java, SvgDecoder())
    }

    class SvgDecoder : ResourceDecoder<InputStream?, SVG?> {
        override fun handles(source: InputStream, options: Options): Boolean {
            // TODO: Can we tell?
            return true
        }

        @Throws(IOException::class)
        override fun decode(
            source: InputStream, width: Int, height: Int, options: Options
        ): Resource<SVG?>? {
            return try {
                val svg = SVG.getFromInputStream(source)
                if (width != SIZE_ORIGINAL) {
                    svg.documentWidth = width.toFloat()
                }
                if (height != SIZE_ORIGINAL) {
                    svg.documentHeight = height.toFloat()
                }
                SimpleResource(svg)
            } catch (ex: SVGParseException) {
                throw IOException("Cannot load SVG from stream", ex)
            }
        }
    }

    class SvgDrawableTranscoder : ResourceTranscoder<SVG?, PictureDrawable> {
        @Nullable
        override fun transcode(
            toTranscode: Resource<SVG?>, options: Options
        ): Resource<PictureDrawable>? {
            val svg = toTranscode.get()
            val picture = svg.renderToPicture()
            val drawable = PictureDrawable(picture)
            return SimpleResource(drawable)
        }
    }
    class SvgSoftwareLayerSetter :
        RequestListener<PictureDrawable?> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any,
            target: Target<PictureDrawable?>,
            isFirstResource: Boolean
        ): Boolean {
            val view = (target as ImageViewTarget<*>).view
            view.setLayerType(ImageView.LAYER_TYPE_NONE, null)
            return false
        }

        override fun onResourceReady(
            resource: PictureDrawable?,
            model: Any,
            target: Target<PictureDrawable?>,
            dataSource: DataSource,
            isFirstResource: Boolean
        ): Boolean {
            val view = (target as ImageViewTarget<*>).view
            view.setLayerType(ImageView.LAYER_TYPE_SOFTWARE, null)
            return false
        }
    }
}