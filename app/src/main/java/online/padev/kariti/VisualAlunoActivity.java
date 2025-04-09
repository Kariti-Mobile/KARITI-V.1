package online.padev.kariti;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class VisualAlunoActivity extends AppCompatActivity {
    ImageButton btnVoltar;
    EditText pesquisarAlunos;
    ArrayList<String> listaAlunos;
    MyAdapter adapterAluno;
    TextView tituloAlunos, totalAlunos;
    RecyclerView recyclerView;
    private static final int REQUEST_CODE = 1;
    private Integer id_aluno;

    BancoDados bancoDados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_aluno);

        btnVoltar = findViewById(R.id.imgBtnVoltar);
        pesquisarAlunos = findViewById(R.id.editTextBuscar);
        recyclerView = findViewById(R.id.listSelecAluno);
        bancoDados = new BancoDados(this);
        totalAlunos = findViewById(R.id.totalAlunos);

        tituloAlunos = findViewById(R.id.toolbar_title);
        tituloAlunos.setText(String.format("%s","Alunos"));

        listaAlunos = (ArrayList<String>) bancoDados.listarNomesAlunos(1);
        if (listaAlunos == null){
            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
            finish();
        }

        totalAlunos.setText(String.format("%s","Total de Alunos: "+ listaAlunos.size()));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapterAluno = new MyAdapter(this, listaAlunos, this::onItemClick, this::onItemLongClick);
        recyclerView.setAdapter(adapterAluno);

        pesquisarAlunos.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){
                adapterAluno.getFilter().filter(charSequence);
            }
            @Override
            public void afterTextChanged(Editable editable){
            }
        });
        btnVoltar.setOnClickListener(view -> {
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
    public void onItemClick(int position) {
        id_aluno = bancoDados.pegarIdAluno(listaAlunos.get(position));
        if (id_aluno == null){
            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getApplicationContext(), EditarAlunoActivity.class);
        intent.putExtra("id_aluno", id_aluno);
        startActivityForResult(intent, REQUEST_CODE);
    }
    public void onItemLongClick(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(VisualAlunoActivity.this);
        builder.setTitle("Atenção!")
                .setMessage("Deseja realmente excluir esse aluno?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    id_aluno = bancoDados.pegarIdAluno(listaAlunos.get(position));
                    if (id_aluno == null){
                        Toast.makeText(VisualAlunoActivity.this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Boolean verificaAluno = bancoDados.verificaExisteAlunoEmTurma(id_aluno);
                    if(verificaAluno == null){
                        Toast.makeText(VisualAlunoActivity.this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(!verificaAluno){
                        Boolean deletaAluno = bancoDados.deletarAluno(id_aluno);
                        if (deletaAluno) {
                            listaAlunos.remove(position);
                            adapterAluno.notifyItemRemoved(position);
                            Toast.makeText(VisualAlunoActivity.this, "Aluno Excluido! ", Toast.LENGTH_SHORT).show();
                        }else
                            Toast.makeText(VisualAlunoActivity.this, "Erro: aluno não excluido!", Toast.LENGTH_SHORT).show();
                    }else avisoNotExluirAluno();
                })
                .setNegativeButton("Não", (dialog, which) -> {
                    //cancelou
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            finish();
            startActivity(getIntent());
        }
    }
    private void avisoNotExluirAluno(){
        AlertDialog.Builder builder = new AlertDialog.Builder(VisualAlunoActivity.this);
        builder.setTitle("Atenção!")
                .setMessage("Este aluno possui vínculo com uma ou mais turma(s) cadastrada(s), não sendo possível excluir!.");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}