package online.padev.kariti;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class EditarProva extends AppCompatActivity {
    private Spinner prova, turma;
    private Button editar, apagar;
    private ArrayList<String> listTurma, lisProva;
    private String turmaSelecionada, provaSelecionada;
    private Integer id_turma;
    private ImageButton voltar;
    BancoDados bancoDados;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_prova);

        voltar = findViewById(R.id.imgBtnVoltar);
        prova = findViewById(R.id.spinnerProva2);
        turma = findViewById(R.id.spinnerTurma2);
        editar = findViewById(R.id.buttonEditar);
        apagar = findViewById(R.id.buttonApagar);
        TextView titulo = findViewById(R.id.toolbar_title);

        titulo.setText("Mais Opções");

        bancoDados = new BancoDados(this);

        listTurma = (ArrayList<String>) bancoDados.listarTurmasPorProva();
        listTurma.add(0, "Selecione a turma");
        SpinnerAdapter adapterTurma = new SpinnerAdapter(this, listTurma);
        turma.setAdapter(adapterTurma);

        turma.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0) {
                    turmaSelecionada = turma.getSelectedItem().toString();
                    id_turma = bancoDados.pegarIdTurma(turmaSelecionada);

                    lisProva = (ArrayList<String>) bancoDados.listarNomesProvasPorTurma(id_turma.toString());
                    SpinnerAdapter adapterProva = new SpinnerAdapter(EditarProva.this, lisProva);
                    prova.setAdapter(adapterProva);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editarProva();
            }
        });
        apagar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apagarProva();
            }
        });
        voltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });
    }
    public void editarProva(){
        if(prova.getSelectedItem() == null){
            return;
        }
        provaSelecionada = (String) prova.getSelectedItem();
        Integer id_prova = bancoDados.pegarIdProva(provaSelecionada, id_turma);
        if(bancoDados.verificaExisteCorrecao(id_prova.toString())){
            naoEditavel();
        }else {
            Intent intent = new Intent(this, EdicaoProva.class);
            intent.putExtra("prova", provaSelecionada);
            intent.putExtra("id_prova", id_prova);
            intent.putExtra("id_turma", id_turma);
            startActivity(intent);
        }
    }
    public void apagarProva(){
        if(prova.getSelectedItem() == null)
            return;
        provaSelecionada = (String) prova.getSelectedItem();
        Integer id_prova = bancoDados.pegarIdProva(provaSelecionada, id_turma);
        if(bancoDados.verificaExisteCorrecao(id_prova.toString())){
            avisoSeApagar(id_prova);
        }else{
            bancoDados.deletarGabarito(id_prova);
            bancoDados.deletarProva(id_prova);
            provaApagada();
        }
    }
    private void deteteProva(Integer id_prova){
        bancoDados.deletarGabarito(id_prova);
        bancoDados.deletarCorrecao(id_prova);
        bancoDados.deletarProva(id_prova);
        provaApagada();

    }
    private void avisoSeApagar(Integer id_prova){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ATENÇÃO")
                .setMessage("Esta prova já foi corrigida. Deseja realmente " +
                        "apagá-la? Caso confirme essa ação, todos os " +
                        "dados relacionados a essa prova, inclusive as correções, serão apagados.\n\nDeseja continuar? ")
                .setPositiveButton("Apagar tudo", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deteteProva(id_prova);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void provaApagada(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Kariti")
                .setMessage("Prova apagada com sucesso!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void naoEditavel(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ATENÇÃO")
                .setMessage("Esta prova já foi corrigida.\n\n" +
                        "Não é possivel editar provas já corrigidas!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}