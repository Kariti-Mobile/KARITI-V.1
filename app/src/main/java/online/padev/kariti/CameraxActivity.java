package online.padev.kariti;

import static online.padev.kariti.Compactador.listCartoes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraxActivity extends AppCompatActivity {
    private static final String TAG = "CameraxActivity";
    private PreviewView previewView;
    ImageButton captureButton, aroundCamera, toggleFlash;
    private int cameraFacing = CameraSelector.LENS_FACING_BACK;
    private ImageCapture imageCapture;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    ActivityResultLauncher<String> requestPermissionLauncher;
    String nomeCartao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camerax);

        previewView = findViewById(R.id.previewCameraX);
        captureButton = findViewById(R.id.buttonCaptureX);
        toggleFlash = findViewById(R.id.buttonFlashX);
        aroundCamera = findViewById(R.id.aroundCameraX);

        nomeCartao = Objects.requireNonNull(getIntent().getExtras()).getString("nomeImagem");

        // Inicializar o launcher para solicitação de permissão
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                startCamera();
            } else {
                Toast.makeText(CameraxActivity.this, "Permissão de câmera não concedida.", Toast.LENGTH_SHORT).show();
                finish(); // Encerrar a atividade se a permissão não for concedida
            }
        });

        // Verificar e solicitar permissão da câmera, se necessário
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
        aroundCamera.setOnClickListener(view -> {
            if (cameraFacing == CameraSelector.LENS_FACING_BACK){
                cameraFacing = CameraSelector.LENS_FACING_FRONT;
            }else {
                cameraFacing = CameraSelector.LENS_FACING_BACK;
            }
            startCamera();
        });
        // Configurar o listener do botão de captura
        captureButton.setOnClickListener(v -> {
            captureButton.setEnabled(false);
            if (imageCapture != null) {
                captureImage();
            } else {
                Toast.makeText(CameraxActivity.this, "Erro ao capturar imagem. Câmera não inicializada.", Toast.LENGTH_SHORT).show();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(listCartoes.isEmpty()){
                    finish();
                }else{
                    avidoDeCancelamento();
                }
            }
        });
    }
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing)
                        .build();

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                cameraProvider.unbindAll();
                Camera camera = cameraProvider.bindToLifecycle(CameraxActivity.this, cameraSelector, preview, imageCapture);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                toggleFlash.setOnClickListener(view -> setFlashIcon(camera));

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Erro ao iniciar a câmera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureImage() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        Date data = new Date();
        String dataHoraAtual = sdf.format(data);

        File outputDirectory = getOutputDirectory();//Cria um diretorio da pasta para armazenamento das imagens no armazenamento externo

        File diretorioAbsoluto = new File(outputDirectory, nomeCartao);//diretorio de destino da imagem

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(diretorioAbsoluto).build();

        imageCapture.takePicture(outputFileOptions, executorService, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Intent intent = new Intent(getApplicationContext(), GaleriaActivity.class);
                intent.putExtra("contexto","continua_correcao");
                intent.putExtra("nomeImagem", nomeCartao);
                intent.putExtra("dataHora", dataHoraAtual);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Erro ao salvar imagem: " + exception.getMessage(), exception);
                runOnUiThread(() -> Toast.makeText(CameraxActivity.this, "Erro ao capturar imagem.", Toast.LENGTH_SHORT).show());
                startCamera(); // Reiniciar a câmera após a falha na captura
            }
        });
    }

    private void setFlashIcon(Camera camera){
        if (camera.getCameraInfo().hasFlashUnit()){
            if (camera.getCameraInfo().getTorchState().getValue() == 0){
                camera.getCameraControl().enableTorch(true);
                toggleFlash.setImageResource(R.drawable.baseline_flash_off_24);
            }else{
                camera.getCameraControl().enableTorch(false);
                toggleFlash.setImageResource(R.drawable.baseline_bolt_24);
            }
        }else{
            runOnUiThread(() -> Toast.makeText(CameraxActivity.this, "Flash não está disponível no momento", Toast.LENGTH_SHORT).show());
        }
    }

    private File getOutputDirectory() {
        // Obtém o diretório de arquivos externos privados da aplicação
        File mediaDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES); // Altera para diretório de imagens

        // Cria um diretório específico para o app dentro de "Pictures"
        File appDir = new File(mediaDir, "CameraXApp");

        // Verifica se o diretório existe e, se não existir, tenta criá-lo
        if (!appDir.exists()) {
            if (!appDir.mkdirs()) {
                Log.e("kariti", "Falha ao criar diretório de mídia");
                return null;
            }
        }

        return appDir;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown(); // Encerrar o executor quando a atividade for destruída
    }
    public void avidoDeCancelamento(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ATENÇÃO!")
                .setMessage("Caso confirme essa ação, o processo de correção em andamento, será cancelado!\n\n" +
                        "Deseja realmente voltar")
                .setPositiveButton("SIM", (dialog, which) -> {
                    Compactador.listCartoes.clear();
                    getOnBackPressedDispatcher();
                    finish();
                })
                .setNegativeButton("NÃO", (dialog, which) -> {
                    // Código para lidar com o clique no botão Cancelar, se necessário
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}