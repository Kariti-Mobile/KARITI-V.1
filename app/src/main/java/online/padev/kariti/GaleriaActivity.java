package online.padev.kariti;

import static online.padev.kariti.Compactador.datasImgs;
import static online.padev.kariti.Compactador.listCartoes;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class GaleriaActivity extends AppCompatActivity {
    AppCompatButton btnFinalizar;
    ImageButton btnVoltar;
    FloatingActionButton btnAdcionarFoto;
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    String nomeCartaoScaneado, nomeCartaoCaturado, dataCartao, contexto;
    Integer qtdQuestoes, qtdAlternativas;
    TextView titulo;
    BancoDados bancoDados;
    Scanner scanner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galeria);

        bancoDados = new BancoDados(this);
        scanner = new Scanner(this);

        btnVoltar = findViewById(R.id.imgBtnVoltar);
        titulo = findViewById(R.id.toolbar_title);
        btnFinalizar = findViewById(R.id.buttonFinalizar);
        btnAdcionarFoto = findViewById(R.id.buttonAdicionarFoto);

        contexto = getIntent().getExtras().getString("contexto");
        if(contexto.equals("inicia_correcao")){
            scanner.iniciarScanner();
        }else{
            nomeCartaoCaturado = getIntent().getExtras().getString("nomeImagem"); //receba o nome do cartão enviado por intent de CameraX
            dataCartao = getIntent().getExtras().getString("dataHora");
            if(!listCartoes.contains(nomeCartaoCaturado)){//Caso esse cartão ainda não esteja na lista de cartoes.
                listCartoes.add(nomeCartaoCaturado);  //Insere nome do novo cartão na lista
                datasImgs.add(dataCartao); //Insere a data e hora de captura do cartao
            }
        }

        //File diretorio = new File(getExternalMediaDirs()[0], "CameraXApp");//diretorios das fotos
        File diretorio = getOutputImgs();

        recyclerView = findViewById(R.id.recyclerViewFotos);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AdapterGaleria(this, diretorio);
        recyclerView.setAdapter(adapter);

        titulo.setText(String.format("%s","Correção"));

        btnAdcionarFoto.setOnClickListener(view -> {
            scanner.iniciarScanner();//Inicia o Scanner
        });
        btnFinalizar.setOnClickListener(v -> {
            btnFinalizar.setEnabled(false);
            try {
                File fileZip = getOutputZip(); //cria um diretorio interno para um arquivo zip
                if(Compactador.compactador(getOutputImgs(), fileZip.getAbsolutePath())){
                    listCartoes.clear();
                    datasImgs.clear();
                    try {
                        File dir = getCacheDir();
                        File fileJson = getOutputJson(dir);
                        UploadEjson.enviarArquivosP(fileZip, new FileOutputStream(fileJson), dir, bancoDados);
                        iniciaAnimacaoCorrecao();
                    } catch (Exception e){
                        Log.e("Kariti", "(Erro ao tentar enviar arquivo zip para correção ou baixar Json) "+e.getMessage());
                        Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }else Toast.makeText(GaleriaActivity.this, "Erro de Compactação", Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                Log.e("kariti",e.getMessage());
                Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnVoltar.setOnClickListener(view -> {
            if(listCartoes.isEmpty()){
                getOnBackPressedDispatcher();
                finish();
            }else{
                avidoDeCancelamento();
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
    /**
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {  // Se o conteúdo do QR Code não for nulo
                String qrCodeConteudo = result.getContents(); // Conteúdo do QR Code
                qrCodeConteudo = qrCodeConteudo.replaceAll("#", "");
                String[] partes = qrCodeConteudo.split("\\."); // partes do valor do QRCODE
                String id_prova = partes[0];
                Boolean verificaProva = bancoDados.verificaExisteProvaPId(id_prova);
                if(verificaProva == null){
                    Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(verificaProva) {
                    qtdQuestoes = bancoDados.pegarQtdQuestoes(id_prova);
                    qtdAlternativas = bancoDados.pegarQtdAlternativas(id_prova);
                    if (qtdQuestoes == null || qtdAlternativas == null){
                        Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    nomeCartaoScaneado = partes[0] + "_" + partes[1] + "_" + qtdQuestoes + "_" + qtdAlternativas + ".jpg";
                    Intent intent = new Intent(this, CameraxActivity.class);
                    intent.putExtra("nomeImagem", nomeCartaoScaneado);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Prova não cadastrada!", Toast.LENGTH_SHORT).show();
                    if(contexto.equals("inicia_correcao")){
                        scanner.iniciarScanner();
                    }
                }
            } else {
                // Caso o usuário tenha cancelado o scanner ou não tenha lido um QR Code
                if(listCartoes.isEmpty()){
                    finish();
                }
                Toast.makeText(this, "Leitura do QR Code cancelada.", Toast.LENGTH_SHORT).show();
            }
        }else {
            // Tratamento adicional para resultados que não sejam do IntentIntegrator
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    public void iniciaAnimacaoCorrecao(){
        Intent intent = new Intent(getApplicationContext(), AnimacaoCorrecao.class);
        startActivity(intent);
        finish();
    }

    public static String leitor(String path) throws IOException {
        BufferedReader buffRead = new BufferedReader(new FileReader(path));
        String linha = "", texto = "";
        while (linha != null) {
            texto += linha;
            linha = buffRead.readLine();
        }
        buffRead.close();
        //texto = "{}";
        return texto;
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
    private File getOutputImgs(){
        File mediaDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File appDir = new File(mediaDir, "CameraXApp");

        if (!appDir.exists()) {
            Log.e("kariti", "diretorio não existe!!");
            return null;
        }
        return appDir;
    }
    private File getOutputZip(){
        File fileZip = new File(getCacheDir(), "saida.zip");
        if (!fileZip.exists()) {
            try {
                // Tenta criar o arquivo
                if (fileZip.createNewFile()) {
                    Log.e("kariti","Diretorio criado");
                } else {
                    Log.i("kariti", "Arquivo já existe.");
                }
            } catch (IOException e) {
                Log.e("kariti", "Erro ao criar diretorio!");
            }
        }
        return fileZip;
    }
    private File getOutputJson(File dir){
        File fileJson = new File(dir, "json.json");
        if (!fileJson.exists()) {
            try {
                // Tenta criar o arquivo
                if (fileJson.createNewFile()) {
                    Log.e("kariti","Diretorio criado");
                } else {
                    Log.i("kariti", "Arquivo já existe.");
                }
            } catch (IOException e) {
                Log.e("kariti", "Erro ao criar diretorio!");
            }
        }
        return fileJson;
    }
}