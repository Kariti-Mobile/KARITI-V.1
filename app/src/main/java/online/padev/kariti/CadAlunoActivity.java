package online.padev.kariti;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class CadAlunoActivity extends AppCompatActivity {
    ImageButton voltar;
    EditText nomeAluno, emailAluno;
    Button cadastrar;
    BancoDados bancoDados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cad_aluno);

        nomeAluno = findViewById(R.id.editTextAlunoCad);
        emailAluno = findViewById(R.id.editTextEmailCad);
        voltar = findViewById(R.id.imgBtnVoltaEscola);
        cadastrar = findViewById(R.id.buttonSalvarEdit);

        bancoDados = new BancoDados(this);

        cadastrar.setOnClickListener(view -> {
            String nome = nomeAluno.getText().toString();
            String email = emailAluno.getText().toString();
            if (nome.trim().isEmpty()) {
                Toast.makeText(CadAlunoActivity.this, "Informe o nome do aluno", Toast.LENGTH_SHORT).show();
                return;
            }
            Boolean verificaAluno = bancoDados.verificaExisteAlunoPNome(nome);
            if(verificaAluno == null){
                Toast.makeText(this, "Erro de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                return;
            }
            if (verificaAluno) {
                Toast.makeText(CadAlunoActivity.this, "Identificamos que esse aluno já está cadastrado!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!email.trim().isEmpty()) {
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Toast.makeText(CadAlunoActivity.this, "E-mail do aluno, inválido!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Boolean verificaEmail = bancoDados.verificaExisteEmailAluno(email);
                if(verificaEmail == null){
                    Toast.makeText(this, "Erro de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (verificaEmail){
                    Toast.makeText(CadAlunoActivity.this, "Este e-mail já esta vinculado a um aluno cadastrado!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            Log.e("kariti","Email: "+email);

            Integer inserirtAluno = bancoDados.cadastrarAluno(nome, email, 1);
            if (inserirtAluno != -1) {
                Toast.makeText(CadAlunoActivity.this, "Aluno cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            } else {Toast.makeText(CadAlunoActivity.this, "Aluno não cadastrado!", Toast.LENGTH_SHORT).show();}

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
}