package online.padev.kariti;

import java.util.Random;

public class GerarCodigoValidacao {
    /**
     * Este método gera um cogigo aleatório de 4 digitos para validação de cadastro de usuário
     * @return retorna uma string de 4 digítos numéricos
     */
    public String gerarVerificador(){
        Random r = new Random();
        String saida = "";
        for(int i = 0; i < 4; i++) {
            saida += "" + r.nextInt(10);
        }
        return saida;
    }

}
