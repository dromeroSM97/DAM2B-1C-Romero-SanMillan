package cat.institut.dam.contactes

import android.net.Uri

object ContactesRepository {

    private val contactes = mutableListOf<Contacte>()
    private val favorits = mutableSetOf<Int>()
    private val fotos = mutableMapOf<Int, Uri>()

    val llista: List<Contacte>
        get() = contactes

    fun actualitza(novs: List<Contacte>) {
        contactes.clear()
        contactes.addAll(novs)
    }

    fun contacte(id: Int): Contacte? {
        return contactes.firstOrNull { it.id == id }
    }

    fun cerca(text: String): List<Contacte> {
        if (text.isBlank()) return llista

        return contactes.filter { contacte ->
            contacte.nom.contains(text, ignoreCase = true) ||
                    contacte.ciutat.contains(text, ignoreCase = true) ||
                    contacte.email.contains(text, ignoreCase = true)
        }
    }

    fun esFavorit(id: Int): Boolean {
        return id in favorits
    }

    fun alternaFavorit(id: Int) {
        if (esFavorit(id)) {
            favorits.remove(id)
        } else {
            favorits.add(id)
        }
    }

    fun desaFoto(id: Int, uri: Uri) {
        fotos[id] = uri
    }

    fun foto(id: Int): Uri? {
        return fotos[id]
    }
}