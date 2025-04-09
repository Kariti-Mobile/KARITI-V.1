package online.padev.kariti;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class GerarCsv {
    /**
     * Este método gera um arquivo CSV (separado por ponto-e-vírgula) a partir de uma lista de dados.
     * @param dados Lista de vetores de string em que cada vetor corresponderá a uma linha do CSV
     * @param file Arquivo onde serão escritas as linhas do CSV.
     * @throws IOException
     */
    public static void gerar(List<String[]> dados, File file)throws IOException {
        gerar(dados, file, ";");
    }
    /**
     * Este método gera um arquivo CSV a partir de uma lista de dados.
     * @param dados Lista de vetores de string em que cada vetor corresponderá a uma linha do CSV
     * @param file Arquivo onde serão escritas as linhas do CSV.
     * @param separador Caractere que separa cada informação nas linhas
     * @throws IOException
     */
    public static void gerar(List<String[]> dados, File file, String separador)
            throws IOException {
        FileOutputStream fos = new FileOutputStream(file);

        StringBuffer sb = new StringBuffer("");
        for(String[] linha : dados){
            //sb.setLength(0);
            for(int i = 0; i < linha.length; i++){
                if (i > 0){
                    sb.append(separador);
                }
                sb.append(linha[i]);
            }
            sb.append("\n");
        }
        fos.write(sb.toString().getBytes());
        fos.close();
    }
}
