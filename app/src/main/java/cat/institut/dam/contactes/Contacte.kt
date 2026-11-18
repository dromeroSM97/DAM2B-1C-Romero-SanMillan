package cat.institut.dam.contactes

import org.json.JSONObject

data class Contacte(
    val id: Int,
    val nom: String,
    val username: String,
    val email: String,
    val telefon: String,
    val web: String,
    val carrer: String,
    val ciutat: String,
    val codiPostal: String,
    val latitud: Double,
    val longitud: Double
)
{
    val inicials: String
        get() = nom.trim().split(" ").map { it.first() }.take(2).joinToString("").uppercase()

    val adreçaCompleta: String
        get() = "$carrer, $ciutat ($codiPostal)"

    val urlAvatar: String
        get() = "https://i.pravatar.cc/300?u=$username"

    companion object {
        fun desDeJson(json: JSONObject): Contacte {
            val adreça = json.getJSONObject("address")
            val geo = adreça.getJSONObject("geo")
            return Contacte(
                id = json.getInt("id"),
                nom = json.getString("name"),
                username = json.getString("username"),
                email = json.getString("email"),
                telefon = json.getString("phone"),
                web = json.getString("website"),
                carrer = adreça.getString("street"),
                ciutat = adreça.getString("city"),
                codiPostal = adreça.getString("zipcode"),
                latitud = geo.getString("lat").toDouble(),
                longitud = geo.getString("lng").toDouble()
            )
        }
    }
}


