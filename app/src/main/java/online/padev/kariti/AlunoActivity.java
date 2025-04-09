package online.padev.kariti;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class AlunoActivity extends AppCompatActivity {
    ImageButton btnVoltar, iconeAjuda;
    Button btnCadastrarAluno, btnVisualizarAluno;
    TextView textviewTitulo;

    BancoDados bancoDados;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aluno);

        btnCadastrarAluno = findViewById(R.id.buttonCadAluno);
        btnVisualizarAluno = findViewById(R.id.buttonVisualAluno);
        textviewTitulo = findViewById(R.id.toolbar_title);
        btnVoltar = findViewById(R.id.imgBtnVoltaDescola);
        iconeAjuda = findViewById(R.id.iconHelp);

        bancoDados = new BancoDados(this);

        textviewTitulo.setText(String.format("%s", "Aluno"));

        btnCadastrarAluno.setOnClickListener(v -> carregarTelaCadastroAluno());
        btnVisualizarAluno.setOnClickListener(v -> carregarTelaVisualizarAluno());
        iconeAjuda.setOnClickListener(v -> ajuda());

        btnVoltar.setOnClickListener(v -> {
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
    private void carregarTelaCadastroAluno(){
        Intent intent = new Intent(this, CadAlunoActivity.class);
        startActivity(intent);
    }
    private void carregarTelaVisualizarAluno(){
        Boolean verificaAlunos = bancoDados.verificaExisteAlunosPorEscola();
        if(verificaAlunos == null){
            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
            return;
        }
        if (verificaAlunos) {
            Intent intent = new Intent(this, VisualAlunoActivity.class);
            startActivity(intent);
        }else{
            avisoSemAlunos();
        }
    }
    public void ajuda() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajuda");
        builder.setMessage("Nesta tela você tem duas opções, cadastrar novos alunos ou visualizar os alunos já cadastrados.\n\n" +
                "IMPORTANTE!\n\n" +
                "O kariti possibilita que seus usuários possam cadastrar provas sem a necessidade de cadastro dos alunos nesta etapa.\n\n" +
                "Para isso, basta selecionar a opção 'Turma -> Cadastrar Turma' informar o nome da turma e quantidade de alunos no campo 'Incluir Alunos Anônimos'.\n" +
                "Dessa forma será criada uma turma com seus alunos anônimos (alunos com identificação unica gerada pelo KARITI).");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    private void avisoSemAlunos(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Atenção!");
        builder.setMessage("Não encontramos alunos cadastrados para essa escola!");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}