package online.padev.kariti;

import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DownloadResultadoCorrecao {
    File arquivoCsv;
    Context context;
    String filePdf;
    public DownloadResultadoCorrecao(File arquivoCsv, Context context, String filePdf){
        this.context = context;
        this.arquivoCsv = arquivoCsv;
        this.filePdf = filePdf;
    }

    public void solicitarResultadoCorrecao(FileOutputStream fos, File fSaida, DownloadManager baixarPdf) {
        Thread thread = new Thread(() -> {
            try {
                String URL = "http://kariti.online/src/services/download_grades/download.php";
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(URL);
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                FileBody x = new FileBody(arquivoCsv);
                entityBuilder.addPart("userfile[]", x);
                HttpEntity entity = entityBuilder.build();
                post.setEntity(entity);
                HttpResponse response = client.execute(post);
                HttpEntity httpEntity = response.getEntity();
                InputStream is = httpEntity.getContent();
                int inByte;
                byte[]buffer = new byte[1024];
                while((inByte = is.read(buffer)) != -1)
                    fos.write(buffer, 0, inByte);
                is.close();
                fos.close();
                baixarPdf.addCompletedDownload(filePdf, "Cartao Resposta: " + filePdf, true, "application /pdf", fSaida.getAbsolutePath(), fSaida.length(), true);
                Log.e("Kariti", "Fim");
            } catch (Exception e) {
                Log.e("kariti", e.toString());
            }
        });
        thread.start();
    }

    /**
     * Método usado para download de cartões respostas, em dispositivos com versão 10 e superiores
     */
    public void baixarResultadoCorrecao11() {
        Thread thread = new Thread(() -> {
            try {
                String URL = "http://kariti.online/src/services/download_grades/download.php";
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(URL);
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                FileBody x = new FileBody(arquivoCsv);
                entityBuilder.addPart("userfile[]", x);
                HttpEntity entity = entityBuilder.build();
                post.setEntity(entity);
                HttpResponse response = client.execute(post);
                HttpEntity httpEntity = response.getEntity();
                InputStream is = httpEntity.getContent();
                List<byte[]> dadosProva = new ArrayList<>();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    byte[] tempBuffer = Arrays.copyOf(buffer, bytesRead);
                    dadosProva.add(tempBuffer);
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    ContentResolver resolver = context.getContentResolver(); // Usando o contexto fornecido
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.Downloads.DISPLAY_NAME, filePdf);
                    contentValues.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                    contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                    Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);

                    try {
                        if (uri != null) {
                            OutputStream outputStream = resolver.openOutputStream(uri);
                            if (outputStream != null) {
                                for (byte[] dados : dadosProva) {
                                    outputStream.write(dados);
                                }
                                outputStream.close();
                                notifyDownloadComplete(filePdf, uri);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("kariti", e.getMessage());
                    } finally {
                        is.close();
                    }
                }
            } catch (Exception e) {
                Log.e("kariti", e.toString());
            }
        });
        thread.start();
    }

    private void notifyDownloadComplete(String fileName, Uri fileUri) {
        // Criar um canal de notificação (Android 8.0 e superior)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            String channelId = "download_channel";
            CharSequence name = "Download Notifications";
            String description = "Notificações sobre downloads";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Criar PendingIntent para abrir o arquivo PDF
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        // Criar e exibir a notificação
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "download_channel")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("Download Completo")
                .setContentText("O arquivo " + fileName + " foi baixado com sucesso!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true).setContentIntent(pendingIntent); // A notificação desaparece quando o usuário a toca

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1, builder.build());
    }
}
