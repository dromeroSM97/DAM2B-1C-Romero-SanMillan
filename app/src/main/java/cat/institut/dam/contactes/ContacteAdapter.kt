package cat.institut.dam.contactes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter del RecyclerView que mostra la llista de contactes
 * i delega els clics de la fila i del favorit cap a MainActivity.
 */
class ContacteAdapter(
    private val onContacteClick: (Contacte) -> Unit,
    private val onFavoritClick: (Contacte) -> Unit
) : RecyclerView.Adapter<ContacteAdapter.ContacteViewHolder>() {

    private var contactes: List<Contacte> = emptyList()

    fun actualitza(novs: List<Contacte>) {
        contactes = novs
        notifyDataSetChanged()
    }

    class ContacteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvInicials: TextView = itemView.findViewById(R.id.tvInicials)
        val tvNom: TextView = itemView.findViewById(R.id.tvNom)
        val tvCiutat: TextView = itemView.findViewById(R.id.tvCiutat)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val chkFavorit: CheckBox = itemView.findViewById(R.id.chkFavorit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContacteViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contacte, parent, false)
        return ContacteViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ContacteViewHolder, position: Int) {
        val contacte = contactes[position]

        holder.tvInicials.text = contacte.inicials
        holder.tvNom.text = contacte.nom
        holder.tvCiutat.text = contacte.ciutat
        holder.tvEmail.text = contacte.email

        // Evitem que es dispari el listener en reciclar la vista
        holder.chkFavorit.setOnCheckedChangeListener(null)
        holder.chkFavorit.isChecked = ContactesRepository.esFavorit(contacte.id)
        holder.chkFavorit.setOnCheckedChangeListener { _, _ ->
            onFavoritClick(contacte)
        }

        // Clic sobre tota la fila per obrir la pantalla de detall
        holder.itemView.setOnClickListener {
            onContacteClick(contacte)
        }
    }

    override fun getItemCount(): Int = contactes.size
}