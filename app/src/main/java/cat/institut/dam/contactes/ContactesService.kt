package cat.institut.dam.contactes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

/**
 * Servei encarregat d'obtenir els contactes (de la xarxa o d'assets),
 * parsejar el JSON i descarregar les imatges d'avatar.
 */
object ContactesService {

    const val URL_CONTACTES = "https://jsonplaceholder.typicode.com/users"

    // Descarrega el JSON del servidor; si falla, llança excepció perquè MainActivity carregui les dades locals
    fun desDeXarxa(): List<Contacte> {
        val connexio = URL(URL_CONTACTES).openConnection() as HttpURLConnection
        try {
            connexio.requestMethod = "GET"
            connexio.connectTimeout = 8000
            connexio.readTimeout = 8000

            if (connexio.responseCode != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("El servidor ha respost amb ${connexio.responseCode}")
            }

            val text = connexio.inputStream.bufferedReader().use { it.readText() }
            return parseja(text)
        } finally {
            connexio.disconnect()
        }
    }

    // Llegeix la còpia local dins d'app/src/main/assets/contactes.json
    fun desDeRecurs(context: Context): List<Contacte> {
        val text = context.assets.open("contactes.json").bufferedReader().use { it.readText() }
        return parseja(text)
    }

    // Converteix el text JSON (un array d'objectes) en una llista de Contacte
    fun parseja(text: String): List<Contacte> {
        val llista = mutableListOf<Contacte>()
        val array = JSONArray(text)

        for (i in 0 until array.length()) {
            val objecteJson = array.getJSONObject(i)
            llista.add(Contacte.desDeJson(objecteJson))
        }

        return llista
    }

    // Descarrega la imatge de l'avatar; retorna null si no hi ha xarxa o falla la descàrrega
    fun baixaImatge(url: String): Bitmap? {
        var connexio: HttpURLConnection? = null
        return try {
            connexio = URL(url).openConnection() as HttpURLConnection
            connexio.connectTimeout = 8000
            connexio.readTimeout = 8000

            if (connexio.responseCode != HttpURLConnection.HTTP_OK) {
                return null
            }

            connexio.inputStream.use { entrada ->
                BitmapFactory.decodeStream(entrada)
            }
        } catch (e: Exception) {
            null
        } finally {
            connexio?.disconnect()
        }
    }
}