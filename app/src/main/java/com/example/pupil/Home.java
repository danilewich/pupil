package com.example.pupil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Home extends Fragment implements View.OnClickListener {

    private Button btnAdd;
    private TextInputLayout etEng, etRus;
    private Cursor mainCursor;
    private DBHelper myDBHelper;
    private SQLiteDatabase myDB;
    private Context thisContext;
    private LinearLayout llMain;
    List<CheckBox> allCbs = new ArrayList<CheckBox>();

    public Home() {
    }

    public static Home newInstance() {
        return new Home();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        thisContext = getActivity();

        myDBHelper = new DBHelper(thisContext);
        myDBHelper.updateDataBase();
        myDB = myDBHelper.getWritableDatabase();

        btnAdd = (Button) view.findViewById(R.id.buttonAdd);
        etRus = (TextInputLayout) view.findViewById(R.id.editTextRus);
        etEng = (TextInputLayout) view.findViewById(R.id.editTextEng);
        llMain = (LinearLayout) view.findViewById(R.id.ll_main);
        createCB();
        btnAdd.setOnClickListener(this);

        return view;
    }

    public void createCB() {

        Cursor c = myDB.rawQuery("SELECT g.name_group, g.id_group\n" +
                "  FROM groups g", null);
        c.moveToFirst();
        CheckBox checkbox;

        while (!c.isAfterLast()) {

            checkbox = new CheckBox(thisContext);
            allCbs.add(checkbox);
            checkbox.setText(c.getString(0).toLowerCase());
            checkbox.setTextSize(23);
            checkbox.setTextColor(getResources().getColor(R.color.textColor));
            checkbox.setId(c.getInt(1));
            LinearLayout.LayoutParams CL = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            CL.topMargin = 10;
            llMain.addView(checkbox, CL);
            c.moveToNext();
        }
        c.close();
    }

    @Override
    public void onClick(View v) {

        String engWord = etEng.getEditText().getText().toString().trim().toLowerCase();
        String rusWord = etRus.getEditText().getText().toString().trim().toLowerCase();
        int newEngId = -1;
        int newRusId = -1;
        String sql;

        ContentValues contentValues = new ContentValues();

        switch (v.getId()) {
            case R.id.buttonAdd:

                if (etEng.getEditText().getText().toString().trim().isEmpty()) {
                    etEng.setError("Введите английское слово");
                    etEng.requestFocus();
                    break;
                } else {
                    etEng.setError(null);
                }

                if (etRus.getEditText().getText().toString().trim().isEmpty()) {
                    etRus.setError("Введите русское слово");
                    etRus.requestFocus();
                    break;
                } else {
                    etRus.setError(null);
                }

                mainCursor = myDB.rawQuery("SELECT ifnull(max(1), -1) \n" +
                        "  FROM eng_words ew, \n" +
                        "       rus_eng_words rew, \n" +
                        "       rus_words rw \n" +
                        " WHERE ew._id_eng_word = rew.id_eng_word AND \n" +
                        "       rw._id_rus_word = rew.id_rus_word AND \n" +
                        "       lower(trim(rw.name_rus_word)) = ? AND \n" +
                        "       lower(trim(ew.name_eng_word)) = ?", new String[]{rusWord, engWord});
                mainCursor.moveToFirst();

                while (!mainCursor.isAfterLast()) {
                    if (mainCursor.getInt(0) == 1) {
                        Toast toast = Toast.makeText(thisContext, "Пара слов уже есть в словаре!", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        etEng.requestFocus();
                    } else {

                        Cursor c;
                        Cursor cc;

                        c = myDB.rawQuery("SELECT (\n" +
                                "           SELECT ifnull(max(ew._id_eng_word), -1) \n" +
                                "             FROM eng_words ew \n" +
                                "            WHERE lower(trim(ew.name_eng_word)) = ? \n" +
                                "       ),\n" +
                                "       (\n" +
                                "           SELECT ifnull(max(rw._id_rus_word), -1) \n" +
                                "             FROM rus_words rw \n" +
                                "            WHERE lower(trim(rw.name_rus_word)) = ? \n" +
                                "       )", new String[]{engWord, rusWord});
                        c.moveToFirst();

                        while (!c.isAfterLast()) {
                            // английское - нет, русское - есть
                            if (c.getString(0).equals("-1") && !c.getString(1).equals("-1")) {
                                sql = "INSERT INTO eng_words(name_eng_word)"
                                        + "VALUES (?)";
                                myDB.execSQL(sql, new String[]{engWord});


                                cc = myDB.rawQuery("SELECT ifnull(max(ew._id_eng_word), -1) " +
                                        "                  FROM eng_words ew" +
                                        "                 WHERE lower(trim(ew.name_eng_word)) = ?", new String[]{engWord});
                                cc.moveToFirst();

                                while (!cc.isAfterLast()) {
                                    if (cc.getString(0).equals("-1")) {
                                        Log.d("mLog", "Error 20001 Ошибка поиска английского слова");
                                        newEngId = -1;
                                    } else {
                                        newEngId = cc.getInt(0);
                                    }
                                    cc.moveToNext();
                                }
                                cc.close();

                                newRusId = c.getInt(1);
                                sql = "INSERT INTO rus_eng_words(id_eng_word, id_rus_word)"
                                        + "VALUES (?, ?)";
                                myDB.execSQL(sql, new Integer[]{newEngId, newRusId});
                            }
                            // английское - есть, русское - нет
                            else if (!c.getString(0).equals("-1") && c.getString(1).equals("-1")) {
                                sql = "INSERT INTO rus_words(name_rus_word)"
                                        + "VALUES (?)";
                                myDB.execSQL(sql, new String[]{rusWord});

                                cc = myDB.rawQuery("SELECT ifnull(max(rw._id_rus_word), -1) " +
                                        "                 FROM rus_words rw" +
                                        "                 WHERE lower(trim(rw.name_rus_word)) = ?", new String[]{rusWord});
                                cc.moveToFirst();
                                while (!cc.isAfterLast()) {
                                    if (cc.getString(0).equals("-1")) {
                                        Log.d("mLog", "Ошибка поиска русского слова");
                                        newRusId = -1;
                                    } else {
                                        newRusId = cc.getInt(0);
                                    }
                                    cc.moveToNext();
                                }
                                cc.close();

                                newEngId = c.getInt(0);
                                sql = "INSERT INTO rus_eng_words(id_eng_word, id_rus_word)"
                                        + "VALUES (?, ?)";
                                myDB.execSQL(sql, new Integer[]{newEngId, newRusId});
                            } else {
                                sql = "INSERT INTO eng_words(name_eng_word)"
                                        + "VALUES (?)";
                                myDB.execSQL(sql, new String[]{engWord});

                                sql = "INSERT INTO rus_words(name_rus_word)"
                                        + "VALUES (?)";
                                myDB.execSQL(sql, new String[]{rusWord});

                                cc = myDB.rawQuery("SELECT ifnull(max(ew._id_eng_word), -1) " +
                                        "                 FROM eng_words ew" +
                                        "                 WHERE lower(trim(ew.name_eng_word)) = ?", new String[]{engWord});
                                cc.moveToFirst();
                                while (!cc.isAfterLast()) {
                                    if (cc.getString(0).equals("-1")) {
                                        Log.d("mLog", "Ошибка поиска английского слова");
                                        newEngId = -1;
                                    } else {
                                        newEngId = cc.getInt(0);
                                    }
                                    cc.moveToNext();
                                }
                                cc.close();

                                cc = myDB.rawQuery("SELECT ifnull(max(rw._id_rus_word), -1) " +
                                        "                 FROM rus_words rw" +
                                        "                 WHERE lower(trim(rw.name_rus_word)) = ?", new String[]{rusWord});
                                cc.moveToFirst();
                                while (!cc.isAfterLast()) {
                                    if (cc.getInt(0) == -1) {
                                        Log.d("mLog", "Ошибка поиска русского слова");
                                        newRusId = -1;
                                    } else {
                                        newRusId = cc.getInt(0);
                                    }
                                    cc.moveToNext();
                                }
                                cc.close();

                                sql = "INSERT INTO rus_eng_words(id_eng_word, id_rus_word)"
                                        + "VALUES (?, ?)";
                                myDB.execSQL(sql, new Integer[]{newEngId, newRusId});
                            }
                            c.moveToNext();
                        }
                        Toast toast = Toast.makeText(thisContext, "Пара добавлена в словарь", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                        int size = allCbs.size();
                        for (int i = 0; i < size; i++) {
                            if (allCbs.get(i).isChecked()) {
                                sql = "INSERT INTO groups_eng_words(id_group, id_eng_word)"
                                        + "VALUES (?, ?)";
                                myDB.execSQL(sql, new Integer[]{allCbs.get(i).getId(), newEngId});
                            }
                        }
                    }
                    mainCursor.moveToNext();
                }
                mainCursor.close();
                break;
        }
        //myDBHelper.close();
    }
}
