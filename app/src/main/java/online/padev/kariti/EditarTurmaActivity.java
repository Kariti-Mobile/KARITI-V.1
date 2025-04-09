package online.padev.kariti;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.Objects;

public class EditarTurmaActivity extends AppCompatActivity {
    ImageButton voltar, iconeAjuda;
    ImageView maisAnonimos, menosAnonimos;
    ListView listViewAlunos;
    EditText editTxtTurma, EditTxtQtdnonimos;
    ArrayList<String> listaAlunosDTurma, alunosSpinner;
    String id_turma, nomeTurmaBD, nomeTurmaAtual, alunoSelecionado;
    BancoDados bancoDados;
    AdapterExclAluno adapter;
    Spinner spinnerAlunos;
    Button btnSalvar;
    Integer id_aluno, qtdAnonimosBD, qtdAnonimosAtual;
    TextView titulo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_turma);

        listViewAlunos = findViewById(R.id.listViewEditarTurma);
        editTxtTurma = findViewById(R.id.editTextEditTurma);
        maisAnonimos = findViewById(R.id.imageViewMaisNovosAnonimos);
        menosAnonimos = findViewById(R.id.imageViewMenosNovosAnonimos);
        EditTxtQtdnonimos = findViewById(R.id.editTextNovosAlunosAnonimos);
        spinnerAlunos = findViewById(R.id.spinnerBuscAlunoNovos);
        btnSalvar = findViewById(R.id.buttonSalvarTurma);
        voltar = findViewById(R.id.imgBtnVoltaDescola);
        iconeAjuda = findViewById(R.id.iconHelp);
        titulo = findViewById(R.id.toolbar_title);

        bancoDados = new BancoDados(this);
        listaAlunosDTurma = new ArrayList<>();

        titulo.setText(String.format("%s","Atualização"));

        id_turma = Objects.requireNonNull(getIntent().getExtras()).getString("id_turma");

        //Lista todos os alunos no Spinner
        alunosSpinner = (ArrayList<String>) bancoDados.listarNomesAlunos(1);
        if (alunosSpinner == null){
            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente - 1", Toast.LENGTH_SHORT).show();
            finish();
        }
        alunosSpinner.add(0, "Selecionar alunos");
        SpinnerAdapter adapterSpinner = new SpinnerAdapter(this, alunosSpinner);
        spinnerAlunos.setAdapter(adapterSpinner);
        spinnerAlunos.setSelection(0);

        //Mostra a turma a ser editada

        nomeTurmaBD = bancoDados.pegarNomeTurma(id_turma);
        qtdAnonimosBD = bancoDados.pegarQtdAlunosPorStatus(id_turma, 0);

        if (nomeTurmaBD == null || qtdAnonimosBD == null){
            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente - 2", Toast.LENGTH_SHORT).show();
            finish();
        }

        editTxtTurma.setText(nomeTurmaBD);
        EditTxtQtdnonimos.setText(String.format("%s", qtdAnonimosBD));

        informAnonimos(qtdAnonimosBD);

        //Lista os aluno cadastrados nesta turma.
        listaAlunosDTurma = (ArrayList<String>) bancoDados.listarAlunosTurmaPorStatus(id_turma, 1);
        if (listaAlunosDTurma == null){
            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente - 3", Toast.LENGTH_SHORT).show();
            finish();
        }
        adapter = new AdapterExclAluno(this, listaAlunosDTurma);
        listViewAlunos.setAdapter(adapter);

        //Identifica o aluno selecionado no Spinner e adiciona no listView
        spinnerAlunos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    alunoSelecionado = spinnerAlunos.getSelectedItem().toString();
                    if(listaAlunosDTurma.contains(alunoSelecionado)) {
                        Toast.makeText(EditarTurmaActivity.this, "Aluno já selecionado!", Toast.LENGTH_SHORT).show();
                        spinnerAlunos.setSelection(0);
                        return;
                    }
                    listaAlunosDTurma.add(alunoSelecionado);
                    adapter = new AdapterExclAluno(EditarTurmaActivity.this, listaAlunosDTurma);
                    listViewAlunos.setAdapter(adapter);
                    spinnerAlunos.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //Remove aluno do listView, após simples click
        listViewAlunos.setOnItemClickListener((adapterView, view, i, l) -> {
                listaAlunosDTurma.remove(i);
                adapter.notifyDataSetChanged();
                Toast.makeText(EditarTurmaActivity.this, "Aluno Removido! ", Toast.LENGTH_SHORT).show();
        });
        menosAnonimos.setOnClickListener(view -> {
            int menos = Integer.parseInt(EditTxtQtdnonimos.getText().toString());
            if(menos > 0)
                menos --;
            EditTxtQtdnonimos.setText(String.valueOf(menos));
        });

        maisAnonimos.setOnClickListener(view -> {
            int mais = Integer.parseInt(EditTxtQtdnonimos.getText().toString());
            mais ++;
            EditTxtQtdnonimos.setText(String.valueOf(mais));
        });
        btnSalvar.setOnClickListener(view -> {
            btnSalvar.setEnabled(false);
            nomeTurmaAtual = editTxtTurma.getText().toString().trim();
            qtdAnonimosAtual = Integer.valueOf(EditTxtQtdnonimos.getText().toString());
            if (nomeTurmaAtual.trim().isEmpty()) {
                Toast.makeText(EditarTurmaActivity.this, "Informe o nome da turma!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (listaAlunosDTurma.isEmpty() && qtdAnonimosAtual.equals(0)) {
                aviso();
                return;
            }
            if (!nomeTurmaAtual.equals(nomeTurmaBD)) {
                Boolean verificaTurma = bancoDados.verificaExisteTurmaPorNome(nomeTurmaAtual);
                if (verificaTurma == null){
                    Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente - 3", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (verificaTurma) {
                    Toast.makeText(this, "Turma já cadastrada! ", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!bancoDados.alterarDadosTurma(nomeTurmaAtual, Integer.valueOf(id_turma))) {
                    Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (!bancoDados.deletarAnonimos(Integer.valueOf(id_turma))){  //Deleta todos os alunos Anonimos pertecentes a essa turma da tabela aluno
                Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!bancoDados.deletarAlunoDeTurma(Integer.valueOf(id_turma))) {  //Deleta todos os alunos pertecentes a essa turma
                Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!listaAlunosDTurma.isEmpty()) {
                for (String aluno : listaAlunosDTurma) {
                    id_aluno = bancoDados.pegarIdAluno(aluno);
                    if (id_aluno != null && id_aluno != -1) {
                        if (!bancoDados.cadastrarAlunoNaTurma(Integer.valueOf(id_turma), id_aluno)){
                            Log.e("kariti","Erro ao tentar vincular o aluno "+aluno+" a turma com id: "+id_turma);
                        }
                    }
                }
            }
            if (!qtdAnonimosAtual.equals(0)) {
                int tamanho = String.valueOf(qtdAnonimosAtual).length();
                for (int x = 1; x <= qtdAnonimosAtual; x++) {
                    String anonimo = "Aluno "+ String.format("%0"+tamanho+"d",x);
                    Integer id_anonimo = bancoDados.cadastrarAluno(anonimo, null, 0);
                    if (id_anonimo != -1){
                        if (!bancoDados.cadastrarAlunoNaTurma(Integer.valueOf(id_turma), id_anonimo)) {
                            Log.e("kariti", "Erro ao tentar vincular o aluno anonimo " + anonimo + " a turma com id: " + id_turma);
                        }
                    }
                }
            }
            Toast.makeText(EditarTurmaActivity.this, "Dados alterados com sucesso!", Toast.LENGTH_SHORT).show();
            recarregarDadosTurma();
        });
        iconeAjuda.setOnClickListener(view -> dialogHelpDetalhes());
        voltar.setOnClickListener(view -> recarregarDadosTurma());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                recarregarDadosTurma();
            }
        });
    }
    public void PopMenu(View v){
        v.setOnClickListener(view -> Toast.makeText(EditarTurmaActivity.this, "Preparado para implementação", Toast.LENGTH_SHORT).show());
    }
    public void informAnonimos(Integer anonimos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("KARITI");
        builder.setMessage("Esta turma possui "+anonimos+" alunos anônimos cadastrados, caso deseje alterar essa quantidade, basta informar um novo valor no campo referente 'Incluir Alunos Anônimos'");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public void dialogHelpDetalhes() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajuda");
        builder.setMessage("Nesta tela os dados da turma podem sem alterados, seguindo o mesmo padrão de cadastro de turma.\n\n" +
                "1 - Nome: caso deseje alterar o nome, basta informar um novo\n\n" +
                "2 - Alunos: podem ser incluídos novos alunos para essa turma selecionando-os no campo 'Selecione os Alunos', os quais antecipadamente já devem estar cadastrados no KARITI na tela de cadastro de alunos. Caso deseje remover, basta clicar no nome do aluno para remove-lo da turma. \n\n" +
                "3 - Anônimos: caso não deseje cadastrar alunos para essa turma, podem ser incluidos alunos anônimos no campo 'Incluir Alunos Anônimos', informando a quantidade no campo sugerido, sem a necessidade de cadastrar todos ou nenhum aluno como descrito na opção 2. \n\n" +
                "Obs. A Turma não pode ser cadastrada sem alunos.");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    private void aviso(){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditarTurmaActivity.this);
        builder.setTitle("Atenção!")
                .setMessage("Não é possível cadastrar uma turma sem alunos. Por favor, selecione os alunos para essa turma ou, caso preferir, informe a quantidade de alunos anônimos! ")
                .setPositiveButton("OK", (dialog, which) -> Toast.makeText(EditarTurmaActivity.this, "Selecione os alunos!", Toast.LENGTH_SHORT).show());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    public void recarregarDadosTurma(){
        setResult(RESULT_OK);
        finish();
    }

}