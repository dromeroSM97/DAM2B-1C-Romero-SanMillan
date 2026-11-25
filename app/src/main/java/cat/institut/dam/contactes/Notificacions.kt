package cat.institut.dam.contactes

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Gestiona la creació del canal de notificacions i l'enviament
 * de l'avís quan s'han carregat els contactes.
 */
object Notificacions {

    private const val CANAL_ID = "contactes_carregats"
    private const val NOTIFICACIO_ID = 1

    @SuppressLint("MissingPermission")
    fun mostraContactesCarregats(context: Context, quantitat: Int) {
        // 1. Creem el canal de notificacions (obligatori a partir d'Android 8 / API 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                context.getString(R.string.canal_notificacions),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val gestor = context.getSystemService(NotificationManager::class.java)
            gestor.createNotificationChannel(canal)
        }

        // 2. Si l'usuari ha denegat el permís o ha desactivat les notificacions, sortim sense fer res
        val managerCompat = NotificationManagerCompat.from(context)
        if (!managerCompat.areNotificationsEnabled()) return

        // 3. Construïm i mostrem la notificació
        val notificacio = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacio)
            .setContentTitle(context.getString(R.string.notificacio_titol))
            .setContentText(context.getString(R.string.notificacio_text, quantitat))
            .setAutoCancel(true)
            .build()

        managerCompat.notify(NOTIFICACIO_ID, notificacio)
    }
}