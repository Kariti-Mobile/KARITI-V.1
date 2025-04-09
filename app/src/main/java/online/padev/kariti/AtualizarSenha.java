package online.padev.kariti;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class AtualizarSenha extends AppCompatActivity{
    private Integer id_usuario;
    String nomeUsuario, emailUsuario, novaSenha, confNovaSenha;
    EditText editTextNovaSenha, editTextConfNovaSenha;
    TextView descricaoNovaSenha, titulo;
    Button btnAlterar;
    BancoDados bancoDados;
    ImageButton ocultarSenha, ocultarSenha2, voltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atualizar_senha);

        descricaoNovaSenha = findViewById(R.id.descricaoNovaSenha);
        editTextNovaSenha = findViewById(R.id.editTextNovaAttSenha);
        editTextConfNovaSenha = findViewById(R.id.editTextConfirmAttSenha);
        btnAlterar = findViewById(R.id.buttonAttSenha);
        voltar = findViewById(R.id.imgBtnVoltar);
        ocultarSenha2 = findViewById(R.id.imgButtonSenhaOFF);
        ocultarSenha = findViewById(R.id.senhaoculta);
        titulo = findViewById(R.id.toolbar_title);

        bancoDados = new BancoDados(this);

        id_usuario = getIntent().getExtras().getInt("id_usuario");
        nomeUsuario = getIntent().getExtras().getString("nome");
        emailUsuario = getIntent().getExtras().getString("email");

        titulo.setText(String.format("%s","Nova senha"));
        descricaoNovaSenha.setText(String.format("Olá, %s!\n Informe uma senha segura para acesso ao KARITI.",nomeUsuario));

        btnAlterar.setOnClickListener(v -> {
            novaSenha = editTextNovaSenha.getText().toString();
            confNovaSenha = editTextConfNovaSenha.getText().toString();
            if(novaSenha.trim().isEmpty() || confNovaSenha.trim().isEmpty()){
                Toast.makeText(this, "Informe a senha nos dois campos!", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!novaSenha.equals(confNovaSenha)){
                Toast.makeText(this, "Senhas divergentes!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (bancoDados.alterarSenha(novaSenha, id_usuario)) {
                mudarParaTelaLogin();
            }else{
                Toast.makeText(this, "Erro de comunicação!\n\n Por favor, tente novamente!", Toast.LENGTH_SHORT).show();
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

        editTextNovaSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        ocultarSenha.setOnClickListener(v -> {
//           Verifica se a senha está visivel ou oculta.
            if(editTextNovaSenha.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD){
//                  Se a senha está visivel ou oculta.
                editTextNovaSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ocultarSenha.setImageResource(R.mipmap.senhaoff);
            } else {
                editTextNovaSenha.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ocultarSenha.setImageResource(R.mipmap.senhaon);
            }
            editTextNovaSenha.setSelection(editTextNovaSenha.getText().length());
        });

        editTextConfNovaSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        ocultarSenha2.setOnClickListener(v -> {
//          Verifica se a senha está visivel ou oculta.
            if(editTextConfNovaSenha.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD){
//                  Se a senha está visivel ou oculta.
                editTextConfNovaSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ocultarSenha2.setImageResource(R.mipmap.senhaoff);
            } else {
                editTextConfNovaSenha.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ocultarSenha2.setImageResource(R.mipmap.senhaon);
            }
            editTextConfNovaSenha.setSelection(editTextConfNovaSenha.getText().length());
        });
    }

    /**
     * Método usado para inicializar a tela de login.
     */
    private void mudarParaTelaLogin(){
        Toast.makeText(AtualizarSenha.this, "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}