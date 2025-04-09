package online.padev.kariti;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText EditTextEmail, EditTextSenha;
    private String emailInformado, senhaInformada, codigo;
    private Integer id_usuario;
    TextView criarConta;
    Button btnEntrar, esqueciSenha;
    ImageButton ocultarSenha;
    BancoDados bancoDados;
    CodSenhaActivity codSenhaActivity;
    EnviarEmail enviarEmail;
    GerarCodigoValidacao gerarCodigo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnEntrar = findViewById(R.id.buttonEntrarL);
        esqueciSenha = findViewById(R.id.buttonEsqueciSenhaL);
        criarConta = findViewById(R.id.textViewCriarConta);
        EditTextEmail = findViewById(R.id.editTextLogin);
        EditTextSenha = findViewById(R.id.editTextSenha);
        ocultarSenha = findViewById(R.id.senhaoculta);

       // EditTextEmail.setText(String.format("%s","felipemartinsdonascimento4@gmail.com"));
        //EditTextSenha.setText(String.format("%s","123"));

        bancoDados = new BancoDados(this);
        enviarEmail = new EnviarEmail();
        gerarCodigo = new GerarCodigoValidacao();
        codSenhaActivity = new CodSenhaActivity();

        btnEntrar.setOnClickListener(v -> {
            emailInformado = EditTextEmail.getText().toString();
            senhaInformada = EditTextSenha.getText().toString();
            if (emailInformado.trim().isEmpty()){
                Toast.makeText(LoginActivity.this, "Informe seu e-mail!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (senhaInformada.trim().isEmpty()){
                Toast.makeText(LoginActivity.this, "Informe a senha!", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(emailInformado).matches()) {
                Toast.makeText(LoginActivity.this, "E-mail Inválido", Toast.LENGTH_SHORT).show();
                return;
            }
            Integer id_usuario = bancoDados.verificaAutenticacao(emailInformado, senhaInformada);
            if (id_usuario == null || id_usuario == -1) {
                Toast.makeText(this, "Usuário e/ou senha inválidos! ", Toast.LENGTH_SHORT).show();
                return;
            }
            BancoDados.USER_ID = id_usuario;
            carregarTelaInicial();
        });
        esqueciSenha.setOnClickListener(v -> {
            if(!VerificaConexaoInternet.verificaConexao(LoginActivity.this)){
                Toast.makeText(LoginActivity.this, "Sem conexão de rede!", Toast.LENGTH_SHORT).show();
                return;
            }
            emailInformado = EditTextEmail.getText().toString();
            if(emailInformado.trim().isEmpty()) {
                alerteEsqueciSenha();
                return;
            }
            id_usuario = bancoDados.verificaExisteEmail(emailInformado);
            if(id_usuario == null || id_usuario == -1) {
                Toast.makeText(this, "E-mail não encontrado!", Toast.LENGTH_SHORT).show();
                return;
            }
            codigo = gerarCodigo.gerarVerificador();
            if (enviarEmail.enviaCodigo(emailInformado, codigo)) {
                carregarTelaCodigo();
            }else {
                Toast.makeText(LoginActivity.this, "Email não Enviado!", Toast.LENGTH_SHORT).show();
            }
        });

        EditTextSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        ocultarSenha.setOnClickListener(v -> {
//                Verifica se a senha está visivel ou oculta.
            if(EditTextSenha.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD){
//                  Se a senha está visivel ou oculta.
                EditTextSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ocultarSenha.setImageResource(R.mipmap.senhaoff);
            } else {
                EditTextSenha.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ocultarSenha.setImageResource(R.mipmap.senhaon);
            }
            EditTextSenha.setSelection(EditTextSenha.getText().length());
        });

        criarConta.setOnClickListener(v -> mudarParaTelaCadastro());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }
    private void carregarTelaInicial(){
        Intent intent = new Intent(this, VisualEscolaActivity.class);
        startActivity(intent);
        finish();
    }
    private void mudarParaTelaCadastro(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void alerteEsqueciSenha(){
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Esqueceu sua senha?")
                .setMessage("Por favor, informe seu e-mail cadastrado no campo E-mail, em seguida pressione 'Esqueci Minha Senha'")
                .setPositiveButton("Ok", (dialog, which) -> Toast.makeText(LoginActivity.this, "Informe o E-mail! ", Toast.LENGTH_SHORT).show());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    /**
     * Este método inicializa a tela de verificação de código, para autenticação de cadastro do usuário.
     */
    private void carregarTelaCodigo(){
        Intent proxima = new Intent(getApplicationContext(), CodSenhaActivity.class);
        proxima.putExtra("identificador", 1);
        proxima.putExtra("id_usuario", id_usuario);
        proxima.putExtra("email", emailInformado);
        proxima.putExtra("codigo", codigo);
        startActivity(proxima);
        finish();
    }
}