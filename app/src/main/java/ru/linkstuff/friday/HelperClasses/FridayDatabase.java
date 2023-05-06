package ru.linkstuff.friday.HelperClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by alexander on 11.09.17.
 */

public class FridayDatabase extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "friday.db";
    private static final int VERSION = 1;

    private String[] commands = {
            "яркост", "громкост", "перезагруз", "выключ", "сдела", "откр",
            "позвон", "заблокир", "включ", "запуст", "добав"
    };

    private int[] id = {
            // 0 - без аргументов
            // 1 - аргументом является цифра и нужны доп. аргументы
            // 2 - требуются аргументы типа изменения (убавить, прибавить, поставить)
            // 3 - аргументом является приложение
            // 4 - аргументом является часть устройства (wi-fi модуль и т.д.)
            2, 2, 0, 4, 3, 3,
            1, 0, 4, 3, 3
    };

    private String[] extraArgs = {
            "на", "до"
    };

    private String[] argsOfChange = {
            "убав", "прибав", "постав", "добав"
    };

    private String[] apps = {
            "браузер", "изменений", "скриншот", "приложение"
    };

    private String[] deviceName = {
            "вай;wi;wifi;"
    };

    private String[] deviceId = {
            "0" //WI-FI
    };

    public FridayDatabase(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "create table " + DictionarySchema.CommandsTable.NAME + "(" +
                DictionarySchema.CommandsTable.COMMAND_ID + ", " +
                DictionarySchema.CommandsTable.COMMAND + ")"
        );

        sqLiteDatabase.execSQL(
                "create table " + DictionarySchema.AppsTable.NAME + "(" +
                        DictionarySchema.AppsTable.PACKAGE + ", " +
                        DictionarySchema.AppsTable.APP_VARIANTS + ")"
        );

        sqLiteDatabase.execSQL(
                "create table " + DictionarySchema.DevicesTable.NAME + "(" +
                        DictionarySchema.DevicesTable.DEVICE_ID + ", " +
                        DictionarySchema.DevicesTable.DEVICE_VARIANTS + ")"
        );

        sqLiteDatabase.execSQL(
                "create table " + DictionarySchema.ExtraArgsTable.NAME + "(" +
                        DictionarySchema.ExtraArgsTable.EXTRA_ARG + ")"
        );

        sqLiteDatabase.execSQL(
                "create table " + DictionarySchema.ArgsOfChangeTable.NAME + "(" +
                        DictionarySchema.ArgsOfChangeTable.ARG_OF_CHANGE + ")"
        );

        getDefaults(sqLiteDatabase);
    }

    private void getDefaults(SQLiteDatabase sqLiteDatabase){
        ContentValues out = new ContentValues();

        for (int i = 0; i < commands.length; ++i){
            out.put(DictionarySchema.CommandsTable.COMMAND_ID, id[i]);
            out.put(DictionarySchema.CommandsTable.COMMAND, commands[i]);

            sqLiteDatabase.insert(DictionarySchema.CommandsTable.NAME, null, out);
        }

        out = new ContentValues();

        for (String e: extraArgs){
            out.put(DictionarySchema.ExtraArgsTable.EXTRA_ARG, e);

            sqLiteDatabase.insert(DictionarySchema.ExtraArgsTable.NAME, null, out);
        }

        out = new ContentValues();

        for (String c: argsOfChange){
            out.put(DictionarySchema.ArgsOfChangeTable.ARG_OF_CHANGE, c);

            sqLiteDatabase.insert(DictionarySchema.ArgsOfChangeTable.NAME, null, out);
        }

        out = new ContentValues();

        for (int i = 0; i < deviceName.length; ++i){
            out.put(DictionarySchema.DevicesTable.DEVICE_ID, deviceId[i]);
            out.put(DictionarySchema.DevicesTable.DEVICE_VARIANTS, deviceName[i]);

            sqLiteDatabase.insert(DictionarySchema.DevicesTable.NAME, null, out);
        }

        out = new ContentValues();

        for (String a: apps){
            out.put(DictionarySchema.AppsTable.APP_VARIANTS, a + ";");
            out.put(DictionarySchema.AppsTable.PACKAGE, "this_" + a);

            sqLiteDatabase.insert(DictionarySchema.AppsTable.NAME, null, out);
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}

