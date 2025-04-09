package online.padev.kariti;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CadTurmaActivity extends AppCompatActivity{
    ImageButton voltar, iconAjudaCadturma;
    Toolbar toolbar;
    EditText nomeTurma, alunosAnonimos;
    ImageView menosAnonimos, maisAnonimos;
    ListView listarAlunosListView;
    Button btnCadastrarTurma;
    BancoDados bancoDados;
    Spinner spinnerAluno;
    String alunoSelecionado;
    Integer id_turma = 0;
    AdapterExclAluno adapterAlunos;
    TextView titulo;
    ArrayList<String> listadAlunos = new ArrayList<>(), nomesAluno;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cad_turma);

        toolbar = findViewById(R.id.myToolBarMenu);
        setSupportActionBar(toolbar);
        voltar = findViewById(R.id.imgBtnVoltaDescola);
        iconAjudaCadturma = findViewById(R.id.iconHelp);
        listarAlunosListView = findViewById(R.id.listViewCadTurma);
        titulo = findViewById(R.id.toolbar_title);

        titulo.setText(String.format("%s","Cadastro"));

        nomeTurma = findViewById(R.id.editTextTurmaCad);
        btnCadastrarTurma = findViewById(R.id.buttonCadastrarTurma);
        spinnerAluno = findViewById(R.id.spinnerBuscAluno);
        alunosAnonimos = findViewById(R.id.editTextAlunosAnonimos);
        menosAnonimos = findViewById(R.id.imageViewMenosAnonimos);
        maisAnonimos = findViewById(R.id.imageViewMaisAnonimos);

        bancoDados = new BancoDados(this);

        nomesAluno = (ArrayList<String>) bancoDados.listarNomesAlunos(1);
        nomesAluno.add(0, "Selecione os Alunos");
        nomesAluno.add(1, "Todos");
        SpinnerAdapter adapter = new SpinnerAdapter(this, nomesAluno);
        spinnerAluno.setAdapter(adapter);
        spinnerAluno.setSelection(0);

        iconAjudaCadturma.setOnClickListener(view -> dialogHelpDetalhes());
        spinnerAluno.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    alunoSelecionado = spinnerAluno.getSelectedItem().toString();
                    if(alunoSelecionado.equals("Todos")){
                        listadAlunos = (ArrayList<String>) bancoDados.listarNomesAlunos(1);
                        if(listadAlunos == null){
                            Toast.makeText(CadTurmaActivity.this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        adapterAlunos = new AdapterExclAluno(CadTurmaActivity.this, listadAlunos);
                        listarAlunosListView.setAdapter(adapterAlunos);
                        adapterAlunos.notifyDataSetChanged();
                        spinnerAluno.setSelection(1);
                    }else {
                        int i = 0;
                        for (String a : listadAlunos) {
                            if (alunoSelecionado.equals(a)) {
                                i = 1;
                                Toast.makeText(CadTurmaActivity.this, "Aluno já selecionado!", Toast.LENGTH_SHORT).show();
                                spinnerAluno.setSelection(0);
                                break;
                            }
                        }
                        if (i != 1) {
                            listadAlunos.add(alunoSelecionado);
                            adapterAlunos = new AdapterExclAluno(CadTurmaActivity.this, listadAlunos);
                            listarAlunosListView.setAdapter(adapterAlunos);
                            adapterAlunos.notifyDataSetChanged();
                            spinnerAluno.setSelection(0);
                        }
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        listarAlunosListView.setOnItemClickListener((adapterView, view, i, l) -> {
            listadAlunos.remove(i);
            adapterAlunos.notifyDataSetChanged();
            Toast.makeText(CadTurmaActivity.this, "Aluno removido! ", Toast.LENGTH_SHORT).show();
        });
        menosAnonimos.setOnClickListener(view -> {
            int menos = Integer.parseInt(alunosAnonimos.getText().toString());
            if(menos > 0)
                menos --;
            alunosAnonimos.setText(String.valueOf(menos));
        });
        maisAnonimos.setOnClickListener(view -> {
            int mais = Integer.parseInt(alunosAnonimos.getText().toString());
            mais ++;
            alunosAnonimos.setText(String.valueOf(mais));
        });

        btnCadastrarTurma.setOnClickListener(v -> {
            btnCadastrarTurma.setEnabled(false);
            try {
                String turma = nomeTurma.getText().toString();
                if(turma.trim().isEmpty()) {
                    Toast.makeText(CadTurmaActivity.this, "Informe o nome da turma!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (listadAlunos.isEmpty() && alunosAnonimos.getText().toString().equals("0")) {
                    aviso();
                    return;
                }
                Integer totAnonimos = Integer.valueOf(alunosAnonimos.getText().toString());
                Boolean verificaTurma = bancoDados.verificaExisteTurmaPorNome(turma);
                if (verificaTurma == null){
                    Toast.makeText(CadTurmaActivity.this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (verificaTurma) {
                    Toast.makeText(CadTurmaActivity.this, "Turma já cadastrada! ", Toast.LENGTH_SHORT).show();
                    return;
                }
                id_turma = bancoDados.cadastrarTurma(turma);
                if (id_turma == null || id_turma == -1) {
                    Toast.makeText(CadTurmaActivity.this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!listadAlunos.isEmpty()){
                    for (String aluno : listadAlunos) {
                        Integer id_aluno = bancoDados.pegarIdAluno(aluno);
                        if (id_aluno != null && id_aluno != -1){
                            if(bancoDados.cadastrarAlunoNaTurma(id_turma, id_aluno)){
                                Log.e("kariti","Aluno cadastrado na turma: "+id_turma);
                            }
                        }else Log.e("kariti","Erro ao tentar cadastrar na turma o aluno: "+aluno);
                    }
                }
                if (!totAnonimos.equals(0)){
                    int tamanho = String.valueOf(totAnonimos).length();
                    for (int x = 1; x <= totAnonimos; x++) {
                        String anonimo = "Aluno "+ String.format("%0"+tamanho+"d",x);
                        Integer id_anonimo = bancoDados.cadastrarAluno(anonimo, null, 0);
                        if(id_anonimo != -1){
                            if(bancoDados.cadastrarAlunoNaTurma(id_turma, id_anonimo)){
                                Log.e("kariti","Aluno anônimo cadastrado na turma: "+id_turma);
                            }
                        }else  Log.e("kariti","Erro ao tentar cadastrar anônimo: "+x);
                    }
                }
                Toast.makeText(CadTurmaActivity.this, "Turma cadastrada com Sucesso", Toast.LENGTH_SHORT).show();
                finish();
            }catch (Exception e){
                Toast.makeText(this, "Erro: turma não cadastrada corretamente!!", Toast.LENGTH_SHORT).show();
                finish();
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
    public void aviso(){
        AlertDialog.Builder builder = new AlertDialog.Builder(CadTurmaActivity.this);
        builder.setTitle("Atenção!")
                .setMessage("Não é possível cadastrar uma turma sem alunos. Por favor, selecione os alunos para essa turma ou, caso preferir, informe a quantidade de alunos anônimos! ")
                .setPositiveButton("OK", (dialog, which) -> Toast.makeText(CadTurmaActivity.this, "Selecione os alunos!", Toast.LENGTH_SHORT).show());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    public void dialogHelpDetalhes() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajuda");
        builder.setMessage("Bem vindo(a) ao cadastro de turma! \n\n" +
                "Nesta tela são solicitados alguns dados para cadastrar um nova turma.\n\n" +
                "1 - Nome: deve ser informado o nome da turma *obrigatório* \n\n" +
                "2 - Alunos: podem ser incluídos alunos para essa turma selecionando-os no campo 'Selecione os Alunos', os quais antecipadamente já devem estar cadastrados no KARITI na tela de cadastro de alunos. Todos os alunos selecionados são listados no campo 'Alunos'. Caso selecione algum aluno errado, basta clicar no nome do aluno para remove-lo da lista. \n\n" +
                "3 - Anônimos: caso não deseje cadastrar alunos para essa turma, podem ser incluidos alunos anônimos no campo 'Incluir Alunos Anônimos', informando a quantidade no campo sugerido, sem a necessidade de cadastrar todos ou nenhum aluno como descrito na opção 2. \n\n" +
                "Obs. A Turma não pode ser cadastrada sem alunos.");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}