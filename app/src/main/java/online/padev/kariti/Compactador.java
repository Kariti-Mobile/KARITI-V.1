package online.padev.kariti;

import android.util.Log;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.List;
import java.util.ArrayList;
//compactar arquivos para enviar
public class Compactador{
    public static ArrayList<String> listCartoes = new ArrayList<>();
    public static ArrayList<String> datasImgs = new ArrayList<>();
    public static boolean compactador(File diretorioImg, String caminhoZip){
        List<String> arquivos = new ArrayList<>();
        for(String cartao : listCartoes){
            arquivos.add(diretorioImg+"/"+cartao); //Carregando as imagens
        }
        return compactar(caminhoZip, arquivos); //retorna true se funcionou
    }
    public static boolean compactar(String arquivoSaida, List<String> arquivosParaCompactar){
        try{
            FileOutputStream fos = new FileOutputStream(arquivoSaida);
            ZipOutputStream zipOut = new ZipOutputStream(fos);

            for(String sourceFile : arquivosParaCompactar){
                File fileToZip = new File(sourceFile);
                FileInputStream fis = new FileInputStream(fileToZip);
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                fis.close();
            }
            zipOut.close();
            fos.close();
            Log.e("kariti", "Arquivos compactados com sucesso");
            return true;
        }catch(Exception e){
            Log.e("KARITI", "Erro de compactação: "+e.getMessage());
            return false;
        }
    }
}