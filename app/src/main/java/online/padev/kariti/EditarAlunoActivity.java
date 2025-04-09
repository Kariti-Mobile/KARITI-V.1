package online.padev.kariti;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class EditarAlunoActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{
    ImageButton voltar;
    EditText editTxtNomeAluno, editTxtEmailAluno;
    Button btnSalvar;
    String alunoBD, emailBD;
    Integer id_aluno;
    BancoDados bancoDados;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_aluno);

        editTxtNomeAluno = findViewById(R.id.editTextAlunoCadastrado);
        editTxtEmailAluno = findViewById(R.id.editTextEmailCadastrado);
        btnSalvar = findViewById(R.id.buttonSalvarEditAluno);
        voltar = findViewById(R.id.imgBtnVoltaEscola);

        bancoDados = new BancoDados(this);

        infoEditarAluno();

        id_aluno = getIntent().getExtras().getInt("id_aluno");
        alunoBD = bancoDados.pegaNomeAlunoPStatus(id_aluno, 1);
        emailBD = bancoDados.pegarEmailAluno(id_aluno);
        if (alunoBD == null || emailBD == null){
            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
            finish();
        }
        editTxtNomeAluno.setText(alunoBD); //Mostra o nome do aluno
        editTxtEmailAluno.setText(emailBD); //Mostra o e-mail do aluno

        btnSalvar.setOnClickListener(view -> {
            String nomeAlunoAtual = editTxtNomeAluno.getText().toString().trim();
            String emailAlunoAtual = editTxtEmailAluno.getText().toString().trim();
            if (nomeAlunoAtual.equals(alunoBD) && emailAlunoAtual.equals(emailBD)){
                Toast.makeText(this, "Sem alterações realizadas", Toast.LENGTH_SHORT).show();
                return;
            }
            if(nomeAlunoAtual.trim().isEmpty()){
                Toast.makeText(EditarAlunoActivity.this, "Informe o nome do aluno!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!alunoBD.equals(nomeAlunoAtual)){
                Boolean verificaNovoAluno = bancoDados.verificaExisteAlunoPNome(nomeAlunoAtual);
                if (verificaNovoAluno == null) {
                    Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (verificaNovoAluno) {
                    Toast.makeText(this, "Já existe um aluno com esse nome, cadastrado nesta escola!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (!emailAlunoAtual.equals(emailBD) && !emailAlunoAtual.trim().isEmpty()) {
                if (!Patterns.EMAIL_ADDRESS.matcher(emailAlunoAtual).matches()) {
                    Toast.makeText(this, "E-mail inválido", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            Boolean alterarDadosAluno = bancoDados.alterarDadosAluno(nomeAlunoAtual, emailAlunoAtual, id_aluno);
            if (alterarDadosAluno == null){
                Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                return;
            }
            if (alterarDadosAluno) {
                Toast.makeText(EditarAlunoActivity.this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show();
                recarregarVisualAlunos();
            } else {
                Toast.makeText(EditarAlunoActivity.this, "Erro: alteração nao realizada!", Toast.LENGTH_SHORT).show();
            }
        });
        voltar.setOnClickListener(view -> recarregarVisualAlunos());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                recarregarVisualAlunos();
            }
        });
    }
    public void recarregarVisualAlunos(){
        setResult(RESULT_OK);
        finish();
    }

    public void popMenuAluno(View v){
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.activity_menualuno);
        popupMenu.show();
    }
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuExcluirAluno) {
            AlertDialog.Builder builder = new AlertDialog.Builder(EditarAlunoActivity.this);
            builder.setTitle("Atenção!")
                    .setMessage("Deseja realmente excluir o aluno?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        Boolean deletarAluno = bancoDados.deletarAluno(id_aluno);
                        if (deletarAluno) {
                            Toast.makeText(this, "Aluno excluido com sucesso", Toast.LENGTH_SHORT).show();
                            recarregarVisualAlunos();
                        }else{
                            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .setNegativeButton("Não", (dialog, which) -> {
                        // cancelou
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return true;
        } else {
            return false;
        }
    }
    public void infoEditarAluno(){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditarAlunoActivity.this);
        builder.setTitle("Ajuda")
                .setMessage("Olá, caso deseje alterar as informações desse aluno, basta informar os novos dados nos campos e clicar em salvar.");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}


