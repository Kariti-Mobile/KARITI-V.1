package online.padev.kariti;

import android.os.Environment;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadEjson {
    static Integer id_prova, id_aluno, resultCorrect, questao, respostaDada, questAnterior, respostaAnterior;
    static String mensagem, respostaDupla;
    public static void enviarArquivosP(File arquivo, FileOutputStream fos, File dir, BancoDados bancoDados) {
        Thread thread = new Thread(() -> {
            try {
                String URL = "http://kariti.online/src/pages/test/correct_test/core.php";
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(URL);

                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                FileBody x = new FileBody(arquivo);

                entityBuilder.addPart("userfile[]", x);
                HttpEntity entity = entityBuilder.build();
                post.setEntity(entity);
                HttpResponse response = client.execute(post);
                HttpEntity httpEntity = response.getEntity();
                InputStream is = httpEntity.getContent();
                int inByte;
                while((inByte = is.read()) != -1)
                    fos.write(inByte);
                is.close();
                fos.close();
                Log.e("kariti","passei 0");
                UploadEjson.fimUpload(dir, bancoDados);
            } catch (Exception e) {
                Log.e("kariti","passei 000");
                Log.e("kariti","Erro: "+e.getMessage());
                AnimacaoCorrecao.encerra("erro");
            }
        });
        thread.start();
    }
    public static void fimUpload(File dir, BancoDados bancoDados){
        try {
            String situacao = Environment.getExternalStorageState();
            if (situacao.equals(Environment.MEDIA_MOUNTED)) {
                String result = GaleriaActivity.leitor(dir+"/json.json");
                JSONArray json = new JSONArray(result);
                List<Object[]> provas = new ArrayList<>();
                for (int x = 0; x < json.length(); x++){
                    JSONObject objJson = json.getJSONObject(x);
                    resultCorrect = objJson.getInt("resultado");
                    id_prova = objJson.getInt("id_prova");
                    id_aluno = objJson.getInt("id_aluno");
                    mensagem = objJson.getString("mensagem");
                    Map<Integer, Integer> respostasProva = new HashMap<>();

                    if(resultCorrect.equals(0)){

                        mensagem = mensagem.replaceAll("\\),\\(", ");(");
                        mensagem = mensagem.replaceAll("\\)", "");
                        mensagem = mensagem.replaceAll("\\(", "");
                        mensagem = mensagem.replaceAll(" ", "");
                        String[] itens = mensagem.split(";");

                        for(String item : itens) { //A cada interação uma questão e sua respectiva resposta
                            String[] sep = item.split(",");
                            questao = Integer.valueOf(sep[0]); //pega a questão
                            respostaDada = Integer.valueOf(sep[1]);//pega a resposta
                            if (questao.equals(questAnterior)) { // Em caso de mais de uma alternativas marcadas para uma questão
                                respostaDupla = (respostaAnterior.toString()) + (respostaDada.toString()); // Concatenando as duas respostas
                                respostaDada = Integer.valueOf(respostaDupla);
                            }
                            respostasProva.put(questao, respostaDada);

                            questAnterior = questao;
                            respostaAnterior = respostaDada;
                        }
                    }else if(!bancoDados.verificaExisteCorrecaoAluno(id_prova, id_aluno)){
                        respostasProva.put(-1, -1);
                    }else{
                        continue;
                    }
                    provas.add(new Object[]{id_prova, id_aluno, respostasProva});
                }
                if (bancoDados.cadastrarCorrecao(provas)){
                    Log.e("kariti","passei 1");
                    AnimacaoCorrecao.encerra("sucesso");

                }else{
                    Log.e("kariti","passei 2");
                    AnimacaoCorrecao.encerra("erro");
                }
            }
        }catch (Exception e){
            Log.e("Kariti", e.toString());
            Log.e("kariti","passei 3");
            AnimacaoCorrecao.encerra("erro");
        }
    }
}