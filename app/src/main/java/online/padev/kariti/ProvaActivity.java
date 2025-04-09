package online.padev.kariti;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class ProvaActivity extends AppCompatActivity {
    ImageButton voltar, iconeAjuda;
    Button btnCadastrarProva, btnGerarCartao, btnCorrigirProva, btnProvasCorrigida, editarProva;
    BancoDados bancoDados;
    TextView textViewTitulo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prova);

        voltar = findViewById(R.id.imgBtnVoltaDescola);
        iconeAjuda = findViewById(R.id.iconHelp);
        btnCadastrarProva = findViewById(R.id.buttonCadProva);
        btnGerarCartao = findViewById(R.id.buttonGerarCatao);
        btnCorrigirProva = findViewById(R.id.buttonCorrigirProva);
        btnProvasCorrigida = findViewById(R.id.buttonVisuProva);
        editarProva = findViewById(R.id.buttonEdicaoProva);
        textViewTitulo = findViewById(R.id.toolbar_title);

        bancoDados = new BancoDados(this);

        textViewTitulo.setText(String.format("%s","Prova"));

        iconeAjuda.setOnClickListener(v -> ajuda());
        btnCadastrarProva.setOnClickListener(v -> carregarCadastroProva());
        editarProva.setOnClickListener(v -> carregarTelaMaisOpcoes());
        btnGerarCartao.setOnClickListener(v -> carregarTelaGerarCartao());
        btnProvasCorrigida.setOnClickListener(v -> carregarTelaProvasCorrigida());
        btnCorrigirProva.setOnClickListener(v -> {
            Boolean verificaProva = bancoDados.verificaExisteProvaCadastrada();
            if (verificaProva == null){
                Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
                return;
            }
            if(verificaProva){
                carregaEtapaCorrecao();
            }else{
                aviso("provas cadastradas");
            }
        });
        voltar.setOnClickListener(v -> {
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
    private void carregarCadastroProva(){
        Boolean verificaTurma = bancoDados.verificaExisteTurmas();
        if (verificaTurma == null){
            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
            return;
        }
        if(verificaTurma){
            Intent intent = new Intent(this, CadProvaActivity.class);
            startActivity(intent);
        }else aviso("turmas cadastradas");
    }
    private void carregarTelaMaisOpcoes(){
        Intent intent = new Intent(getApplicationContext(), EditarProva.class);
        startActivity(intent);
    }
    private void carregarTelaGerarCartao(){
        Boolean verificaProvaCad = bancoDados.verificaExisteProvaCadastrada();
        if (verificaProvaCad == null){
            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
            return;
        }
        if (verificaProvaCad) {
            Intent intent = new Intent(this, ProvaCartoesActivity.class);
            intent.putExtra("endereco", 2);
            startActivity(intent);
        }else{
            aviso("provas cadastradas");
        }
    }
    private void carregarTelaProvasCorrigida(){
        Boolean verificaExisteProvaCorrigida = bancoDados.verificaExisteProvaCorrigida();
        if (verificaExisteProvaCorrigida == null){
            Toast.makeText(this, "Falha de comunicação! \n\n Por favor, tente novamente", Toast.LENGTH_SHORT).show();
            return;
        }
        if(verificaExisteProvaCorrigida){
            Intent intent = new Intent(this, VisualProvaActivity.class);
            startActivity(intent);
        }else{
            aviso("provas corrigidas");
        }
    }
    private void carregaEtapaCorrecao(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("INSTRUÇÕES IMPORTATES!");
        builder.setMessage("Para garantir melhor desempenho do KARITI nas correções, é essencial que a imagem do cartão resposta seja capturada:\n\n" +
                "• Em superfície com fundo escuro e uniforme\n\n" +
                "• Em ambientes com boa iluminação\n\n" +
                "• De forma que o cartão seja enquadrado por inteiro na imagem\n\n" +
                "• Com boa visibilidade\n");
        builder.setPositiveButton("Iniciar Correção", (dialog, which) -> {
            Intent intent = new Intent(getApplicationContext(), GaleriaActivity.class);
            intent.putExtra("contexto","inicia_correcao");
            startActivity(intent);
            dialog.dismiss();
        });
        builder.show();
    }
    private void aviso(String descricao){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Atenção!");
        builder.setMessage("Não encontramos "+descricao+" para essa escola. Para ter acesso a essa opção é necessário ter "+descricao+".");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    private void ajuda() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajuda");
        builder.setMessage("Tela principal de prova.\n\n" +
                "• Cadastrar Prova - Selecionando essa opção, será derecionado a tela que solicita as informações necessárias para elaboração da prova e, em seguida solicita o preenchimento do gabarito.\n\n" +
                "• Gerar Cartões - Nesta opção é realizado o download dos cartões resposta de uma prova já cadastrada na opção anterior.\n\n" +
                "• Corrigir Prova - Após selecionada essa opção, basta realizar os passos sugeriodos pelo KARITI, iniciar correção clicando no botão 'Scannear Cartão', capturar o QrCode da prova e capturar a imagem do cartão resposta, em seguida são listadas as provas capuradas na próxima tela, onde, são sugeridas duas opções, continuar capturando mais provas ou finalizar a correção.\n\n" +
                "• Visualizar Prova - Nesta opção pode ser visualizado o resultado da correção das provas informando a quantidade de acertos e nota de cada aluno.");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}