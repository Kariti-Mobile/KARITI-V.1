package online.padev.kariti;

import android.app.Activity;

import com.google.zxing.integration.android.IntentIntegrator;

public class Scanner{
    private final Activity activity;

    public Scanner(Activity activity) {
        this.activity = activity;
    }

    public void iniciarScanner(){
        IntentIntegrator intentIntegrator = new IntentIntegrator(activity);
        intentIntegrator.setOrientationLocked(false);// rotação do scanner
        intentIntegrator.setPrompt("Escaneie o QR CODE da Prova");
        //intentIntegrator.setBeepEnabled(true);              // som ao scanear
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE); //especifica o qrcode
        intentIntegrator.initiateScan();                     //iniciar o scan
    }
}
