package online.padev.kariti;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GabaritoActivity extends AppCompatActivity {
    TextView txtViewNotaProva, txtViewProva, txtViewTurma, txtViewData;
    Button btnCadastrarProva;
    ImageButton voltar, iconAjuda;
    BancoDados bancoDados;
    LinearLayout layoutHorizontal;
    String prova, turma, data, dataForm, status;
    Integer id_turma, id_prova, quest, alter;
    TextView titulo;
    Map<String, Object> info;
    List<RadioGroup> listRadioGroups;
    HashMap<Integer, Integer> alternativasEscolhidas;
    ArrayList<Float> notasPorQuestao;
    Boolean situacao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gabarito);


        voltar = findViewById(R.id.imgBtnVoltaDescola);
        iconAjuda = findViewById(R.id.iconHelp);
        titulo = findViewById(R.id.toolbar_title);
        btnCadastrarProva = findViewById(R.id.btnCadProva);
        txtViewProva = findViewById(R.id.textViewProva);
        txtViewTurma = findViewById(R.id.textViewTurma);
        txtViewData = findViewById(R.id.textViewData);
        txtViewNotaProva = findViewById(R.id.txtViewNotaProva);
        layoutHorizontal = findViewById(R.id.layoutHorizontalAlternat);

        bancoDados = new BancoDados(this);
        info = new HashMap<>();
        listRadioGroups = new ArrayList<>();
        alternativasEscolhidas = new HashMap<>();

        titulo.setText(String.format("%s","Gabarito"));


        prova = Objects.requireNonNull(getIntent().getExtras()).getString("nomeProva");
        turma = getIntent().getExtras().getString("turma");
        id_turma = getIntent().getExtras().getInt("id_turma");
        data = getIntent().getExtras().getString("data");
        dataForm = getIntent().getExtras().getString("dataForm");
        quest = getIntent().getExtras().getInt("quest");
        alter = getIntent().getExtras().getInt("alter");
        status = getIntent().getExtras().getString("status");
        if(status.equals("atualizacao")){
            id_prova = getIntent().getExtras().getInt("id_prova");
            btnCadastrarProva.setText(String.format("%s","Salvar"));
        }
        txtViewProva.setText(String.format("Prova: %s", prova));
        txtViewTurma.setText(String.format("Turma: %s", turma));
        txtViewData.setText(String.format("Data: %s", data));

        btnCadastrarProva.setOnClickListener(v -> {
            btnCadastrarProva.setEnabled(false);
            boolean respostaSelecionada = false;
            boolean respostasNotasPreenchidas = true;
            for (RadioGroup radioGroup : listRadioGroups) {
                if (radioGroup.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(GabaritoActivity.this, "Por favor, selecione uma resposta para todas as questões.", Toast.LENGTH_SHORT).show();
                    respostaSelecionada = false;
                    break;
                } else {
                    respostaSelecionada = true;
                }
            }
            // Verifica se todos os campos de notas foram preenchidos
            for (int j = 0; j < layoutHorizontal.getChildCount(); j++) {
                LinearLayout questaoLayout = (LinearLayout) layoutHorizontal.getChildAt(j);
                EditText pontosEditText = (EditText) questaoLayout.getChildAt(2);
                String nt = pontosEditText.getText().toString();
                if (nt.isEmpty()) {
                    Toast.makeText(GabaritoActivity.this, "Por favor, preencha todas as notas para as questões.", Toast.LENGTH_SHORT).show();
                    respostasNotasPreenchidas = false;
                    break;
                }
            }
            if (respostaSelecionada && respostasNotasPreenchidas) {
                if(status.equals("novaProva")) {
                    id_prova = bancoDados.cadastrarProva(prova, dataForm, quest, alter, id_turma);
                    if(id_prova == null || id_prova == -1){
                        Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }else{
                    if(bancoDados.deletarGabarito(id_prova)){
                        if(!bancoDados.alterarDadosProva(id_prova, prova, dataForm, id_turma, quest, alter)){
                            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }else{
                        Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                notasPorQuestao = (ArrayList<Float>) info.get("notaQuest");
                if(!notasPorQuestao.isEmpty() && id_prova != null){
                    situacao = true;
                    for(int i = 1; i <= quest; i++){
                        Integer resp = alternativasEscolhidas.get(i-1);
                        if(!bancoDados.cadastrarGabarito(id_prova, i, resp+1, notasPorQuestao.get(i-1))){
                            Log.e("kariti","Erro ao tentar cadastrar resposta da questao "+i+" prova "+id_prova);
                            situacao = false;
                        }
                    }
                    if (situacao){
                        dialogProvaSucess();
                    } else {
                        avisoErroDeCadastro();
                    }
                }
            }
       });
        int quantidadeQuestoes = quest;
        int quantidadeAlternativas = alter;
        txtViewNotaProva.setText("Nota total da prova " + quantidadeQuestoes + " pontos.");

        String[] letras = new String[quantidadeAlternativas];
        for (int i = 0; i < quantidadeAlternativas; i++) {
            char letra = (char)('A' + i);
            letras[i] = String.valueOf(letra);
        }

        //Questões e Radio
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        for (int i = 0; i < quantidadeQuestoes; i++) {
            LinearLayout layoutQuestao = new LinearLayout(this);
            layoutQuestao.setOrientation(LinearLayout.HORIZONTAL);

            TextView textViewNumeroQuestao = new TextView(this);
            textViewNumeroQuestao.setText((i + 1) + " ");
            layoutQuestao.addView(textViewNumeroQuestao);

            //Agrupar os RadioButtons
            RadioGroup radioGroupAlternativas = new RadioGroup(this);
            radioGroupAlternativas.setOrientation(LinearLayout.HORIZONTAL);
            listRadioGroups.add(radioGroupAlternativas);

            // Loop para criar Radio para as respostas
            for (int j = 0; j < quantidadeAlternativas; j++) {
                params.setMargins(0, 20, 20, 0);

                RadioButton radioAlternativa = new RadioButton(this);
                radioAlternativa.setLayoutParams(params);
                radioAlternativa.setText(letras[j]);
                radioGroupAlternativas.addView(radioAlternativa);
            }
            radioGroupAlternativas.setOnCheckedChangeListener((group, checkedId) -> {
                for (int i12 = 0; i12 < listRadioGroups.size(); i12++) {
                    if (listRadioGroups.get(i12) == group) {
                        int positionDaQuestao = i12;
                        int selectedRadioButtonId = group.getCheckedRadioButtonId();
                        RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
                        int position = group.indexOfChild(selectedRadioButton);
                        alternativasEscolhidas.put(positionDaQuestao, position);
                        break;
                    }
                }
            });
            layoutQuestao.addView(radioGroupAlternativas);

            LinearLayout.LayoutParams paramsText = new LinearLayout.LayoutParams(
                    150,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            EditText editTextPontos = new EditText(this);
            editTextPontos.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editTextPontos.setText(String.valueOf(1));
            editTextPontos.setGravity(Gravity.CENTER);
            editTextPontos.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            editTextPontos.setBackground(ContextCompat.getDrawable(this, R.drawable.borda_fina));
            paramsText.setMargins(5, 15, 0, 0);

            editTextPontos.setLayoutParams(paramsText);

            layoutQuestao.addView(editTextPontos);

            editTextPontos.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }
                @Override
                public void afterTextChanged(Editable editable) {
                    calcularNotaTotal();
                }
            });

            layoutHorizontal.addView(layoutQuestao);
            calcularNotaTotal();

        }
        iconAjuda.setOnClickListener(v -> dialogHelpDetalhes());
        voltar.setOnClickListener(view -> avisoVoltar());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                avisoVoltar();
            }
        });

    }
    public void dialogProvaSucess(){
        AlertDialog.Builder builder = new AlertDialog.Builder(GabaritoActivity.this);
        builder.setTitle("Prova cadastrada com sucesso!")
                .setMessage("Selecione uma das opções a seguir, para ter acesso aos Cartões Resposta.")
                .setPositiveButton("OK", (dialog, which) -> {
                    finish();
                    baixarCartoes();
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    public void avisoErroDeCadastro(){
        AlertDialog.Builder builder = new AlertDialog.Builder(GabaritoActivity.this);
        builder.setTitle("AVISO!")
                .setMessage("Falha no cadastro do gabarito!")
                .setPositiveButton("Sair", (dialog, which) -> finish());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    public void baixarCartoes() {
        Intent intent = new Intent(this, ProvaCartoesActivity.class);
        intent.putExtra("prova", prova);
        intent.putExtra("id_turma", id_turma);
        intent.putExtra("endereco", 1);
        startActivity(intent);
    }
    private void calcularNotaTotal() {
        float notas = 0;
        ArrayList<Float> nPquest = new ArrayList<>();
        info.put("notaQuest", nPquest);
        //modificado
        for (int j = 0; j < layoutHorizontal.getChildCount(); j++) {
            LinearLayout questaoLayout = (LinearLayout) layoutHorizontal.getChildAt(j);
            EditText pontosEditText = (EditText) questaoLayout.getChildAt(2);
            String nota = pontosEditText.getText().toString();
            if(nota.isEmpty() || nota.charAt(0) == '.'){
                nota = "0"+nota;
            }
            if (!nota.isEmpty()) {
                float n = Float.parseFloat(nota);
                nPquest.add(n);
                notas += n;
            }
        }
        txtViewNotaProva.setText(String.format("Nota total da prova %.2f pontos.", notas));
    }
    public void dialogHelpDetalhes() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajuda");
        builder.setMessage("olá, agora é hora de preencher o gabarito da sua prova.\n" +
                "• Marque as respostas correspondentes as questões da prova\n" +
                "• Informe o peso de cada questão nos campos sugeridos \n\n" +
                "• Antes de finalizar o cadastro confira todos os dados! ");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    public void avisoVoltar(){
        AlertDialog.Builder builder = new AlertDialog.Builder(GabaritoActivity.this);
        builder.setTitle("ATENÇÃO!")
                .setMessage("Ao confirmar essa ação, os dados dessa prova serão perdidos!\n\n" +
                        "Deseja realmente voltar?")
                .setPositiveButton("SIM", (dialog, which) -> finish())
                .setNegativeButton("NÃO", (dialog, which) -> {
                    //CONTINUE
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
