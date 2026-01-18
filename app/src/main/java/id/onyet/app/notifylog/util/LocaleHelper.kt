package id.onyet.app.notifylog.util

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import android.view.View
import java.util.Locale

object LocaleHelper {

    enum class Language(val code: String, val displayName: String, val flag: String, val isRtl: Boolean = false) {
        ENGLISH("en", "English", "🇺🇸"),
        INDONESIAN("in", "Bahasa Indonesia", "🇮🇩"),
        CHINESE("zh", "中文", "🇨🇳"),
        ARABIC("ar", "العربية", "🇸🇦", isRtl = true),
        RUSSIAN("ru", "Русский", "🇷🇺"),
        GERMAN("de", "Deutsch", "🇩🇪");

        companion object {
            fun fromCode(code: String): Language {
                return entries.find { it.code == code } ?: ENGLISH
            }
        }
    }

    fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)

        // Also update the application-level resources for dialogs and modals
        updateResources(context, locale)

        config.setLocales(LocaleList(locale))
        return context.createConfigurationContext(config)
    }

    @Suppress("DEPRECATION")
    private fun updateResources(context: Context, locale: Locale) {
        // Update application context resources if available
        val appContext = try {
            context.applicationContext
        } catch (e: Exception) {
            null
        }

        if (appContext != null && appContext !== context) {
            val appResources = appContext.resources
            val appConfig = Configuration(appResources.configuration)
            appConfig.setLocales(LocaleList(locale))
            appResources.updateConfiguration(appConfig, appResources.displayMetrics)
        }

        // Also update the current context resources
        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocales(LocaleList(locale))
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun getLocale(context: Context): Locale {
        return context.resources.configuration.locales[0]
    }

    fun isRtl(languageCode: String): Boolean {
        return Language.fromCode(languageCode).isRtl
    }
}
