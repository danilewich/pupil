package com.example.pupil;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Words extends Fragment implements View.OnClickListener {

    Cursor c = null;
    Cursor cc = null;
    Button btn1, btn2, btn3, btn4, btn5, btn6, rightBtn;
    TextView tw;
    private DBHelper myDBHelper;
    private SQLiteDatabase myDB;
    Context thisContext;
    int rightAnswer = -1;
    int rusWId = -1;
    int engWId = -1;
    int curGrp = -1;
    boolean language_change = true;
    boolean one_click = true;
    String rightWord;
    ProgressBar progressBar;
    List<String> groups = new ArrayList<String>();
    //Spinner spinner;

    boolean gameOn = false;
    Handler mHandler;
    long startTime;

    /*Group groupBtn;*/

    public Words() {
    }

    public static Words newInstance() {
        return new Words();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.item_grp);
        Spinner spinner = (Spinner) item.getActionView();

        fillGroup();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(thisContext, R.layout.spiner, groups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String item = (String) parent.getItemAtPosition(position);
                Cursor c = myDB.rawQuery("SELECT ifnull(max(id_group), -1)\n" +
                        "  FROM groups\n" +
                        " WHERE lower(name_group) = ?", new String[]{item.toLowerCase()});
                c.moveToFirst();

                while (!c.isAfterLast()) {
                    curGrp = c.getInt(0);
                    c.moveToNext();
                }
                c.close();
                printWord();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinner.setOnItemSelectedListener(itemSelectedListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_change:
                if (language_change) {
                    language_change = false;
                    printWord();
                } else {
                    language_change = true;
                    printWord();
                }
                break;
            case R.id.item_grp:
                break;
        }
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_words, container, false);
        thisContext = getActivity();

        myDBHelper = new DBHelper(thisContext);
        myDBHelper.updateDataBase();
        myDB = myDBHelper.getWritableDatabase();

        btn1 = (Button) view.findViewById(R.id.btn1);
        btn2 = (Button) view.findViewById(R.id.btn2);
        btn3 = (Button) view.findViewById(R.id.btn3);
        btn4 = (Button) view.findViewById(R.id.btn4);
        btn5 = (Button) view.findViewById(R.id.btn5);
        btn6 = (Button) view.findViewById(R.id.btn6);
        tw = (TextView) view.findViewById(R.id.textView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        /*groupBtn = (Group) view.findViewById(R.id.groupBtn);*/

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn6.setOnClickListener(this);

        printWord();

        return view;
    }

    public void fillGroup() {
        Cursor c = myDB.rawQuery("SELECT *\n" +
                "  FROM (\n" +
                "           SELECT g.name_group as name_group,\n" +
                "                  g.id_group as id_group\n" +
                "             FROM groups g\n" +
                "           UNION\n" +
                "           SELECT 'Все' as name_group,\n" +
                "                -1 as id_group\n" +
                "       )\n" +
                " ORDER BY id_group", null);
        c.moveToFirst();

        while (!c.isAfterLast()) {
            groups.add(c.getString(0).toLowerCase());
            c.moveToNext();
        }
        c.close();
    }

    @Override
    public void onClick(View v) {
        if (one_click) {
            one_click = false;

            switch (v.getId()) {
                case R.id.btn1:
                    selectAnswer(btn1, 1);
                    break;
                case R.id.btn2:
                    selectAnswer(btn2, 2);
                    break;
                case R.id.btn3:
                    selectAnswer(btn3, 3);
                    break;
                case R.id.btn4:
                    selectAnswer(btn4, 4);
                    break;
                case R.id.btn5:
                    selectAnswer(btn5, 5);
                    break;
                case R.id.btn6:
                    selectAnswer(btn6, 6);
                    break;
            }

            startTime = System.currentTimeMillis();
            mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    if (gameOn) {
                        long seconds = ((System.currentTimeMillis() - startTime));
                        progressBar.setProgress((int) seconds);
                    }

                    mHandler.sendEmptyMessageDelayed(0, 10);
                }
            };

            gameOn = true;
            mHandler.sendEmptyMessage(0);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    printWord();
                    one_click = true;
                    gameOn = false;
                }
            }, 1500);
        }
    }

    public void selectAnswer(Button btn, int id) {
        String sql;

        if (rightAnswer == id) {
            btn.setBackgroundResource(R.drawable.btn_right_answer);
            btn.setTextColor(getResources().getColor(R.color.textColorPrimary));
            sql = "INSERT INTO history(id_eng_words, id_rus_word, correctly)"
                    + "VALUES (?, ?, ?)";
            myDB.execSQL(sql, new String[]{String.valueOf(engWId), String.valueOf(rusWId), "Y"});
        } else {
            btn.setBackgroundResource(R.drawable.btn_wrong_answer);
            btn.setTextColor(getResources().getColor(R.color.textColorPrimary));

            rightBtn.setBackgroundResource(R.drawable.btn_right_answer);
            rightBtn.setTextColor(getResources().getColor(R.color.textColorPrimary));

            sql = "INSERT INTO history(id_eng_words, id_rus_word, correctly)"
                    + "VALUES (?, ?, ?)";
            myDB.execSQL(sql, new String[]{String.valueOf(engWId), String.valueOf(rusWId), "N"});
        }
    }

    public void printWord() {

        String engWName = "";
        String rusWName = "";
        int wrong_minus_right = 2;
        int last_time = 5;

        String[] wrongWords = new String[5];
        int a = 1; // начальное значение диапазона для кнопок
        int b = 6; // конечное значение диапазона для кнопок
        rightAnswer = a + (int) (Math.random() * b);

        progressBar.setProgress(0);

        for (int i = 0; i < 2; i++) {

            if (i == 0) {
                wrong_minus_right = 3;
                last_time = 10;
            } else if (i == 1) {
                wrong_minus_right = -100;
                last_time = 4320; // три дня
            }
            c = myDB.rawQuery("SELECT ifnull(max(name_eng_word), -1),\n" +
                    "       _id_eng_word,\n" +
                    "       _id_rus_word,\n" +
                    "       name_rus_word,\n" +
                    "       wrong_minus_right,\n" +
                    "       last_time\n" +
                    "  FROM (\n" +
                    "           SELECT name_eng_word,\n" +
                    "                  _id_eng_word,\n" +
                    "                  _id_rus_word,\n" +
                    "                  name_rus_word,\n" +
                    "                  wrong_minus_right,\n" +
                    "                  last_time\n" +
                    "             FROM (\n" +
                    "                      SELECT name_eng_word,\n" +
                    "                             _id_eng_word,\n" +
                    "                             _id_rus_word,\n" +
                    "                             name_rus_word,\n" +
                    "                             wrong_minus_right,\n" +
                    "                             CAST ( (julianday('now', 'localtime') - julianday(cur_date) ) " +
                    "* 24 * 60 AS INTEGER) AS last_time\n" +
                    "                        FROM (\n" +
                    "                                 SELECT ( (\n" +
                    "                                              SELECT count( * ) \n" +
                    "                                                FROM history h\n" +
                    "                                               WHERE h.correctly = 'N' AND \n" +
                    "                                                     h.id_rus_word = rw._id_rus_word AND \n" +
                    "                                                     h.id_eng_words = ew._id_eng_word\n" +
                    "                                          )\n" +
                    "-                                       (\n" +
                    "                                            SELECT count( * ) \n" +
                    "                                              FROM history h\n" +
                    "                                             WHERE h.correctly = 'Y' AND \n" +
                    "                                                   h.id_rus_word = rw._id_rus_word AND \n" +
                    "                                                   h.id_eng_words = ew._id_eng_word\n" +
                    "                                        )\n" +
                    "                                        ) AS wrong_minus_right,\n" +
                    "                                        (\n" +
                    "                                            SELECT max(cur_date) \n" +
                    "                                              FROM history h\n" +
                    "                                             WHERE h.id_rus_word = rw._id_rus_word AND \n" +
                    "                                                   h.id_eng_words = ew._id_eng_word\n" +
                    "                                        )\n" +
                    "                                        AS cur_date,\n" +
                    "                                        rw.name_rus_word,\n" +
                    "                                        ew.name_eng_word,\n" +
                    "                                        ew._id_eng_word,\n" +
                    "                                        rw._id_rus_word\n" +
                    "                                   FROM rus_words rw,\n" +
                    "                                        rus_eng_words rew,\n" +
                    "                                        eng_words ew\n" +
                    "                                  WHERE rw._id_rus_word = rew.id_rus_word AND \n" +
                    "                                        ew._id_eng_word = rew.id_eng_word\n" +
                    "                                  ORDER BY RANDOM() \n" +
                    "                             )\n" +
                    "                  )\n" +
                    "            WHERE wrong_minus_right > " + wrong_minus_right + " AND \n" +
                    "                  last_time > " + last_time + " \n" +
                    "            ORDER BY RANDOM() \n" +
                    "            LIMIT 1\n" +
                    "       )", null);
            c.moveToFirst();

            if (c.getInt(0) == -1) {
                continue;
            } else break;
        }

        c.moveToFirst();
        if (c.getInt(0) == -1) {
            c = myDB.rawQuery("WITH minus AS (\n" +
                    "    SELECT name_eng_word,\n" +
                    "           _id_eng_word,\n" +
                    "           _id_rus_word,\n" +
                    "           name_rus_word\n" +
                    "      FROM (\n" +
                    "               SELECT name_eng_word,\n" +
                    "                      _id_eng_word,\n" +
                    "                      _id_rus_word,\n" +
                    "                      name_rus_word,\n" +
                    "                      wrong_minus_right,\n" +
                    "                      last_time\n" +
                    "                 FROM (\n" +
                    "                          SELECT name_eng_word,\n" +
                    "                                 _id_eng_word,\n" +
                    "                                 _id_rus_word,\n" +
                    "                                 name_rus_word,\n" +
                    "                                 wrong_minus_right,\n" +
                    "                                 CAST ( (julianday('now', 'localtime') - julianday(cur_date) ) * 24 * 60 AS INTEGER) AS last_time\n" +
                    "                            FROM (\n" +
                    "                                     SELECT ( (\n" +
                    "                                                  SELECT count( * ) \n" +
                    "                                                    FROM history h\n" +
                    "                                                   WHERE h.correctly = 'N' AND \n" +
                    "                                                         h.id_rus_word = rw._id_rus_word AND \n" +
                    "                                                         h.id_eng_words = ew._id_eng_word\n" +
                    "                                              )\n" +
                    "-                                           (\n" +
                    "                                                SELECT count( * ) \n" +
                    "                                                  FROM history h\n" +
                    "                                                 WHERE h.correctly = 'Y' AND \n" +
                    "                                                       h.id_rus_word = rw._id_rus_word AND \n" +
                    "                                                       h.id_eng_words = ew._id_eng_word\n" +
                    "                                            )\n" +
                    "                                            ) AS wrong_minus_right,\n" +
                    "                                            (\n" +
                    "                                                SELECT max(cur_date) \n" +
                    "                                                  FROM history h\n" +
                    "                                                 WHERE h.id_rus_word = rw._id_rus_word AND \n" +
                    "                                                       h.id_eng_words = ew._id_eng_word\n" +
                    "                                            )\n" +
                    "                                            AS cur_date,\n" +
                    "                                            rw.name_rus_word,\n" +
                    "                                            ew.name_eng_word,\n" +
                    "                                            ew._id_eng_word,\n" +
                    "                                            rw._id_rus_word\n" +
                    "                                       FROM rus_words rw,\n" +
                    "                                            rus_eng_words rew,\n" +
                    "                                            eng_words ew\n" +
                    "                                      WHERE rw._id_rus_word = rew.id_rus_word AND \n" +
                    "                                            ew._id_eng_word = rew.id_eng_word\n" +
                    "                                      ORDER BY RANDOM() \n" +
                    "                                 )\n" +
                    "                      )\n" +
                    "                WHERE wrong_minus_right < -4 AND \n" +
                    "                      last_time < 2880\n" +
                    "           )\n" +
                    ")\n" +
                    "SELECT *\n" +
                    "  FROM (\n" +
                    "           SELECT ew.name_eng_word,\n" +
                    "                  ew._id_eng_word,\n" +
                    "                  rw._id_rus_word,\n" +
                    "                  rw.name_rus_word\n" +
                    "             FROM eng_words ew\n" +
                    "                  LEFT JOIN\n" +
                    "                  rus_eng_words rew ON ew._id_eng_word = rew.id_eng_word\n" +
                    "                  LEFT JOIN\n" +
                    "                  rus_words rw ON rw._id_rus_word = rew.id_rus_word\n" +
                    "                  LEFT JOIN\n" +
                    "                  groups_eng_words gew ON gew.id_eng_word = ew._id_eng_word\n" +
                    "                  LEFT JOIN\n" +
                    "                  groups g ON g.id_group = gew.id_group\n" +
                    "            WHERE (g.id_group = " + curGrp + " OR \n" +
                    "                    -1 = + " + curGrp + ") \n" +
                    "           EXCEPT\n" +
                    "           SELECT *\n" +
                    "             FROM minus\n" +
                    "       )\n" +
                    " ORDER BY RANDOM() \n" +
                    " LIMIT 1", null);
        }
        c.moveToFirst();

        while (!c.isAfterLast()) {
            engWName = c.getString(0);
            rusWName = c.getString(3);
            rusWId = c.getInt(2);
            engWId = c.getInt(1);
            c.moveToNext();
        }
        c.close();

        if (language_change) {
            c = myDB.rawQuery("SELECT rw._id_rus_word,\n" +
                    "       rw.name_rus_word\n" +
                    "  FROM rus_words rw\n" +
                    " WHERE rw._id_rus_word <> " + rusWId + " \n" +
                    " ORDER BY RANDOM() \n" +
                    " LIMIT 5", null);
        } else {
            c = myDB.rawQuery("SELECT ew._id_eng_word,\n" +
                    "       ew.name_eng_word\n" +
                    "  FROM eng_words ew\n" +
                    " WHERE ew._id_eng_word <> " + engWId + " \n" +
                    " ORDER BY RANDOM() \n" +
                    " LIMIT 5", null);
        }
        c.moveToFirst();

        for (int i = 0; i < 5; i++) {
            wrongWords[i] = c.getString(1);
            c.moveToNext();
        }
        c.close();

        btn1.setBackgroundResource(R.drawable.btn_design);
        btn1.setTextColor(getResources().getColor(R.color.textColor));
        btn2.setBackgroundResource(R.drawable.btn_design);
        btn2.setTextColor(getResources().getColor(R.color.textColor));
        btn3.setBackgroundResource(R.drawable.btn_design);
        btn3.setTextColor(getResources().getColor(R.color.textColor));
        btn4.setBackgroundResource(R.drawable.btn_design);
        btn4.setTextColor(getResources().getColor(R.color.textColor));
        btn5.setBackgroundResource(R.drawable.btn_design);
        btn5.setTextColor(getResources().getColor(R.color.textColor));
        btn6.setBackgroundResource(R.drawable.btn_design);
        btn6.setTextColor(getResources().getColor(R.color.textColor));

        //groupBtn.setBackgroundResource(R.drawable.btn_wrong_answer);

        if (language_change) {
            tw.setText(engWName);
            rightWord = rusWName;
        } else {
            tw.setText(rusWName);
            rightWord = engWName;
        }

        switch (rightAnswer) {
            case 1:
                btn1.setText(rightWord);
                rightBtn = btn1;
                btn2.setText(wrongWords[0]);
                btn3.setText(wrongWords[1]);
                btn4.setText(wrongWords[2]);
                btn5.setText(wrongWords[3]);
                btn6.setText(wrongWords[4]);
                break;
            case 2:
                btn2.setText(rightWord);
                rightBtn = btn2;
                btn1.setText(wrongWords[0]);
                btn3.setText(wrongWords[1]);
                btn4.setText(wrongWords[2]);
                btn5.setText(wrongWords[3]);
                btn6.setText(wrongWords[4]);
                break;
            case 3:
                btn3.setText(rightWord);
                rightBtn = btn3;
                btn2.setText(wrongWords[0]);
                btn1.setText(wrongWords[1]);
                btn4.setText(wrongWords[2]);
                btn5.setText(wrongWords[3]);
                btn6.setText(wrongWords[4]);
                break;
            case 4:
                btn4.setText(rightWord);
                rightBtn = btn4;
                btn2.setText(wrongWords[0]);
                btn3.setText(wrongWords[1]);
                btn1.setText(wrongWords[2]);
                btn5.setText(wrongWords[3]);
                btn6.setText(wrongWords[4]);
                break;
            case 5:
                btn5.setText(rightWord);
                rightBtn = btn5;
                btn2.setText(wrongWords[0]);
                btn3.setText(wrongWords[1]);
                btn4.setText(wrongWords[2]);
                btn1.setText(wrongWords[3]);
                btn6.setText(wrongWords[4]);
                break;
            case 6:
                btn6.setText(rightWord);
                rightBtn = btn6;
                btn2.setText(wrongWords[0]);
                btn3.setText(wrongWords[1]);
                btn4.setText(wrongWords[2]);
                btn5.setText(wrongWords[3]);
                btn1.setText(wrongWords[4]);
                break;
        }
    }
}

