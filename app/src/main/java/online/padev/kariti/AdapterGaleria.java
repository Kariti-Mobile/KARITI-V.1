package online.padev.kariti;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.io.File;

public class AdapterGaleria extends RecyclerView.Adapter<AdapterGaleria.ViewHolder> {
    Context context;
    File diretorio;

    public AdapterGaleria(Context context, File dir) {
        this.context = context;
        this.diretorio = dir;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listagem_galeria, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String nomeFoto = Compactador.listCartoes.get(position);
        String dataFoto = Compactador.datasImgs.get(position);
        String caminhoFoto = diretorio+"/"+nomeFoto;

        // Defina os dados nos elementos de visualização
        holder.nomeDaFoto.setText(nomeFoto);
        holder.dataDaFoto.setText(dataFoto);
        //holder.imageViewGaleria.setImageBitmap(bitPhoto);

        // Carregar a imagem usando Glide
        Glide.with(context)
                .load(caminhoFoto) // Carregar a imagem a partir do caminho do arquivo
                .into(holder.imageViewGaleria);
    }

    @Override
    public int getItemCount() {
        return Compactador.listCartoes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewGaleria;
        TextView nomeDaFoto;
        TextView dataDaFoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewGaleria = itemView.findViewById(R.id.imageViewGaleria);
            nomeDaFoto = itemView.findViewById(R.id.nomeDaFoto);
            dataDaFoto = itemView.findViewById(R.id.dataDafoto);
        }
    }
}