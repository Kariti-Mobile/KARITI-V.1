package online.padev.kariti;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class CadProvaActivity extends AppCompatActivity {
    Button datePickerButton;
    Calendar calendar;
    EditText nomeProva;
    TextView qtdQuest, qtdAlter;
    Button btnGerProva;
    Spinner spinnerTurma;
    BancoDados bancoDados;
    ArrayList<String> listTurmaEmProva;
    ImageButton voltar, questMenos, questMais, altMais, altMenos;
    String dataform;
    Integer id_turma;
    TextView titulo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cad_prova);

        datePickerButton = findViewById(R.id.datePickerButton);
        voltar = findViewById(R.id.imgBtnVoltar);
        btnGerProva = findViewById(R.id.btnGerarProva);
        nomeProva = findViewById(R.id.editTextNomeProva);
        qtdQuest = findViewById(R.id.textViewQuantity);
        qtdAlter = findViewById(R.id.textVieAlter);
        questMais = findViewById(R.id.imageButtonMaisQuest);
        questMenos = findViewById(R.id.imageButtonMenosQuest);
        altMais = findViewById(R.id.imgBtnMaisAlter);
        altMenos = findViewById(R.id.imgBtnMenoAlter);
        spinnerTurma = findViewById(R.id.spinnerTurmaPprova);
        titulo = findViewById(R.id.toolbar_title);

        bancoDados = new BancoDados(this);

        titulo.setText(String.format("%s","Nova Prova"));

        listTurmaEmProva = (ArrayList<String>) bancoDados.listarNomesTurmas(); //Obtem a lista das turmas delimitadas por escola
        if(listTurmaEmProva == null){
            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
            return;
        }
        listTurmaEmProva.add(0, "Selecione a Turma");
        SpinnerAdapter adapter = new SpinnerAdapter(this, listTurmaEmProva);
        spinnerTurma.setAdapter(adapter);

        questMais.setOnClickListener(v -> {
            int quest = Integer.parseInt(qtdQuest.getText().toString());
            if(quest < 20)
                quest ++;
            qtdQuest.setText(String.valueOf(quest));
        });
        questMenos.setOnClickListener(v -> {
            int quest = Integer.parseInt(qtdQuest.getText().toString());
            if(quest > 0)
                quest --;
            qtdQuest.setText(String.valueOf(quest));
        });
        altMais.setOnClickListener(v -> {
            int alter = Integer.parseInt(qtdAlter.getText().toString());
            if(alter < 7)
                alter ++;
            qtdAlter.setText(String.valueOf(alter));
        });
        altMenos.setOnClickListener(v -> {
            int alter = Integer.parseInt(qtdAlter.getText().toString());
            if(alter > 0)
                alter --;
            qtdAlter.setText(String.valueOf(alter));
        });
        btnGerProva.setOnClickListener(v -> {
            String data = datePickerButton.getText().toString();
            String prova = nomeProva.getText().toString();
            Integer quest = Integer.valueOf(qtdQuest.getText().toString());
            Integer alter = Integer.valueOf(qtdAlter.getText().toString());
            String turma = spinnerTurma.getSelectedItem().toString();
            if(prova.trim().isEmpty()){
                Toast.makeText(CadProvaActivity.this, "Informe o nome da prova!", Toast.LENGTH_SHORT).show();
                return;
            }
            if(spinnerTurma.getSelectedItem() == "Selecione a Turma"){
                Toast.makeText(CadProvaActivity.this, "Selecione uma turma!", Toast.LENGTH_SHORT).show();
                return;
            }
            if(data.equals("Selecionar Data")){
                Toast.makeText(CadProvaActivity.this, "Selecione uma data!", Toast.LENGTH_SHORT).show();
                return;
            }
            if(quest.equals(0)){
                Toast.makeText(CadProvaActivity.this, "Informe a quantidade de questões!", Toast.LENGTH_SHORT).show();
                return;
            }
            if(alter.equals(0)){
                Toast.makeText(CadProvaActivity.this, "Informe a quantidade de alternativas!", Toast.LENGTH_SHORT).show();
                return;
            }
            id_turma = bancoDados.pegarIdTurma(turma);
            if(id_turma == null || id_turma == -1){
                Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                return;
            }
            Boolean verificaProva = bancoDados.verificaExisteProvaPNome(prova, id_turma.toString());
            if(verificaProva == null){
                Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                return;
            }
            if(verificaProva) {
                Toast.makeText(CadProvaActivity.this, "Esta turma já pussui uma prova cadastrada com o nome, "+prova, Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getApplicationContext(), GabaritoActivity.class);
            intent.putExtra("nomeProva", prova);
            intent.putExtra("turma", turma);
            intent.putExtra("id_turma", id_turma);
            intent.putExtra("data", data);
            intent.putExtra("dataForm", dataform);
            intent.putExtra("quest", quest);
            intent.putExtra("alter", alter);
            intent.putExtra("status", "novaProva");
            startActivity(intent);
            finish();
        });
        // Obtém a instância do calendário com a data atual
        calendar = Calendar.getInstance();
        datePickerButton.setOnClickListener(v -> {
            // Cria um DatePickerDialog com a data atual
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    CadProvaActivity.this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        // Atualiza a data no calendário quando o usuário seleciona uma nova data
                        calendar.set(year, monthOfYear, dayOfMonth);
                        // Atualiza o texto do botão com a data selecionada
                        datePickerButton.setText(formatDate(calendar));
                        dataform = formatDateBanco(calendar);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            // Exibe o DatePickerDialog
            datePickerDialog.show();
        });
        voltar.setOnClickListener(v -> getOnBackPressedDispatcher());
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
}