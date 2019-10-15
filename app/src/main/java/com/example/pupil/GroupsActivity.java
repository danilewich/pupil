package com.example.pupil;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class GroupsActivity extends AppCompatActivity {

    private ActionBar toolbar;
    ImageButton btnAddGrp;
    Button clrGrp;
    TextInputEditText etAddGrp;
    private DBHelper myDBHelper;
    private SQLiteDatabase myDB;
    private Context thisContext;
    LinearLayout llGroups;
    List<CheckBox> allCbs = new ArrayList<CheckBox>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        toolbar = getSupportActionBar();
        thisContext = this;
        toolbar.setTitle(R.string.title_groups);

        myDBHelper = new DBHelper(this);
        myDBHelper.updateDataBase();
        myDB = myDBHelper.getWritableDatabase();
        llGroups = (LinearLayout) findViewById(R.id.llgrp);
        btnAddGrp = (ImageButton) findViewById(R.id.btn_add_grp);
        etAddGrp = (TextInputEditText) findViewById(R.id.et_add_grp);
        clrGrp = (Button) findViewById(R.id.clr_grp);
        createCB();

        View.OnClickListener oclBtnAddGrp = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameGrp = etAddGrp.getText().toString().trim().toLowerCase();
                if(!nameGrp.equals("")) {
                    String sql = "INSERT INTO groups(name_group)"
                            + "VALUES (?)";
                    myDB.execSQL(sql, new String[]{nameGrp});
                    Toast toast = Toast.makeText(thisContext, "Группа добавлена!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    llGroups.removeAllViews();
                    createCB();
                } else {
                    Toast toast = Toast.makeText(thisContext, "Введите группу!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        };

        View.OnClickListener oclBtnClrGrp = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int size = allCbs.size();
                boolean checked = false;
                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        if (allCbs.get(i).isChecked()) {
                            String sql = "DELETE FROM groups\n" +
                                    "      WHERE id_group = ?";
                            myDB.execSQL(sql, new Integer[]{allCbs.get(i).getId()});
                            checked = true;
                        }
                    }

                    Toast toast;
                    if (checked) {
                        toast = Toast.makeText(thisContext, "Группы удалены!", Toast.LENGTH_SHORT);
                        llGroups.removeAllViews();
                        createCB();
                    }
                    else toast = Toast.makeText(thisContext, "Группы не выбраны!", Toast.LENGTH_SHORT);

                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        };

        btnAddGrp.setOnClickListener(oclBtnAddGrp);
        clrGrp.setOnClickListener(oclBtnClrGrp);
    }

    private void createCB() {

        Cursor c = myDB.rawQuery("SELECT g.name_group, g.id_group\n" +
                "  FROM groups g", null);
        c.moveToFirst();
        CheckBox checkbox;

        while (!c.isAfterLast()) {
            checkbox = new CheckBox(thisContext);
            allCbs.add(checkbox);
            checkbox.setText(c.getString(0).toLowerCase());
            checkbox.setTextSize(23);
            checkbox.setId(c.getInt(1));
            LinearLayout.LayoutParams CL = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            CL.topMargin = 10;
            llGroups.addView(checkbox, CL);
            c.moveToNext();
        }
        c.close();
    }

}
