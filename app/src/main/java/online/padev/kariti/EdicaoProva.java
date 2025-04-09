package online.padev.kariti;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class EdicaoProva extends AppCompatActivity {
    private EditText nomeProva;
    private TextView questoesAtuais, alternativas;
    private Spinner spinnerTurma;
    private ImageButton menosQuestoes, maisQuestoes, menosAlternativas, maisAlternativas, voltar;
    private Button proximo, dataAtual;
    private Calendar calendar;
    private String dataFormatada, dataBD, provaBD, turmaBD, novaProva, novaTurma, novaData;
    private Integer id_provaBD, id_turmaBD, novaQuestao, novaAlternativa;
    private TextView titulo;
    BancoDados bancoDados;
    private Integer qtdQuestoesBD, qtdAlternativasBD;
    private ArrayList<String> listTurma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edicao_prova);

        voltar = findViewById(R.id.imgBtnVoltar);
        dataAtual = findViewById(R.id.NovaData);
        nomeProva = findViewById(R.id.EdicaoProva);
        spinnerTurma = findViewById(R.id.EdicaoTurma);
        titulo = findViewById(R.id.toolbar_title);
        questoesAtuais = findViewById(R.id.qtdQuestoes);
        alternativas = findViewById(R.id.qtdAlternativas);
        menosQuestoes = findViewById(R.id.menosquest);
        maisQuestoes = findViewById(R.id.maisquest);
        menosAlternativas = findViewById(R.id.menosAlter);
        maisAlternativas = findViewById(R.id.maisAlter);
        proximo = findViewById(R.id.btnEditProximo);

        titulo.setText("Edição");

        bancoDados = new BancoDados(this);

        id_provaBD = Objects.requireNonNull(getIntent().getExtras()).getInt("id_prova");
        provaBD = getIntent().getExtras().getString("prova");
        id_turmaBD = getIntent().getExtras().getInt("id_turma");
        turmaBD = bancoDados.pegarNomeTurma(id_turmaBD.toString());
        qtdQuestoesBD = bancoDados.pegarQtdQuestoes(id_provaBD.toString());
        qtdAlternativasBD = bancoDados.pegarQtdAlternativas(id_provaBD.toString());
        dataBD = bancoDados.pegarDataProva(id_provaBD.toString());

        this.dataBD = formataDataCadastrada(dataBD);

        nomeProva.setText(provaBD);
        questoesAtuais.setText(String.valueOf(qtdQuestoesBD));
        alternativas.setText(String.valueOf(qtdAlternativasBD));
        dataAtual.setText(dataBD);

        listTurma = (ArrayList<String>) bancoDados.listarNomesTurmas();
        listTurma.add(0, turmaBD);
        SpinnerAdapter adapter = new SpinnerAdapter(this, listTurma);
        spinnerTurma.setAdapter(adapter);

        maisQuestoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quest = Integer.parseInt(questoesAtuais.getText().toString());
                if(quest < 20)
                    quest ++;
                questoesAtuais.setText(String.valueOf(quest));
            }
        });
        menosQuestoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quest = Integer.parseInt(questoesAtuais.getText().toString());
                if(quest > 0)
                    quest --;
                questoesAtuais.setText(String.valueOf(quest));
            }
        });
        maisAlternativas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int alter = Integer.parseInt(alternativas.getText().toString());
                if(alter < 7)
                    alter ++;
                alternativas.setText(String.valueOf(alter));
            }
        });
        menosAlternativas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int alter = Integer.parseInt(alternativas.getText().toString());
                if(alter > 0)
                    alter --;
                alternativas.setText(String.valueOf(alter));
            }
        });

        calendar = Calendar.getInstance();
        dataAtual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cria um DatePickerDialog com a data atual
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        EdicaoProva.this,
                        (view, year, monthOfYear, dayOfMonth) -> {
                            // Atualiza a data no calendário quando o usuário seleciona uma nova data
                            calendar.set(year, monthOfYear, dayOfMonth);
                            // Atualiza o texto do botão com a data selecionada
                            dataAtual.setText(formatDate(calendar));
                            dataFormatada = formatDateBanco(calendar);
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

                // Exibe o DatePickerDialog
                datePickerDialog.show();
            }
        });
        proximo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                novaProva = nomeProva.getText().toString();
                novaTurma = spinnerTurma.getSelectedItem().toString(); // nome da turma não tem como ser vazio!
                id_turmaBD = bancoDados.pegarIdTurma(novaTurma);
                novaData = dataAtual.getText().toString();
                novaQuestao = Integer.valueOf(questoesAtuais.getText().toString());
                novaAlternativa = Integer.valueOf(alternativas.getText().toString());
                if(!novaProva.trim().isEmpty()){ //verifica se o campo prova esta vazio
                    if(!novaProva.equals(provaBD) || !novaTurma.equals(turmaBD) || !novaData.equals(dataBD) || !novaQuestao.equals(qtdQuestoesBD) || !novaAlternativa.equals(qtdAlternativasBD)){
                        if(novaQuestao.equals(0) || novaAlternativa.equals(0)){
                            Toast.makeText(EdicaoProva.this, "Quantidade de questões e/ou alternativas, não podem ser igual a 0.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(!novaProva.equals(provaBD) || !novaTurma.equals(turmaBD)){
                            Boolean verificaProva = bancoDados.verificaExisteProvaPNome(novaProva, id_turmaBD.toString());
                            if(verificaProva == null){
                                Toast.makeText(EdicaoProva.this, "Erro na comunicação, tente novamente!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if(verificaProva) {
                                Toast.makeText(EdicaoProva.this, "Esta turma já pussui uma prova cadastrada com esse nome, " + novaProva, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        confirmeAlteracaoDados();
                    }else{
                        carregarTelaGabarito();
                    }
                }else{
                    Toast.makeText(EdicaoProva.this, "Informe o nome da Prova!", Toast.LENGTH_SHORT).show();
                }
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
    private String formatDate(Calendar calendar) {
        String dateFormat = "dd/MM/yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.getDefault());
        return simpleDateFormat.format(calendar.getTime());
    }
    private String formatDateBanco(Calendar calendar) {
        String dateFormat = "yyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.getDefault());
        return simpleDateFormat.format(calendar.getTime());
    }
    private String formataDataCadastrada(String data){
        String[] itens = data.split("-");
        String dataFor = itens[2]+"/"+itens[1]+"/"+itens[0];
        return dataFor;
    }
    private void confirmeAlteracaoDados(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ATENÇÃO")
                .setMessage("Confirma as alterações realizadas para esta prova? ")
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        carregarTelaGabarito();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void carregarTelaGabarito(){
        Intent intent = new Intent(getApplicationContext(), GabaritoActivity.class);
        intent.putExtra("id_prova", id_provaBD);
        intent.putExtra("nomeProva", novaProva);
        intent.putExtra("id_turma", id_turmaBD);
        intent.putExtra("turma", novaTurma);
        intent.putExtra("data", novaData);
        intent.putExtra("dataForm", dataFormatada);
        intent.putExtra("quest", novaQuestao);
        intent.putExtra("alter", novaAlternativa);
        intent.putExtra("status", "atualizacao");
        startActivity(intent);
        finish();
    }
}