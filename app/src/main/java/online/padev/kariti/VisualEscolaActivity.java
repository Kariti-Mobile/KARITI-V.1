package online.padev.kariti;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Collections;

public class VisualEscolaActivity extends AppCompatActivity {
    ImageButton iconeSair;
    FloatingActionButton btnEscolaDesativada, btnCadastrarEscola;
    ImageButton iconeAjuda;
    TextView titulo, txtDescricaoDesativadas, txtDescricaoNovaEscola;
    ListView listViewEscolas;
    EscolaAdapter adapter;
    private ArrayList<String> listaEscolasBD;
    BancoDados bancoDados;
    private static final int REQUEST_CODE = 1;
    private Integer id_escola;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_escola);

        iconeSair = findViewById(R.id.imageButtonInicio);
        btnEscolaDesativada = findViewById(R.id.iconarquivadas);
        listViewEscolas = findViewById(R.id.listViewEscolas);
        iconeAjuda = findViewById(R.id.iconHelpLogout);
        btnCadastrarEscola = findViewById(R.id.iconmaisescolas);
        titulo = findViewById(R.id.toolbar_title);
        txtDescricaoDesativadas = findViewById(R.id.txtDescricaoDesativadas);
        txtDescricaoNovaEscola = findViewById(R.id.txtDescricaoNovaEscola);

        bancoDados = new BancoDados(this);

        titulo.setText(String.format("%s","Acessar com:"));

        listaEscolasBD = (ArrayList<String>) bancoDados.listarEscolas(1); //carrega todas as escolas ativadas para o usuario logado
        if(listaEscolasBD == null){
            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
            finish();
        }
        if(listaEscolasBD.isEmpty()){
            if(!bancoDados.listarEscolas(0).isEmpty()){
                telaEscolaDesativada();
            }else{
                cadastrarNovaEscola();
            }
        }
        adapter = new EscolaAdapter(this, listaEscolasBD, listaEscolasBD);
        listViewEscolas.setAdapter(adapter);

        listViewEscolas.setOnItemClickListener((parent, view, position, id) -> {
            BancoDados.ID_ESCOLA = bancoDados.pegarIdEscola(adapter.getItem(position));
            if (BancoDados.ID_ESCOLA == null || BancoDados.ID_ESCOLA == -1){
                Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                return;
            }
            carregarDetalhesEscola();
        });
        listViewEscolas.setOnItemLongClickListener((parent, view, position, id) -> {
            // Exibir a caixa de diálogo
            AlertDialog.Builder builder = new AlertDialog.Builder(VisualEscolaActivity.this);
            builder.setTitle("Atenção!")
                    .setMessage("Deseja desativar essa escola?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        id_escola = bancoDados.pegarIdEscola(adapter.getItem(position));
                        if (id_escola == null || id_escola == -1){
                            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(bancoDados.alterarStatusEscola(id_escola,0)){
                            listaEscolasBD.remove(position);
                            adapter.notifyDataSetChanged();
                            if(listaEscolasBD.isEmpty()) {
                                finish();
                            }
                            Toast.makeText(VisualEscolaActivity.this, "Escola desativada", Toast.LENGTH_SHORT).show();
                        }else
                            Toast.makeText(VisualEscolaActivity.this, "Erro: escola não desativada!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Não", (dialog, which) -> {
                        // Código para lidar com o clique no botão cancelar, se necessário
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            // Retorna true para indicar que o evento de long press foi consumido
            return true;
        });

        //Exibir o texto sobre o botão
        txtDescricaoNovaEscola.setVisibility(View.VISIBLE);
        txtDescricaoDesativadas.setVisibility(View.VISIBLE);
        // Ocultar o texto após 3 segundos
        new Handler().postDelayed(() -> txtDescricaoNovaEscola.setVisibility(View.INVISIBLE), 10000);
        new Handler().postDelayed(() -> txtDescricaoDesativadas.setVisibility(View.INVISIBLE), 10000);

        btnCadastrarEscola.setOnClickListener(v -> {
            txtDescricaoNovaEscola.setVisibility(View.VISIBLE);
            new Handler().postDelayed(() -> txtDescricaoNovaEscola.setVisibility(View.INVISIBLE), 3000);
            cadastrarNovaEscola();
        });

        btnEscolaDesativada.setOnClickListener(v -> {
            txtDescricaoDesativadas.setVisibility(View.VISIBLE);
            new Handler().postDelayed(() -> txtDescricaoDesativadas.setVisibility(View.INVISIBLE), 3000);
            telaEscolaDesativada();
        });
        iconeAjuda.setOnClickListener(v -> ajuda());
        iconeSair.setOnClickListener(v -> sair());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                sair();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            finish();
            startActivity(getIntent());
        }
    }
    private void sair(){
        BancoDados.USER_ID = null;
        finish();
        Toast.makeText(VisualEscolaActivity.this, "Usuário desconectado", Toast.LENGTH_SHORT).show();
    }
    private void ajuda() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajuda");
        builder.setMessage("• Para continuar navegando nas funcionalidades do app, clique no campo com a escola desejada. \n\n" +
                "• Cada escola possui suas informações que são restritas a outras. \n\n" +
                "• Para desativar uma escola, basta selecionar a escola desejada e confirmar a ação. " +
                "Posteriormente, você poderá encontrar suas escolas desativadas clicando no botão 'Escolas Desativadas'.");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    private void carregarDetalhesEscola(){
        Intent intent = new Intent(this, DetalhesEscolaActivity.class);
        startActivity(intent);
    }
    private void telaEscolaDesativada() {
        if(!bancoDados.listarEscolas(0).isEmpty()) {
            Intent intent = new Intent(this, EscolaDesativadaActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
        }else{
            avisoSemEscolasDesativadas();
        }
    }
    private void cadastrarNovaEscola() {
        // Inflar o layout customizado
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.cadastrar_escola_dialog, null);

        // Inicializar os elementos do layout
        FloatingActionButton cancelarFlut = dialogView.findViewById(R.id.btnvoltarflutuante);
        EditText editTextEscola = dialogView.findViewById(R.id.editTextNomeEscolaDialog);
        Button btnCadastrar = dialogView.findViewById(R.id.buttonDialog);

        // Criar o AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(dialogView);
        // Mostrar o diálogo
        AlertDialog dialog = builder.create();
        dialog.show();

        btnCadastrar.setOnClickListener(v -> {
            String nomeEscola = editTextEscola.getText().toString();
            if (!nomeEscola.trim().isEmpty()) {
                Boolean verificaEscola = bancoDados.verificaExisteEscola(nomeEscola);
                if(verificaEscola == null){
                    Toast.makeText(this, "Falha na comunicação, tente novamente!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!verificaEscola){
                    if (bancoDados.cadastrarEscola(nomeEscola, 1)) {
                        listaEscolasBD.add(nomeEscola);
                        Collections.sort(listaEscolasBD);
                        adapter.notifyDataSetChanged();
                        dialog.dismiss();
                        Toast.makeText(this, "Escola cadastrada com sucesso!", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(this, "Erro: Escola não cadastrada!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Atenção: Escola já cadastrada!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Informe o nome da escola!", Toast.LENGTH_SHORT).show();
            }
        });
        cancelarFlut.setOnClickListener(v -> {
            if(listaEscolasBD.isEmpty()) finish();
            dialog.dismiss();//Fecha o diálogo
        });
    }
    private void avisoSemEscolasDesativadas() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("KARITI");
        builder.setMessage("Aqui você encontra todas as suas escolas desativas.\n\n" +
                "Obs. Você não possui escolas desativadas até o momento!");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        // Criando o diálogo
        AlertDialog dialog = builder.create();

        // Exibindo o diálogo
        dialog.show();
        // Mudando a cor do botão "OK" depois de mostrar o diálogo
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                ContextCompat.getColor(this, R.color.azul)
        );
    }
}