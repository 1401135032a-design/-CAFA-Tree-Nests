package com.example

import android.app.Application
import android.content.ComponentCallbacks2
import coil.Coil
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

/**
 * Application class configured for high-concurrency campus scale (5,000+ active users)
 * with bounded Coil memory/disk caches and proactive memory trimming.
 *
 * NOTE: Firebase App Check is intentionally NOT initialized here; it is configured in
 * MainActivity.onCreate(savedInstanceState) with the launch Intent per App Check contract.
 */
class YangmeiApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    // Cap decoded bitmap cache at 20% of available app heap to prevent OOM on image-heavy feeds
                    .maxSizePercent(0.20)
                    .strongReferencesEnabled(true)
                    .weakReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("yangmei_image_cache"))
                    // Bounded 64 MB local disk cache for smooth feed scrolling
                    .maxSizeBytes(64L * 1024L * 1024L)
                    .build()
            }
            .allowRgb565(true)
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            Coil.imageLoader(this).memoryCache?.trimMemory(level)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Coil.imageLoader(this).memoryCache?.clear()
    }
}
