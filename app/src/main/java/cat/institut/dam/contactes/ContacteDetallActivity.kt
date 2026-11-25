package cat.institut.dam.contactes

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.AlarmClock
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ContacteDetallActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "extra_id"
        private const val REQUEST_FOTO = 100
    }

    private val filPrincipal = Handler(Looper.getMainLooper())
    private lateinit var ivFoto: ImageView
    private var contacteId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detall)
        Finestres.aplica(this, findViewById(R.id.scrollDetall))
        contacteId = intent.getIntExtra(EXTRA_ID, -1)
        val contacte = ContactesRepository.contacte(contacteId)

        if (contacte == null) {
            Toast.makeText(this, R.string.error_contacte, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        ivFoto = findViewById(R.id.ivFoto)
        val tvNom: TextView = findViewById(R.id.tvNom)
        val tvEmail: TextView = findViewById(R.id.tvEmail)
        val tvTelefon: TextView = findViewById(R.id.tvTelefon)
        val tvAdreca: TextView = findViewById(R.id.tvAdreca)
        val tvWeb: TextView = findViewById(R.id.tvWeb)
        val chkFavorit: CheckBox = findViewById(R.id.chkFavorit)

        tvNom.text = contacte.nom
        tvEmail.text = contacte.email
        tvTelefon.text = contacte.telefon
        tvAdreca.text = contacte.adreçaCompleta
        tvWeb.text = contacte.web

        chkFavorit.isChecked = ContactesRepository.esFavorit(contacte.id)
        chkFavorit.setOnCheckedChangeListener { _, _ ->
            ContactesRepository.alternaFavorit(contacte.id)
        }

        carregaImatge(contacte)

        findViewById<Button>(R.id.btnTrucar).setOnClickListener {
            obreIntent(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contacte.telefon}")))
        }

        findViewById<Button>(R.id.btnCorreu).setOnClickListener {
            obreIntent(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${contacte.email}")))
        }

        findViewById<Button>(R.id.btnMapa).setOnClickListener {
            val etiqueta = Uri.encode(contacte.nom)
            val uriMapa = Uri.parse(
                "geo:${contacte.latitud},${contacte.longitud}" +
                        "?q=${contacte.latitud},${contacte.longitud}($etiqueta)"
            )
            obreIntent(Intent(Intent.ACTION_VIEW, uriMapa))
        }

        findViewById<Button>(R.id.btnWeb).setOnClickListener {
            var web = contacte.web
            if (!web.startsWith("http")) web = "https://$web"
            obreIntent(Intent(Intent.ACTION_VIEW, Uri.parse(web)))
        }

        findViewById<Button>(R.id.btnAlarma).setOnClickListener {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, 9)
                putExtra(AlarmClock.EXTRA_MINUTES, 0)
                putExtra(AlarmClock.EXTRA_MESSAGE, getString(R.string.alarma_missatge, contacte.nom))
            }
            obreIntent(intent)
        }

        findViewById<Button>(R.id.btnGaleria).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                )
            }
            @Suppress("DEPRECATION")
            startActivityForResult(intent, REQUEST_FOTO)
        }
    }

    private fun carregaImatge(contacte: Contacte) {
        val fotoDesada = ContactesRepository.foto(contacte.id)
        if (fotoDesada != null) {
            ivFoto.setImageURI(fotoDesada)
            return
        }

        ivFoto.setImageResource(R.drawable.ic_contacte)

        Thread {
            val bitmap = ContactesService.baixaImatge(contacte.urlAvatar)
            if (bitmap != null) {
                filPrincipal.post {
                    ivFoto.setImageBitmap(bitmap)
                }
            }
        }.start()
    }

    private fun obreIntent(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.error_app_no_disponible, Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_FOTO || resultCode != RESULT_OK) return

        val uri = data?.data ?: return
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            // Alguns selectors no concedeixen permís persistent
        }

        ContactesRepository.desaFoto(contacteId, uri)
        ivFoto.setImageURI(uri)
    }
}