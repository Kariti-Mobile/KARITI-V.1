package online.padev.kariti;

import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Objects;

public class DetalheCorrecao extends AppCompatActivity {

    ImageButton voltar;
    String nomeAluno, status;
    Integer id_aluno, id_prova, qtdQuestoes;
    BancoDados bancoDados;
    TextView alunoDetalhe, notaTotal;
    ArrayList<String> respostasDadas, gabarito;
    ArrayList<Float> peso;
    TextView titulo;
    float nota = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_correcao);

        voltar = findViewById(R.id.imgBtnVoltar);
        alunoDetalhe  = findViewById(R.id.textViewDetalheAluno);
        notaTotal = findViewById(R.id.textViewNotaTotalDetalhe);
        titulo = findViewById(R.id.toolbar_title);

        bancoDados = new BancoDados(this);

        titulo.setText(String.format("%s","Detalhes"));

        id_aluno = Objects.requireNonNull(getIntent().getExtras()).getInt("id_aluno");
        id_prova = getIntent().getExtras().getInt("id_prova");
        nomeAluno = bancoDados.pegaNomeAluno(id_aluno);
        qtdQuestoes = bancoDados.pegarQtdQuestoes(id_prova.toString());

        if (nomeAluno == null || qtdQuestoes == null){ //vericação caso ocorra exceções no Banco
            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
            finish();
        }

        alunoDetalhe.setText(nomeAluno);
        //Carrega todas as respostas ordenadas por questao
        respostasDadas = (ArrayList<String>) bancoDados.listarRespostasDadas(id_prova, id_aluno); // lista as respostas dos alunos em formato de letras
        gabarito = (ArrayList<String>) bancoDados.listarRespostasGabarito(id_prova); // lista as respostas do gabarito em formato de letras
        peso = (ArrayList<Float>) bancoDados.listarNotasPorQuestao(id_prova);

        if (respostasDadas == null || gabarito == null || peso == null){ //vericação caso ocorra exceções no Banco
            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
            finish();
        }

        ShapeDrawable border = new ShapeDrawable(new RectShape());
        border.getPaint().setColor(0xFF000000); // Cor da borda
        border.getPaint().setStrokeWidth(1); // Largura da borda
        border.getPaint().setStyle(Paint.Style.STROKE);

        int numRespostas = respostasDadas.size(); //Quantidade de respostas cadastradas no BD
        if (numRespostas < qtdQuestoes){ //Caso quantidade de respostas dadas menor q de questões
            for (int a = numRespostas; a < qtdQuestoes; a++){
                respostasDadas.add(a, "-"); //Aumenta o tamanho da lista até o tamanho da questões
            }
        }

        for(int x = 1; x <= qtdQuestoes; x++) {
            if(gabarito.get(x-1).equals(respostasDadas.get(x-1))){
                nota += peso.get(x-1);
                status = "CERTA";
            }else {status = "ERRADA";}

            TableLayout tableLayout = findViewById(R.id.tableLayoutDetalheCorrecao);
            TableRow row = new TableRow(this);
            row.setBackground(border);
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(layoutParams);

            // Cria uma célula para a nova linha para armazenar a questão
            TextView cell1 = new TextView(this);
            cell1.setText(String.valueOf(x));
            cell1.setGravity(Gravity.CENTER);
            cell1.setTextSize(16);
            row.addView(cell1);

            // Cria outra célula para a nova linha para armazenar a resposta marcada pelo aluno
            TextView cell2 = new TextView(this);
            cell2.setText(respostasDadas.get(x-1));
            cell2.setGravity(Gravity.CENTER);
            cell2.setTextSize(16);
            row.addView(cell2);

            // Cria uma célula para a nova linha para armazenar a resposta do gabarito
            TextView cell3 = new TextView(this);
            cell3.setText(gabarito.get(x-1));
            cell3.setGravity(Gravity.CENTER);
            cell3.setTextSize(16);
            row.addView(cell3);

            // Cria outra célula para a nova linha para armazenar o status de acertos do aluno
            TextView cell4 = new TextView(this);
            cell4.setText(status);
            cell4.setTextSize(14);
            cell4.setGravity(Gravity.CENTER);
            row.addView(cell4);

            // Cria outra célula para a nova linha para armazenar o peso da questão
            TextView cell5 = new TextView(this);
            cell5.setText(String.valueOf(peso.get(x-1)));
            cell5.setGravity(Gravity.CENTER);
            cell5.setTextSize(16);
            row.addView(cell5);

            // Adiciona a nova linha à tabela
            tableLayout.addView(row);
        }
        notaTotal.setText(String.format("Nota total obtida: %.2f pontos", nota));


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
}