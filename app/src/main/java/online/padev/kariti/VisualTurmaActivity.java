package online.padev.kariti;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class VisualTurmaActivity extends AppCompatActivity {
    ImageButton btnVoltar;
    ListView listViewTurma;
    ArrayList<String> listaTurma;
    TextView titulo;
    Integer id_turma;
    BancoDados bancoDados;
    private static final int REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_turma);

        btnVoltar = findViewById(R.id.imgBtnVoltar);
        listViewTurma = findViewById(R.id.listViewVisualTurma);
        titulo = findViewById(R.id.toolbar_title);

        titulo.setText(String.format("%s","Turmas"));

        bancoDados = new BancoDados(this);

        listaTurma = (ArrayList<String>) bancoDados.listarNomesTurmas();
        if (listaTurma == null){
            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente 7", Toast.LENGTH_SHORT).show();
            finish();
        }
        EscolaAdapter adapter = new EscolaAdapter(this, listaTurma, listaTurma);
        listViewTurma.setAdapter(adapter);

        listViewTurma.setOnItemClickListener((parent, view, position, id) -> {
            id_turma = bancoDados.pegarIdTurma(adapter.getItem(position));
            if (id_turma == null){
                Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente 7", Toast.LENGTH_SHORT).show();
                return;
            }
            telaDadosTurma(id_turma);
        });

        listViewTurma.setOnItemLongClickListener((parent, view, position, id) -> {
            // Exibir a caixa de diálogo
            AlertDialog.Builder builder = new AlertDialog.Builder(VisualTurmaActivity.this);
            builder.setTitle("Atenção!")
                    .setMessage("Deseja excluir essa turma?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        id_turma = bancoDados.pegarIdTurma(listaTurma.get(position));
                        Boolean verificaTurma = bancoDados.verificaExisteTurmaEmProva(id_turma);
                        if (id_turma == null || verificaTurma == null){
                            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente 7", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(!verificaTurma) {
                            Boolean deletaTurma = bancoDados.deletarTurma(id_turma);
                            if (deletaTurma) {
                                listaTurma.remove(position);
                                adapter.notifyDataSetChanged();
                                if(listaTurma.isEmpty()){
                                    finish();
                                }
                                Toast.makeText(VisualTurmaActivity.this, "Turma excluida com sucesso! ", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(VisualTurmaActivity.this, "Algo deu errado, falha ao tentar excluir a turma! ", Toast.LENGTH_SHORT).show();
                            }
                        }else avisoNotExluir();
                    })
                    .setNegativeButton("Não", (dialog, which) -> {
                        // cancelou
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return true;
        });
        btnVoltar.setOnClickListener(view -> {
            getOnBackPressedDispatcher();
            finish();
        });
    }
    private void telaDadosTurma(Integer idTurma) {
        Intent intent = new Intent(this, DadosTurmaActivity.class);
        intent.putExtra("idTurma", idTurma);
        startActivityForResult(intent, REQUEST_CODE);
    }
    private void avisoNotExluir(){
        AlertDialog.Builder builder = new AlertDialog.Builder(VisualTurmaActivity.this);
        builder.setTitle("Atenção!")
                .setMessage("Esta turma possui vínculo com uma ou mais prova(s) cadastrada(s), não sendo possível excluir!.");
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

}