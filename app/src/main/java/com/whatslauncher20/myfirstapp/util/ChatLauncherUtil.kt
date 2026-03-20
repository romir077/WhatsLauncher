package com.whatslauncher20.myfirstapp.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

private const val PREFS_NAME = "whatslauncher_prefs"
private const val KEY_RECENT = "recent_numbers"
private const val MAX_RECENT = 10
private const val KEY_FAVORITES = "favorite_numbers"
private const val KEY_TEMPLATES = "message_templates"

fun validatePhone(phone: String): String? {
    if (phone.isEmpty()) return "Please enter a phone number"
    if (phone.length < 4 || phone.length > 15) return "Enter a valid phone number"
    if (!phone.all { it.isDigit() }) return "Only digits allowed"
    return null
}

fun openWhatsApp(context: Context, fullNumber: String, message: String = "") {
    val url = if (message.isNotEmpty()) {
        "https://wa.me/$fullNumber?text=${Uri.encode(message)}"
    } else {
        "https://wa.me/$fullNumber"
    }
    launchAppUrl(context, url, "WhatsApp")
}

fun openTelegram(context: Context, fullNumber: String, @Suppress("UNUSED_PARAMETER") message: String = "") {
    val uri = "tg://resolve?phone=$fullNumber"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    intent.setPackage("org.telegram.messenger")
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        webIntent.setPackage("org.telegram.messenger.web")
        try {
            context.startActivity(webIntent)
        } catch (e2: ActivityNotFoundException) {
            launchAppUrl(context, "https://t.me/+$fullNumber", "Telegram")
        }
    }
}

fun openSignal(context: Context, fullNumber: String, @Suppress("UNUSED_PARAMETER") message: String = "") {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sgnl://signal.me/#p/+$fullNumber"))
    intent.setPackage("org.thoughtcrime.securesms")
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Fallback to web link
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://signal.me/#p/+$fullNumber")))
        } catch (e2: ActivityNotFoundException) {
            Toast.makeText(context, "Signal is not installed", Toast.LENGTH_LONG).show()
        }
    }
}

fun openArattai(context: Context, countryCode: String, phoneNumber: String, message: String = "") {
    val url = buildString {
        append("https://aratt.ai/message/$countryCode-$phoneNumber")
        if (message.isNotEmpty()) {
            append("?text=${Uri.encode(message)}")
        }
    }
    launchAppUrl(context, url, "Arattai")
}

private fun launchAppUrl(context: Context, url: String, appName: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "$appName is not installed", Toast.LENGTH_LONG).show()
    }
}

fun loadRecentNumbers(context: Context): List<String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val raw = prefs.getString(KEY_RECENT, "") ?: ""
    return if (raw.isEmpty()) emptyList() else raw.split("|")
}

fun saveRecentNumber(context: Context, number: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val existing = loadRecentNumbers(context).toMutableList()
    existing.remove(number)
    existing.add(0, number)
    val trimmed = existing.take(MAX_RECENT)
    prefs.edit().putString(KEY_RECENT, trimmed.joinToString("|")).apply()
}

fun removeRecentNumber(context: Context, number: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val existing = loadRecentNumbers(context).toMutableList()
    existing.remove(number)
    prefs.edit().putString(KEY_RECENT, existing.joinToString("|")).apply()
}

fun clearRecentNumbers(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().remove(KEY_RECENT).apply()
}

// --- Favorites ---

fun loadFavorites(context: Context): List<String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val raw = prefs.getString(KEY_FAVORITES, "") ?: ""
    return if (raw.isEmpty()) emptyList() else raw.split("|")
}

fun addFavorite(context: Context, number: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val existing = loadFavorites(context).toMutableList()
    if (!existing.contains(number)) {
        existing.add(0, number)
        prefs.edit().putString(KEY_FAVORITES, existing.joinToString("|")).apply()
    }
}

fun removeFavorite(context: Context, number: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val existing = loadFavorites(context).toMutableList()
    existing.remove(number)
    prefs.edit().putString(KEY_FAVORITES, existing.joinToString("|")).apply()
}

fun isFavorite(context: Context, number: String): Boolean {
    return loadFavorites(context).contains(number)
}

// --- Message Templates ---

fun loadTemplates(context: Context): List<String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val raw = prefs.getString(KEY_TEMPLATES, "") ?: ""
    return if (raw.isEmpty()) emptyList() else raw.split("\u001F")
}

fun saveTemplate(context: Context, template: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val existing = loadTemplates(context).toMutableList()
    existing.remove(template)
    existing.add(0, template)
    prefs.edit().putString(KEY_TEMPLATES, existing.joinToString("\u001F")).apply()
}

fun removeTemplate(context: Context, template: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val existing = loadTemplates(context).toMutableList()
    existing.remove(template)
    prefs.edit().putString(KEY_TEMPLATES, existing.joinToString("\u001F")).apply()
}

fun extractPhoneNumber(text: String): String? {
    val cleaned = text.replace(Regex("[\\s\\-()]+"), "")
    val match = Regex("(?:\\+?91)?([6-9]\\d{9})").find(cleaned)
    return match?.groupValues?.get(1)
}

data class Country(val flag: String, val name: String, val code: String)

val COUNTRY_CODES = listOf(
    Country("\uD83C\uDDE6\uD83C\uDDEB", "Afghanistan", "+93"),
    Country("\uD83C\uDDE6\uD83C\uDDF1", "Albania", "+355"),
    Country("\uD83C\uDDE9\uD83C\uDDFF", "Algeria", "+213"),
    Country("\uD83C\uDDE6\uD83C\uDDF8", "American Samoa", "+1684"),
    Country("\uD83C\uDDE6\uD83C\uDDE9", "Andorra", "+376"),
    Country("\uD83C\uDDE6\uD83C\uDDF4", "Angola", "+244"),
    Country("\uD83C\uDDE6\uD83C\uDDEE", "Anguilla", "+1264"),
    Country("\uD83C\uDDE6\uD83C\uDDF6", "Antarctica", "+672"),
    Country("\uD83C\uDDE6\uD83C\uDDEC", "Antigua & Barbuda", "+1268"),
    Country("\uD83C\uDDE6\uD83C\uDDF7", "Argentina", "+54"),
    Country("\uD83C\uDDE6\uD83C\uDDF2", "Armenia", "+374"),
    Country("\uD83C\uDDE6\uD83C\uDDFC", "Aruba", "+297"),
    Country("\uD83C\uDDE6\uD83C\uDDFA", "Australia", "+61"),
    Country("\uD83C\uDDE6\uD83C\uDDF9", "Austria", "+43"),
    Country("\uD83C\uDDE6\uD83C\uDDFF", "Azerbaijan", "+994"),
    Country("\uD83C\uDDE7\uD83C\uDDF8", "Bahamas", "+1242"),
    Country("\uD83C\uDDE7\uD83C\uDDED", "Bahrain", "+973"),
    Country("\uD83C\uDDE7\uD83C\uDDE9", "Bangladesh", "+880"),
    Country("\uD83C\uDDE7\uD83C\uDDE7", "Barbados", "+1246"),
    Country("\uD83C\uDDE7\uD83C\uDDFE", "Belarus", "+375"),
    Country("\uD83C\uDDE7\uD83C\uDDEA", "Belgium", "+32"),
    Country("\uD83C\uDDE7\uD83C\uDDFF", "Belize", "+501"),
    Country("\uD83C\uDDE7\uD83C\uDDEF", "Benin", "+229"),
    Country("\uD83C\uDDE7\uD83C\uDDF2", "Bermuda", "+1441"),
    Country("\uD83C\uDDE7\uD83C\uDDF9", "Bhutan", "+975"),
    Country("\uD83C\uDDE7\uD83C\uDDF4", "Bolivia", "+591"),
    Country("\uD83C\uDDE7\uD83C\uDDE6", "Bosnia & Herzegovina", "+387"),
    Country("\uD83C\uDDE7\uD83C\uDDFC", "Botswana", "+267"),
    Country("\uD83C\uDDE7\uD83C\uDDF7", "Brazil", "+55"),
    Country("\uD83C\uDDE7\uD83C\uDDF3", "Brunei", "+673"),
    Country("\uD83C\uDDE7\uD83C\uDDEC", "Bulgaria", "+359"),
    Country("\uD83C\uDDE7\uD83C\uDDEB", "Burkina Faso", "+226"),
    Country("\uD83C\uDDE7\uD83C\uDDEE", "Burundi", "+257"),
    Country("\uD83C\uDDF0\uD83C\uDDED", "Cambodia", "+855"),
    Country("\uD83C\uDDE8\uD83C\uDDF2", "Cameroon", "+237"),
    Country("\uD83C\uDDE8\uD83C\uDDE6", "Canada", "+1"),
    Country("\uD83C\uDDE8\uD83C\uDDFB", "Cape Verde", "+238"),
    Country("\uD83C\uDDF0\uD83C\uDDFE", "Cayman Islands", "+1345"),
    Country("\uD83C\uDDE8\uD83C\uDDEB", "Central African Republic", "+236"),
    Country("\uD83C\uDDF9\uD83C\uDDE9", "Chad", "+235"),
    Country("\uD83C\uDDE8\uD83C\uDDF1", "Chile", "+56"),
    Country("\uD83C\uDDE8\uD83C\uDDF3", "China", "+86"),
    Country("\uD83C\uDDE8\uD83C\uDDF4", "Colombia", "+57"),
    Country("\uD83C\uDDF0\uD83C\uDDF2", "Comoros", "+269"),
    Country("\uD83C\uDDE8\uD83C\uDDEC", "Congo", "+242"),
    Country("\uD83C\uDDE8\uD83C\uDDE9", "Congo (DRC)", "+243"),
    Country("\uD83C\uDDE8\uD83C\uDDF7", "Costa Rica", "+506"),
    Country("\uD83C\uDDE8\uD83C\uDDEE", "C\u00f4te d'Ivoire", "+225"),
    Country("\uD83C\uDDED\uD83C\uDDF7", "Croatia", "+385"),
    Country("\uD83C\uDDE8\uD83C\uDDFA", "Cuba", "+53"),
    Country("\uD83C\uDDE8\uD83C\uDDFE", "Cyprus", "+357"),
    Country("\uD83C\uDDE8\uD83C\uDDFF", "Czech Republic", "+420"),
    Country("\uD83C\uDDE9\uD83C\uDDF0", "Denmark", "+45"),
    Country("\uD83C\uDDE9\uD83C\uDDEF", "Djibouti", "+253"),
    Country("\uD83C\uDDE9\uD83C\uDDF2", "Dominica", "+1767"),
    Country("\uD83C\uDDE9\uD83C\uDDF4", "Dominican Republic", "+1809"),
    Country("\uD83C\uDDEA\uD83C\uDDE8", "Ecuador", "+593"),
    Country("\uD83C\uDDEA\uD83C\uDDEC", "Egypt", "+20"),
    Country("\uD83C\uDDF8\uD83C\uDDFB", "El Salvador", "+503"),
    Country("\uD83C\uDDEC\uD83C\uDDF6", "Equatorial Guinea", "+240"),
    Country("\uD83C\uDDEA\uD83C\uDDF7", "Eritrea", "+291"),
    Country("\uD83C\uDDEA\uD83C\uDDEA", "Estonia", "+372"),
    Country("\uD83C\uDDF8\uD83C\uDDFF", "Eswatini", "+268"),
    Country("\uD83C\uDDEA\uD83C\uDDF9", "Ethiopia", "+251"),
    Country("\uD83C\uDDEB\uD83C\uDDF0", "Falkland Islands", "+500"),
    Country("\uD83C\uDDEB\uD83C\uDDF4", "Faroe Islands", "+298"),
    Country("\uD83C\uDDEB\uD83C\uDDEF", "Fiji", "+679"),
    Country("\uD83C\uDDEB\uD83C\uDDEE", "Finland", "+358"),
    Country("\uD83C\uDDEB\uD83C\uDDF7", "France", "+33"),
    Country("\uD83C\uDDEC\uD83C\uDDEB", "French Guiana", "+594"),
    Country("\uD83C\uDDF5\uD83C\uDDEB", "French Polynesia", "+689"),
    Country("\uD83C\uDDEC\uD83C\uDDE6", "Gabon", "+241"),
    Country("\uD83C\uDDEC\uD83C\uDDF2", "Gambia", "+220"),
    Country("\uD83C\uDDEC\uD83C\uDDEA", "Georgia", "+995"),
    Country("\uD83C\uDDE9\uD83C\uDDEA", "Germany", "+49"),
    Country("\uD83C\uDDEC\uD83C\uDDED", "Ghana", "+233"),
    Country("\uD83C\uDDEC\uD83C\uDDEE", "Gibraltar", "+350"),
    Country("\uD83C\uDDEC\uD83C\uDDF7", "Greece", "+30"),
    Country("\uD83C\uDDEC\uD83C\uDDF1", "Greenland", "+299"),
    Country("\uD83C\uDDEC\uD83C\uDDE9", "Grenada", "+1473"),
    Country("\uD83C\uDDEC\uD83C\uDDF5", "Guadeloupe", "+590"),
    Country("\uD83C\uDDEC\uD83C\uDDFA", "Guam", "+1671"),
    Country("\uD83C\uDDEC\uD83C\uDDF9", "Guatemala", "+502"),
    Country("\uD83C\uDDEC\uD83C\uDDF3", "Guinea", "+224"),
    Country("\uD83C\uDDEC\uD83C\uDDFC", "Guinea-Bissau", "+245"),
    Country("\uD83C\uDDEC\uD83C\uDDFE", "Guyana", "+592"),
    Country("\uD83C\uDDED\uD83C\uDDF9", "Haiti", "+509"),
    Country("\uD83C\uDDED\uD83C\uDDF3", "Honduras", "+504"),
    Country("\uD83C\uDDED\uD83C\uDDF0", "Hong Kong", "+852"),
    Country("\uD83C\uDDED\uD83C\uDDFA", "Hungary", "+36"),
    Country("\uD83C\uDDEE\uD83C\uDDF8", "Iceland", "+354"),
    Country("\uD83C\uDDEE\uD83C\uDDF3", "India", "+91"),
    Country("\uD83C\uDDEE\uD83C\uDDE9", "Indonesia", "+62"),
    Country("\uD83C\uDDEE\uD83C\uDDF7", "Iran", "+98"),
    Country("\uD83C\uDDEE\uD83C\uDDF6", "Iraq", "+964"),
    Country("\uD83C\uDDEE\uD83C\uDDEA", "Ireland", "+353"),
    Country("\uD83C\uDDEE\uD83C\uDDF1", "Israel", "+972"),
    Country("\uD83C\uDDEE\uD83C\uDDF9", "Italy", "+39"),
    Country("\uD83C\uDDEF\uD83C\uDDF2", "Jamaica", "+1876"),
    Country("\uD83C\uDDEF\uD83C\uDDF5", "Japan", "+81"),
    Country("\uD83C\uDDEF\uD83C\uDDF4", "Jordan", "+962"),
    Country("\uD83C\uDDF0\uD83C\uDDFF", "Kazakhstan", "+7"),
    Country("\uD83C\uDDF0\uD83C\uDDEA", "Kenya", "+254"),
    Country("\uD83C\uDDF0\uD83C\uDDEE", "Kiribati", "+686"),
    Country("\uD83C\uDDF0\uD83C\uDDF5", "North Korea", "+850"),
    Country("\uD83C\uDDF0\uD83C\uDDF7", "South Korea", "+82"),
    Country("\uD83C\uDDF0\uD83C\uDDFC", "Kuwait", "+965"),
    Country("\uD83C\uDDF0\uD83C\uDDEC", "Kyrgyzstan", "+996"),
    Country("\uD83C\uDDF1\uD83C\uDDE6", "Laos", "+856"),
    Country("\uD83C\uDDF1\uD83C\uDDFB", "Latvia", "+371"),
    Country("\uD83C\uDDF1\uD83C\uDDE7", "Lebanon", "+961"),
    Country("\uD83C\uDDF1\uD83C\uDDF8", "Lesotho", "+266"),
    Country("\uD83C\uDDF1\uD83C\uDDF7", "Liberia", "+231"),
    Country("\uD83C\uDDF1\uD83C\uDDFE", "Libya", "+218"),
    Country("\uD83C\uDDF1\uD83C\uDDEE", "Liechtenstein", "+423"),
    Country("\uD83C\uDDF1\uD83C\uDDF9", "Lithuania", "+370"),
    Country("\uD83C\uDDF1\uD83C\uDDFA", "Luxembourg", "+352"),
    Country("\uD83C\uDDF2\uD83C\uDDF4", "Macau", "+853"),
    Country("\uD83C\uDDF2\uD83C\uDDEC", "Madagascar", "+261"),
    Country("\uD83C\uDDF2\uD83C\uDDFC", "Malawi", "+265"),
    Country("\uD83C\uDDF2\uD83C\uDDFE", "Malaysia", "+60"),
    Country("\uD83C\uDDF2\uD83C\uDDFB", "Maldives", "+960"),
    Country("\uD83C\uDDF2\uD83C\uDDF1", "Mali", "+223"),
    Country("\uD83C\uDDF2\uD83C\uDDF9", "Malta", "+356"),
    Country("\uD83C\uDDF2\uD83C\uDDED", "Marshall Islands", "+692"),
    Country("\uD83C\uDDF2\uD83C\uDDF6", "Martinique", "+596"),
    Country("\uD83C\uDDF2\uD83C\uDDF7", "Mauritania", "+222"),
    Country("\uD83C\uDDF2\uD83C\uDDFA", "Mauritius", "+230"),
    Country("\uD83C\uDDF2\uD83C\uDDFD", "Mexico", "+52"),
    Country("\uD83C\uDDEB\uD83C\uDDF2", "Micronesia", "+691"),
    Country("\uD83C\uDDF2\uD83C\uDDE9", "Moldova", "+373"),
    Country("\uD83C\uDDF2\uD83C\uDDE8", "Monaco", "+377"),
    Country("\uD83C\uDDF2\uD83C\uDDF3", "Mongolia", "+976"),
    Country("\uD83C\uDDF2\uD83C\uDDEA", "Montenegro", "+382"),
    Country("\uD83C\uDDF2\uD83C\uDDF8", "Montserrat", "+1664"),
    Country("\uD83C\uDDF2\uD83C\uDDE6", "Morocco", "+212"),
    Country("\uD83C\uDDF2\uD83C\uDDFF", "Mozambique", "+258"),
    Country("\uD83C\uDDF2\uD83C\uDDF2", "Myanmar", "+95"),
    Country("\uD83C\uDDF3\uD83C\uDDE6", "Namibia", "+264"),
    Country("\uD83C\uDDF3\uD83C\uDDF7", "Nauru", "+674"),
    Country("\uD83C\uDDF3\uD83C\uDDF5", "Nepal", "+977"),
    Country("\uD83C\uDDF3\uD83C\uDDF1", "Netherlands", "+31"),
    Country("\uD83C\uDDF3\uD83C\uDDE8", "New Caledonia", "+687"),
    Country("\uD83C\uDDF3\uD83C\uDDFF", "New Zealand", "+64"),
    Country("\uD83C\uDDF3\uD83C\uDDEE", "Nicaragua", "+505"),
    Country("\uD83C\uDDF3\uD83C\uDDEA", "Niger", "+227"),
    Country("\uD83C\uDDF3\uD83C\uDDEC", "Nigeria", "+234"),
    Country("\uD83C\uDDF3\uD83C\uDDF4", "Norway", "+47"),
    Country("\uD83C\uDDF4\uD83C\uDDF2", "Oman", "+968"),
    Country("\uD83C\uDDF5\uD83C\uDDF0", "Pakistan", "+92"),
    Country("\uD83C\uDDF5\uD83C\uDDFC", "Palau", "+680"),
    Country("\uD83C\uDDF5\uD83C\uDDF8", "Palestine", "+970"),
    Country("\uD83C\uDDF5\uD83C\uDDE6", "Panama", "+507"),
    Country("\uD83C\uDDF5\uD83C\uDDEC", "Papua New Guinea", "+675"),
    Country("\uD83C\uDDF5\uD83C\uDDFE", "Paraguay", "+595"),
    Country("\uD83C\uDDF5\uD83C\uDDEA", "Peru", "+51"),
    Country("\uD83C\uDDF5\uD83C\uDDED", "Philippines", "+63"),
    Country("\uD83C\uDDF5\uD83C\uDDF1", "Poland", "+48"),
    Country("\uD83C\uDDF5\uD83C\uDDF9", "Portugal", "+351"),
    Country("\uD83C\uDDF5\uD83C\uDDF7", "Puerto Rico", "+1787"),
    Country("\uD83C\uDDF6\uD83C\uDDE6", "Qatar", "+974"),
    Country("\uD83C\uDDF7\uD83C\uDDEA", "R\u00e9union", "+262"),
    Country("\uD83C\uDDF7\uD83C\uDDF4", "Romania", "+40"),
    Country("\uD83C\uDDF7\uD83C\uDDFA", "Russia", "+7"),
    Country("\uD83C\uDDF7\uD83C\uDDFC", "Rwanda", "+250"),
    Country("\uD83C\uDDF0\uD83C\uDDF3", "Saint Kitts & Nevis", "+1869"),
    Country("\uD83C\uDDF1\uD83C\uDDE8", "Saint Lucia", "+1758"),
    Country("\uD83C\uDDFB\uD83C\uDDE8", "Saint Vincent", "+1784"),
    Country("\uD83C\uDDFC\uD83C\uDDF8", "Samoa", "+685"),
    Country("\uD83C\uDDF8\uD83C\uDDF2", "San Marino", "+378"),
    Country("\uD83C\uDDF8\uD83C\uDDF9", "S\u00e3o Tom\u00e9 & Pr\u00edncipe", "+239"),
    Country("\uD83C\uDDF8\uD83C\uDDE6", "Saudi Arabia", "+966"),
    Country("\uD83C\uDDF8\uD83C\uDDF3", "Senegal", "+221"),
    Country("\uD83C\uDDF7\uD83C\uDDF8", "Serbia", "+381"),
    Country("\uD83C\uDDF8\uD83C\uDDE8", "Seychelles", "+248"),
    Country("\uD83C\uDDF8\uD83C\uDDF1", "Sierra Leone", "+232"),
    Country("\uD83C\uDDF8\uD83C\uDDEC", "Singapore", "+65"),
    Country("\uD83C\uDDF8\uD83C\uDDF0", "Slovakia", "+421"),
    Country("\uD83C\uDDF8\uD83C\uDDEE", "Slovenia", "+386"),
    Country("\uD83C\uDDF8\uD83C\uDDE7", "Solomon Islands", "+677"),
    Country("\uD83C\uDDF8\uD83C\uDDF4", "Somalia", "+252"),
    Country("\uD83C\uDDFF\uD83C\uDDE6", "South Africa", "+27"),
    Country("\uD83C\uDDF8\uD83C\uDDF8", "South Sudan", "+211"),
    Country("\uD83C\uDDEA\uD83C\uDDF8", "Spain", "+34"),
    Country("\uD83C\uDDF1\uD83C\uDDF0", "Sri Lanka", "+94"),
    Country("\uD83C\uDDF8\uD83C\uDDE9", "Sudan", "+249"),
    Country("\uD83C\uDDF8\uD83C\uDDF7", "Suriname", "+597"),
    Country("\uD83C\uDDF8\uD83C\uDDEA", "Sweden", "+46"),
    Country("\uD83C\uDDE8\uD83C\uDDED", "Switzerland", "+41"),
    Country("\uD83C\uDDF8\uD83C\uDDFE", "Syria", "+963"),
    Country("\uD83C\uDDF9\uD83C\uDDFC", "Taiwan", "+886"),
    Country("\uD83C\uDDF9\uD83C\uDDEF", "Tajikistan", "+992"),
    Country("\uD83C\uDDF9\uD83C\uDDFF", "Tanzania", "+255"),
    Country("\uD83C\uDDF9\uD83C\uDDED", "Thailand", "+66"),
    Country("\uD83C\uDDF9\uD83C\uDDF1", "Timor-Leste", "+670"),
    Country("\uD83C\uDDF9\uD83C\uDDEC", "Togo", "+228"),
    Country("\uD83C\uDDF9\uD83C\uDDF4", "Tonga", "+676"),
    Country("\uD83C\uDDF9\uD83C\uDDF9", "Trinidad & Tobago", "+1868"),
    Country("\uD83C\uDDF9\uD83C\uDDF3", "Tunisia", "+216"),
    Country("\uD83C\uDDF9\uD83C\uDDF7", "Turkey", "+90"),
    Country("\uD83C\uDDF9\uD83C\uDDF2", "Turkmenistan", "+993"),
    Country("\uD83C\uDDF9\uD83C\uDDE8", "Turks & Caicos", "+1649"),
    Country("\uD83C\uDDF9\uD83C\uDDFB", "Tuvalu", "+688"),
    Country("\uD83C\uDDFA\uD83C\uDDEC", "Uganda", "+256"),
    Country("\uD83C\uDDFA\uD83C\uDDE6", "Ukraine", "+380"),
    Country("\uD83C\uDDE6\uD83C\uDDEA", "United Arab Emirates", "+971"),
    Country("\uD83C\uDDEC\uD83C\uDDE7", "United Kingdom", "+44"),
    Country("\uD83C\uDDFA\uD83C\uDDF8", "United States", "+1"),
    Country("\uD83C\uDDFA\uD83C\uDDFE", "Uruguay", "+598"),
    Country("\uD83C\uDDFA\uD83C\uDDFF", "Uzbekistan", "+998"),
    Country("\uD83C\uDDFB\uD83C\uDDFA", "Vanuatu", "+678"),
    Country("\uD83C\uDDFB\uD83C\uDDEA", "Venezuela", "+58"),
    Country("\uD83C\uDDFB\uD83C\uDDF3", "Vietnam", "+84"),
    Country("\uD83C\uDDFB\uD83C\uDDEC", "British Virgin Islands", "+1284"),
    Country("\uD83C\uDDFB\uD83C\uDDEE", "US Virgin Islands", "+1340"),
    Country("\uD83C\uDDFE\uD83C\uDDEA", "Yemen", "+967"),
    Country("\uD83C\uDDFF\uD83C\uDDF2", "Zambia", "+260"),
    Country("\uD83C\uDDFF\uD83C\uDDFC", "Zimbabwe", "+263")
)

fun findCountryByCode(code: String): Country? =
    COUNTRY_CODES.find { it.code == code }
