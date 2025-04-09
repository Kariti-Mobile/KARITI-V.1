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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
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

public class VisualProvaCorrigidaActivity extends AppCompatActivity {
    ImageButton voltar;
    Button btnBaixarCartoes;
    ArrayList<String> listaAlunos, respostasDadas, gabarito;
    ArrayList<Integer> listaIdAlunos;
    Integer id_prova, id_aluno, qtdQuestoesProva;
    String nomeProva, nomeTurma, filePdf;
    TextView txtProva;
    List<String[]> dadosProvaCorrigida;
    ArrayList<Float> peso;
    TextView titulo;
    private File filecsv;

    BancoDados bancoDados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_prova_corrigida);

        voltar = findViewById(R.id.imgBtnVoltar);
        btnBaixarCartoes = findViewById(R.id.buttonBaixarResultado);
        txtProva = findViewById(R.id.textViewProvaResult);
        titulo = findViewById(R.id.toolbar_title);

        bancoDados = new BancoDados(this);
        listaAlunos = new ArrayList<>();

        titulo.setText(String.format("%s","Provas Corrigidas"));

        id_prova = getIntent().getExtras().getInt("id_prova");
        nomeProva = getIntent().getExtras().getString("prova");
        nomeTurma = getIntent().getExtras().getString("turma");

        txtProva.setText(String.format("%s","Prova: "+nomeProva));

        listaIdAlunos = (ArrayList<Integer>) bancoDados.listarIdsAlunosPorProvaCorrigida(id_prova); //pega todos os alunos com provas corrigidas
        qtdQuestoesProva = bancoDados.pegarQtdQuestoes(id_prova.toString());
        peso = (ArrayList<Float>) bancoDados.listarNotasPorQuestao(id_prova);
        gabarito = (ArrayList<String>) bancoDados.listarRespostasGabarito(id_prova); // lista as respostas do gabarito em formato de letras

        if (qtdQuestoesProva == null || gabarito == null || peso == null || listaIdAlunos == null){
            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente 7", Toast.LENGTH_SHORT).show();
            finish();
        }

        ShapeDrawable border = new ShapeDrawable(new RectShape());
        border.getPaint().setColor(0xFF000000); // Cor da borda
        border.getPaint().setStrokeWidth(1); // Largura da borda
        border.getPaint().setStyle(Paint.Style.STROKE);

        for(int id : listaIdAlunos) { // interage sob esses alunos
            float nota = 0;
            int acertos = 0;
            id_aluno = id;
            Boolean verificaCorrecao = bancoDados.verificaSituacaoCorrecao(id_prova, id_aluno, -1);
            if(verificaCorrecao == null){
                Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente 8", Toast.LENGTH_SHORT).show();
                finish();
            }
            respostasDadas = (ArrayList<String>) bancoDados.listarRespostasDadas(id_prova, id_aluno); // lista as respostas dos alunos em formato de letras
            incrementaRespostas(); //caso quantidade de respostadas dadas, menor que o esperado, incrementa!
            if(!verificaCorrecao) {
                for (int i = 0; i < qtdQuestoesProva; i++) {
                    if (gabarito.get(i).equals(respostasDadas.get(i))) {
                        nota += peso.get(i);
                        acertos += 1;
                    }
                }
            }else{
                nota = -1;
                acertos = -1;
            }

            TableLayout tableLayout = findViewById(R.id.tableLayout);
            TableRow row = new TableRow(this);
            row.setBackground(border);
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(layoutParams);

            // Cria uma célula para a nova linha para armazenar nome do aluno
            TextView cell1 = new TextView(this);
            String nomeAluno = bancoDados.pegaNomeAluno(id_aluno);
            if (nomeAluno == null){
                Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente 9", Toast.LENGTH_SHORT).show();
                finish();
            }
            String nomeAlunoEdit = editaNomeAluno(nomeAluno);
            cell1.setText(String.format("  %s", nomeAlunoEdit));
            //cell1.setGravity(Gravity.CENTER);
            row.addView(cell1);

            // Cria outra célula para a nova linha para armazenar o total de acertos do aluno na prova
            TextView cell2 = new TextView(this);
            if(acertos != -1){
                cell2.setText(String.valueOf(acertos));
            }else{
                cell2.setText("-");}
            cell2.setGravity(Gravity.CENTER);
            cell2.setTextSize(16);
            row.addView(cell2);

            // Cria outra célula para a nova linha para armazenar a nota total do aluno
            TextView cell3 = new TextView(this);
            if(nota != -1){
                cell3.setText(String.valueOf(nota));
            }else{
                cell3.setText("-");
            }
            cell3.setGravity(Gravity.CENTER);
            cell3.setTextSize(16);
            row.addView(cell3);

            // Cria outra célula para a nova linha com botão para exibir detalhamento da nota do aluno
            Button cell4 = new Button(this);
            cell4.setId(id_aluno);
            cell4.setText(String.format("%s","VER"));
            cell4.setGravity(Gravity.CENTER);
            cell4.setPadding(0,0,0,0);
            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT, // Largura ajustável ao conteúdo
                    TableRow.LayoutParams.WRAP_CONTENT  // Altura ajustável ao conteúdo
            );
            cell4.setLayoutParams(params);
            row.addView(cell4);

            // Adiciona a nova linha à tabela
            tableLayout.addView(row);

            cell4.setOnClickListener(v -> {
                Boolean verificaCorrecao2 = bancoDados.verificaSituacaoCorrecao(id_prova, v.getId(), -1);
                if(verificaCorrecao2 == null){
                    Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente 8", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!verificaCorrecao2){
                    Intent intent = new Intent(getApplicationContext(), DetalheCorrecao.class);
                    intent.putExtra("id_aluno", v.getId());
                    intent.putExtra("id_prova", id_prova);
                    startActivity(intent);
                }else{
                    informeProvaNaoCorrigida();
                }

            });
        }
        btnBaixarCartoes.setOnClickListener(v -> {
            try {
                dadosProvaCorrigida = new ArrayList<>();
                String prof = bancoDados.pegarNomeUsuario();
                String qtdAlternativas = String.valueOf(bancoDados.pegarQtdAlternativas(id_prova.toString()));
                String nota = String.valueOf(bancoDados.pegarNotaProva(id_prova.toString()));
                String dataProva = bancoDados.pegarDataProva(id_prova.toString());
                for(int id_aluno: listaIdAlunos) {
                    if(!bancoDados.verificaSituacaoCorrecao(id_prova, id_aluno, -1)) {
                        String nomeAluno = bancoDados.pegaNomeAluno(id_aluno);
                        String respostasDadas = bancoDados.listarRespostasDadasNumero(id_prova, id_aluno);
                        //respostasDadas = respostasDadas.replaceAll("(?<=\\d)(?=\\d)", ",");
                        String respostasEsperadas = bancoDados.listarRespostasGabaritoNumerico(id_prova.toString());
                        respostasEsperadas = respostasEsperadas.replaceAll("(?<=\\d)(?=\\d)", ",");
                        String notasQuestoes = bancoDados.listarNotasProva(id_prova.toString());
                        notasQuestoes = notasQuestoes.replaceAll("(?<=\\d)(?=\\d)", ",");
                        dadosProvaCorrigida.add(new String[]{id_prova.toString(), nomeProva, prof, nomeTurma, dataProva, qtdQuestoesProva.toString(), qtdAlternativas, nota, respostasDadas, respostasEsperadas, String.valueOf(id_aluno), nomeAluno, notasQuestoes});
                    }
                }
                try {
                    filecsv  = criarDiretorio();
                    filePdf = "Corrigida_" + nomeProva + dataHoraAtual() + ".pdf";
                    GerarCsv.gerar(dadosProvaCorrigida, filecsv);// Gerando e salvando arquivo.csv
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                        solicitaPermissao();
                    }else {
                        baixarCartoesV11();
                    }
                } catch (IOException e) {
                    Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente 8", Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente 8", Toast.LENGTH_SHORT).show();
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
    private String editaNomeAluno(String aluno){
        String[] separa = aluno.trim().split("\\s+");
        //String novoNome = "";
        if(separa.length > 2) {
            return separa[0] + " " + separa[separa.length - 1];
        }else{
            return aluno;
        }
    }
    private void incrementaRespostas(){
        int numRespostas = respostasDadas.size(); //Quantidade de respostas cadastradas no BD
        if (numRespostas < qtdQuestoesProva){ //Caso quantidade de respostas dadas menor q de questões
            for (int a = numRespostas; a < qtdQuestoesProva; a++){
                respostasDadas.add(a, "-"); //Aumenta o tamanho da lista até o tamanho da questões
            }
        }
    }
    public void PopMenu(View v){
        v.setOnClickListener(view -> Toast.makeText(VisualProvaCorrigidaActivity.this, "Preparado para implementação", Toast.LENGTH_SHORT).show());
    }
    private File criarDiretorio(){
        File filecsv = new File(getCacheDir(), "dadosCorrecao.csv");
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

    public void informeProvaNaoCorrigida(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ATENÇÃO");
        builder.setMessage("Esta prova não foi corrigida. Veja algumas das causas que podem ter colaborado para este resultado:\n\n" +
                "• Fundo da imagem com ruidos ou diferente do padrão uniforme \n\n" +
                "• Cartão resposta não estava visível totalmente na imagem\n\n" +
                "• Ambiente com pouca luminosidade\n\n" +
                "• imagem ofuscada\n\n" +
                "• Cartão resposta Rasurado\n\n" +
                "Para ter melhor resultado na correção é essencial que sejam seguidas as orientações destacadas na fase de correção!");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    private String dataHoraAtual(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        Date date = new Date();
        return sdf.format(date);
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
    }
    public void permissaoNegada(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ATENÇÃO");
        builder.setMessage("Para realizar o download do resultado de correção em seu dispositivo, é necessário que conceda permissão ao Kariti! .");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    private void baixarCartoesV9(){
        try {
            DownloadResultadoCorrecao downloadResultadoCorrecao = new DownloadResultadoCorrecao(filecsv, this, filePdf);
            File fSaida = new File(getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filePdf);
            downloadResultadoCorrecao.solicitarResultadoCorrecao(new FileOutputStream(fSaida), fSaida, (DownloadManager) getSystemService(DOWNLOAD_SERVICE));
            AlertDialog.Builder builder = new AlertDialog.Builder(VisualProvaCorrigidaActivity.this);
            builder.setTitle("Por favor, Aguarde!")
                    .setMessage("Download em execução. Você será notificado quando o arquivo estiver baixado.");
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
            DownloadResultadoCorrecao downloadResultadoCorrecao = new DownloadResultadoCorrecao(filecsv, this, filePdf);
            downloadResultadoCorrecao.baixarResultadoCorrecao11();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
    private void solicitaPermissao(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }else{
            baixarCartoesV9();
        }
    }

}