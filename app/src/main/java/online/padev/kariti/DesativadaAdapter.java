package online.padev.kariti;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import online.padev.kariti.R;

import java.util.ArrayList;

public class DesativadaAdapter extends ArrayAdapter<String> {

    private ArrayList<String> escolas, ids;

    private Context context;

    public DesativadaAdapter(Context context, ArrayList<String> escolas, ArrayList<String> ids) {
        super(context, R.layout.custom_escola_desativada, escolas);
        this.context = context;
        this.escolas = escolas;
        this.ids = ids;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.custom_escola_desativada, null);
        }

        TextView textViewNome = view.findViewById(R.id.textViewNome);
        ImageView imageViewIcon = view.findViewById(R.id.imageViewIcon);

        String nomeEscola = escolas.get(position);
        String idEscola = ids.get(position);
        textViewNome.setText(nomeEscola);


        return view;
    }
}
