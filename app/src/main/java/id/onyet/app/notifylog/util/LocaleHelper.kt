package id.onyet.app.notifylog.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    enum class Language(val code: String, val displayName: String, val flag: String) {
        ENGLISH("en", "English", "🇺🇸"),
        INDONESIAN("in", "Bahasa Indonesia", "🇮🇩"),
        CHINESE("zh", "中文", "🇨🇳"),
        ARABIC("ar", "العربية", "🇸🇦"),
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

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }

    fun getLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
    }

    fun getCurrentLanguage(context: Context): Language {
        val locale = getLocale(context)
        return Language.fromCode(locale.language)
    }
}
