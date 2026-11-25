package cat.institut.dam.contactes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    companion object {
        /** Codi de la petició de permís de notificacions. */
        private const val REQUEST_NOTIFICACIONS = 100
    }

    private val filPrincipal = Handler(Looper.getMainLooper())

    private lateinit var etCerca: EditText
    private lateinit var btnActualitza: Button
    private lateinit var pbCarga: ProgressBar
    private lateinit var txtBuit: TextView
    private lateinit var txtAvis: TextView
    private lateinit var rvContactes: RecyclerView
    private lateinit var adapter: ContacteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Finestres.aplica(this, findViewById<View>(R.id.arrel))

        etCerca = findViewById(R.id.etCerca)
        btnActualitza = findViewById(R.id.btnActualitza)
        pbCarga = findViewById(R.id.pbCarga)
        txtBuit = findViewById(R.id.txtBuit)
        txtAvis = findViewById(R.id.txtAvis)
        rvContactes = findViewById(R.id.rvContactes)

        // TODO 1: crea el teu ContacteAdapter, assigna'l al RecyclerView i posa-li un LinearLayoutManager.
        adapter = ContacteAdapter(
            onContacteClick = { contacte ->
                val intent = Intent(this, ContacteDetallActivity::class.java)
                intent.putExtra(ContacteDetallActivity.EXTRA_ID, contacte.id)
                startActivity(intent)
            },
            onFavoritClick = { contacte ->
                ContactesRepository.alternaFavorit(contacte.id)
            }
        )
        rvContactes.layoutManager = LinearLayoutManager(this)
        rvContactes.adapter = adapter

        // TODO 2: el cercador ha de filtrar la llista a cada tecla.
        etCerca.doAfterTextChanged { text ->
            filtraLlista(text?.toString().orEmpty())
        }

        // TODO 3: el botó ha de tornar a carregar els contactes.
        btnActualitza.setOnClickListener {
            carrega()
        }

        // TODO 4: crida a carrega() que gestionarà la càrrega, el permís i la notificació.
        carrega()
    }

    override fun onResume() {
        super.onResume()
        // Refresquem la llista per mantenir sincronitzats els favorits en tornar de ContacteDetallActivity
        filtraLlista(etCerca.text?.toString().orEmpty())
    }

    /**
     * TODO 5: carrega els contactes en un fil de segon pla i torna al fil principal amb Handler.
     */
    private fun carrega() {
        pbCarga.visibility = View.VISIBLE
        txtAvis.visibility = View.GONE
        btnActualitza.isEnabled = false

        Thread {
            var modeLocal = false
            val llistaCarregada = try {
                ContactesService.desDeXarxa()
            } catch (e: Exception) {
                modeLocal = true
                try {
                    ContactesService.desDeRecurs(this)
                } catch (e2: Exception) {
                    emptyList()
                }
            }

            filPrincipal.post {
                pbCarga.visibility = View.GONE
                btnActualitza.isEnabled = true

                if (modeLocal && llistaCarregada.isNotEmpty()) {
                    txtAvis.setText(R.string.avis_dades_locals)
                    txtAvis.visibility = View.VISIBLE
                }

                ContactesRepository.actualitza(llistaCarregada)
                filtraLlista(etCerca.text?.toString().orEmpty())

                if (llistaCarregada.isNotEmpty()) {
                    demanaPermisNotificacions()
                    Notificacions.mostraContactesCarregats(this, llistaCarregada.size)
                } else {
                    Toast.makeText(this, R.string.error_carrega, Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun filtraLlista(texte: String) {
        val filtrats = ContactesRepository.cerca(texte)
        adapter.actualitza(filtrats)

        if (filtrats.isEmpty()) {
            txtBuit.visibility = View.VISIBLE
            txtBuit.setText(
                if (texte.isBlank()) R.string.txt_buit else R.string.txt_buit_cerca
            )
        } else {
            txtBuit.visibility = View.GONE
        }
    }

    /**
     * A partir d'Android 13 (API 33) cal demanar permís en temps d'execució
     * per poder mostrar notificacions.
     */
    private fun demanaPermisNotificacions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val permissio = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(this, permissio) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        requestPermissions(arrayOf(permissio), REQUEST_NOTIFICACIONS)
    }

    /**
     * Resposta de l'usuari a la petició de permís: si el denega, avisa'l amb un Toast.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICACIONS) {
            val concedit = grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (concedit) {
                Notificacions.mostraContactesCarregats(this, ContactesRepository.llista.size)
            } else {
                // TODO 6: avisa l'usuari que no es mostraran notificacions
                Toast.makeText(this, R.string.permis_denegada, Toast.LENGTH_LONG).show()
            }
        }
    }
}