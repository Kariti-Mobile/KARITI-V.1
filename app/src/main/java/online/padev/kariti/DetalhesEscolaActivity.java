package online.padev.kariti;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class DetalhesEscolaActivity extends AppCompatActivity {
    ImageButton btnVoltar, iconeAjuda;
    Button btnTurma, btnAluno, btnProva;
    TextView textViewEscola;
    BancoDados bancoDados;
    String nomeEscola;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_escola);

        btnVoltar = findViewById(R.id.imgBtnVoltaDescola);
        btnVoltar.setVisibility(View.VISIBLE);
        iconeAjuda = findViewById(R.id.iconHelp);

        btnTurma = findViewById(R.id.btnTurma);
        btnAluno = findViewById(R.id.buttonAluno);
        btnProva = findViewById(R.id.btnProva);
        textViewEscola = findViewById(R.id.toolbar_title);

        bancoDados = new BancoDados(this);

        nomeEscola = bancoDados.pegarNomeEscola();
        if (nomeEscola == null){ //vericação caso ocorra exceções no Banco
            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
            finish();
        }

        textViewEscola.setText(nomeEscola);

        btnTurma.setOnClickListener(v -> carregarTelaTurma());
        btnAluno.setOnClickListener(v -> carregarTelaAluno());
        btnProva.setOnClickListener(v -> carregarTelaProva());
        iconeAjuda.setOnClickListener(v -> ajuda());

        btnVoltar.setOnClickListener(v -> {
            BancoDados.ID_ESCOLA = null;
            getOnBackPressedDispatcher();
            finish();
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                BancoDados.ID_ESCOLA = null;
                finish();
            }
        });
    }
    public void carregarTelaTurma(){
        Intent intent = new Intent(this, TurmaActivity.class);
        startActivity(intent);
    }
    public void carregarTelaAluno(){
        Intent intent = new Intent(this, AlunoActivity.class);
        startActivity(intent);
    }
    public void carregarTelaProva(){
        Intent intent = new Intent(this, ProvaActivity.class);
        startActivity(intent);
    }

    public void ajuda() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajuda");
        builder.setMessage("• Clique na opção \"Aluno\" para cadastrar seus estudantes, independentemente das turmas às quais eles pertencem. Caso não deseje cadastrar os alunos, será possível cadastrar estudantes anônimos (sem definição de nome) em etapa posterior.\n\n\n" +
                "• Clique na opção \"Turma\" para cadastrar as turmas de estudantes e para vincular os estudantes correspondentes (podendo também ser inseridos alunos anônimos nesta etapa).\n\n" +
                "• Após cadastrada a turma e vinculados os alunos correspondentes, clique em \"Prova\" para cadastrar as informações sobre uma prova a ser aplicada, incluindo suas informações básicas e seu gabarito.\n");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

}