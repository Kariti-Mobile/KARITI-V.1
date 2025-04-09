package online.padev.kariti;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import online.padev.kariti.R;

import java.util.ArrayList;

public class SpinnerAdapter extends ArrayAdapter<String> {
    private ArrayList<String> aluno;
    private Context context;
    private LayoutInflater mInflater;

    public SpinnerAdapter(Context context, ArrayList<String> aluno) {
        super(context, R.layout.spinner_select, aluno);
        this.context = context;
        this.aluno = aluno;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.spinner_select, parent, false);
        TextView textView = view.findViewById(R.id.text_viewAluno);
        textView.setText(aluno.get(position));


        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

}
