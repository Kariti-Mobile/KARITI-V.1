package online.padev.kariti;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class AnimacaoCorrecao extends AppCompatActivity {
    TextView titulo, informativo;
        static AnimacaoCorrecao instanciaEncerra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animacao_correcao);

        instanciaEncerra = this;

        ImageButton btnVoltar = findViewById(R.id.imgBtnVoltar);
        titulo = findViewById(R.id.toolbar_title);
        informativo = findViewById(R.id.textViewInformativo);

        informativo.setText(String.format(" %s ","Prova(s) enviada(s) para correção!\n\n" +
                "Em instantes sua prova será corrigida. Após a correção, o resultado " +
                "estará disponível na opção 'Visualizar Correção'\n\n" +
                "Por favor, aguarde..."));

        titulo.setText(String.format("%s","Corrigindo..."));
        btnVoltar.setOnClickListener(v -> {
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
    protected void onDestroy() {
        super.onDestroy();
        instanciaEncerra = null;
    }

    public static void encerra(String status) {
        if (instanciaEncerra != null) {
            instanciaEncerra.finish();
        }
    }
}