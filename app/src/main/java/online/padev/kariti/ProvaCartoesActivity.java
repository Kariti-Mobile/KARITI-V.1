package online.padev.kariti;

import static android.os.Environment.getExternalStoragePublicDirectory;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.DownloadManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ProvaCartoesActivity extends AppCompatActivity {
    ImageButton voltar;
    Button btnBaixarCartoes;
    Integer id_turma, endereco;
    String prova, nomeTurma, id_prova, nomeProva,filePdf;
    ArrayList<String> listagemProvas, listaTurmas, listaAlunos;
    List<String[]> dados;
    ArrayList<Integer> listIdsAlunos;
    BancoDados bancoDados;
    Spinner spinnerTurma, spinnerProva, spinnerAluno;
    TextView titulo;
    private File filecsv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prova_cartoes);

        voltar = findViewById(R.id.imgBtnVoltar);
        spinnerTurma = findViewById(R.id.spinnerTurma);
        spinnerProva = findViewById(R.id.spinnerProva);
        spinnerAluno = findViewById(R.id.spinnerAlunos);
        btnBaixarCartoes = findViewById(R.id.baixarcatoes);
        titulo = findViewById(R.id.toolbar_title);

        bancoDados = new BancoDados(this);

        titulo.setText(String.format("%s","Cartões"));

        endereco = Objects.requireNonNull(getIntent().getExtras()).getInt("endereco");
        prova = getIntent().getExtras().getString("prova");
        listaTurmas = (ArrayList<String>) bancoDados.listarTurmasPorProva();
        if(listaTurmas == null){
            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
            return;
        }

        if(endereco.equals(2)){ //para quando a activity que a chamou foi ProvaActivity
            listaTurmas.add(0,"Selecione a turma");
            SpinnerAdapter adapterTurma = new SpinnerAdapter(this, listaTurmas);
            spinnerTurma.setAdapter(adapterTurma);

        }else if(endereco.equals(1)) { //para quando a activity que chamou for Gabarito
            id_turma = getIntent().getExtras().getInt("id_turma");
            nomeTurma = bancoDados.pegarNomeTurma(String.valueOf(id_turma));
            if (nomeTurma == null){
                Toast.makeText(ProvaCartoesActivity.this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                return;
            }
            listaTurmas.add(0, nomeTurma);
            SpinnerAdapter adapterTurma = new SpinnerAdapter(this, listaTurmas);
            spinnerTurma.setAdapter(adapterTurma);
            //Lista todas provas pertecentes a turma selecionada
            listagemProvas = (ArrayList<String>) bancoDados.listarNomesProvasPorTurma(String.valueOf(id_turma));
            if (listagemProvas == null){
                Toast.makeText(ProvaCartoesActivity.this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                return;
            }
            listagemProvas.add(0, prova);
            SpinnerAdapter adapterProva = new SpinnerAdapter(ProvaCartoesActivity.this, listagemProvas);
            spinnerProva.setAdapter(adapterProva);
            //Lista todos os alunos pertecentes a turma selecionada
            listaAlunos = (ArrayList<String>) bancoDados.listarAlunosPorTurma(id_turma.toString());
            if (listaAlunos == null){
                Toast.makeText(ProvaCartoesActivity.this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                return;
            }
            listaAlunos.add(0, "Todos");
            SpinnerAdapter adapterAluno = new SpinnerAdapter(ProvaCartoesActivity.this, listaAlunos);
            spinnerAluno.setAdapter(adapterAluno);
        }

        spinnerTurma.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0){
                    nomeTurma= spinnerTurma.getSelectedItem().toString();
                    id_turma = bancoDados.pegarIdTurma(nomeTurma);
                    if (id_turma == null){
                        Toast.makeText(ProvaCartoesActivity.this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    listagemProvas = (ArrayList<String>) bancoDados.listarNomesProvasPorTurma(String.valueOf(id_turma));
                    if (listagemProvas == null){
                        Toast.makeText(ProvaCartoesActivity.this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    SpinnerAdapter adapterProva = new SpinnerAdapter(ProvaCartoesActivity.this, listagemProvas);
                    spinnerProva.setAdapter(adapterProva);

                    listaAlunos = (ArrayList<String>) bancoDados.listarAlunosPorTurma(id_turma.toString());
                    if (listaAlunos == null){
                        Toast.makeText(ProvaCartoesActivity.this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    listaAlunos.add(0, "Todos");
                    SpinnerAdapter adapterAluno = new SpinnerAdapter(ProvaCartoesActivity.this, listaAlunos);
                    spinnerAluno.setAdapter(adapterAluno);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        btnBaixarCartoes.setOnClickListener(v -> {
            btnBaixarCartoes.setEnabled(false);
            try {
                if (!VerificaConexaoInternet.verificaConexao(this)) {
                    Toast.makeText(this, "Sem conexão de rede!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(spinnerProva.getSelectedItem() != null) {
                    nomeProva = spinnerProva.getSelectedItem().toString();
                    String aluno = spinnerAluno.getSelectedItem().toString();
                    id_prova = String.valueOf(bancoDados.pegarIdProva(nomeProva, id_turma));
                    String prof = bancoDados.pegarNomeUsuario(BancoDados.USER_ID);
                    String data = bancoDados.pegarDataProva(id_prova);
                    String nota = String.valueOf(bancoDados.pegarNotaProva(id_prova));
                    String questoes = String.valueOf(bancoDados.pegarQtdQuestoes(id_prova));
                    String alternativas = String.valueOf(bancoDados.pegarQtdAlternativas(id_prova));

                    dados = new ArrayList<>();

                    if (aluno.equals("Todos")) {
                        listIdsAlunos = (ArrayList<Integer>) bancoDados.listarIdsAlunosPorTurma(id_turma.toString());
                        if (listIdsAlunos == null){
                            Toast.makeText(ProvaCartoesActivity.this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }else {
                        Integer id_al = bancoDados.pegarIdAluno(aluno);
                        listIdsAlunos.add(id_al);
                    }

                    dados.add(new String[]{"ID_PROVA", "NOME_PROVA", "NOME_PROFESSOR", "NOME_TURMA", "DATA_PROVA", "NOTA_PROVA", "QTD_QUESTOES", "QTD_ALTERNATIVAS", "ID_ALUNO", "NOME_ALUNO"});
                    for (int id : listIdsAlunos) {
                        String nomeAluno = bancoDados.pegaNomeAluno(id);
                        String id_aluno = String.valueOf(id);
                        dados.add(new String[]{id_prova, nomeProva, prof, nomeTurma, data, nota, questoes, alternativas, id_aluno, nomeAluno});
                    }
                    try {
                        //pega data e hora atual
                        filePdf = nomeProva + dataHoraAtual() + ".pdf"; //Cria um nome para o pdf
                        filecsv  = criarDiretorio(); //cria um diretorio interno para adicionar .csv
                        GerarCsv.gerar(dados, filecsv);// Gerando e salvando arquivo.csv
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                            solicitaPermissao();
                        }else {
                            baixarCartoesV11();
                        }
                    }catch (Exception e){
                        Log.e("kariti","Erro: "+e.getMessage());
                        Toast.makeText(this, "Ocorreu uma falha de comunicação no Kariti! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                    }



                }else Toast.makeText(ProvaCartoesActivity.this, "Selecione os dados", Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                Log.e("kariti",e.getMessage());
                Toast.makeText(this, "Ocorreu uma falha de comunicação no Kariti! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
            }

        });
        voltar.setOnClickListener(view -> {
                getOnBackPressedDispatcher();
                finish();
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }
    private void baixarCartoesV9(){
        try {
            DownloadCartoes downloadCartoes = new DownloadCartoes(filecsv, this, filePdf);
            File fSaida = new File(getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filePdf);
            downloadCartoes.baixarCartoesV9(new FileOutputStream(fSaida), fSaida, (DownloadManager) getSystemService(DOWNLOAD_SERVICE));
            AlertDialog.Builder builder = new AlertDialog.Builder(ProvaCartoesActivity.this);
            builder.setTitle("Por favor, Aguarde!")
                    .setMessage("Download em execução. Você será notificado quando o arquivo for baixado!");
            builder.setPositiveButton("OK", (dialog, which) -> {
                dialog.dismiss();
                finish();
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        } catch (Exception e) {
            Log.e("Kariti", e.toString());
        }
    }
    private void baixarCartoesV11(){
        try {
            DownloadCartoes downloadCartoes = new DownloadCartoes(filecsv, this, filePdf);
            downloadCartoes.baixarCartoesV11();
            AlertDialog.Builder builder = new AlertDialog.Builder(ProvaCartoesActivity.this);
            builder.setTitle("Por favor, Aguarde!")
                    .setMessage("Download em execução. Você será notificado quando o arquivo for baixado!");
            builder.setPositiveButton("OK", (dialog, which) -> {
                dialog.dismiss();
                finish();
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } catch (Exception e) {
            Log.e("Kariti", e.toString());
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) { // Verifica se o código de solicitação é o esperado
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisão concedida com sucesso", Toast.LENGTH_SHORT).show();
                Log.d("Permissão", "Permissão WRITE_EXTERNAL_STORAGE concedida.");
                baixarCartoesV9();
            } else {
                // Permissão negada
                Log.d("Permissão", "Permissão WRITE_EXTERNAL_STORAGE negada.");
                permissaoNegada();
                // Informe ao usuário que a permissão é necessária ou tome uma ação adequada
            }
        }
    }public void permissaoNegada(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ATENÇÃO");
        builder.setMessage("Para realizar o download dos cartões resposta em seu dispositivo, é necessário que conceda permissão ao Kariti! .");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    private File criarDiretorio(){
        File filecsv = new File(getCacheDir(), "dadosProva.csv");
        if (!filecsv.exists()) {
            try {
                // Tenta criar o arquivo
                if (filecsv.createNewFile()) {
                    Log.e("kariti","Diretorio criado");
                } else {
                    Log.i("kariti", "Arquivo já existe.");
                }
            } catch (IOException e) {
                Log.e("kariti", "Erro ao criar diretorio!");
            }
        }
        Log.e("kariti","Caminho: "+filecsv);
        return filecsv;
    }
    private String dataHoraAtual(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        Date date = new Date();
        return sdf.format(date);
    }
    private void solicitaPermissao(){
        if (ContextCompat.checkSelfPermission(ProvaCartoesActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ProvaCartoesActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }else{
            baixarCartoesV9();
        }
    }
}