package com.example.pupil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class Settings extends Fragment implements View.OnClickListener {

    AlertDialog.Builder ad;
    Context thisContext;
    Button btnClear;
    private DBHelper myDBHelper;
    private SQLiteDatabase myDB;

    public Settings() {
    }

    public static Settings newInstance() {
        return new Settings();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        thisContext = getActivity();

        myDBHelper = new DBHelper(thisContext);
        myDBHelper.updateDataBase();
        myDB = myDBHelper.getWritableDatabase();

        btnClear = (Button) view.findViewById(R.id.btn_clear);

        btnClear.setOnClickListener(this);

        return view;
    }

    private void createTwoButtonsAlertDialog(String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(thisContext);
        builder.setTitle(title);
        builder.setMessage(content);
        builder.setNegativeButton("ДА",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteHistoryAnswer();
                        Toast toast = Toast.makeText(thisContext, "История удалена", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                });
        builder.setPositiveButton("НЕТ",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }

    public void deleteHistoryAnswer() {
        String sql;
        sql = "DELETE FROM history";
        myDB.execSQL(sql);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_clear:
                createTwoButtonsAlertDialog("Вы уверены, что хотите очистить историю ответов?", "");
        }
    }
}
