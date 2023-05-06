package ru.linkstuff.friday.Splitter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ru.linkstuff.friday.HelperClasses.DictionarySchema;
import ru.linkstuff.friday.HelperClasses.FridayDatabase;

public class Dictionary {
    private SQLiteDatabase database;
    private Cursor cursor;

    public Dictionary(Context context){
        database = new FridayDatabase(context).getWritableDatabase();
    }

    public TemporaryDictionary findCommand(String command){
        cursor = database.query(
                DictionarySchema.CommandsTable.NAME,
                new String[]{
                        DictionarySchema.CommandsTable.COMMAND,
                        DictionarySchema.CommandsTable.COMMAND_ID
                },
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()){
            if (command.contains(cursor.getString(cursor.getColumnIndex(DictionarySchema.CommandsTable.COMMAND)))){
                TemporaryDictionary out = new TemporaryDictionary(
                        cursor.getString(cursor.getColumnIndex(DictionarySchema.CommandsTable.COMMAND)),
                        cursor.getInt(cursor.getColumnIndex(DictionarySchema.CommandsTable.COMMAND_ID))
                );

                cursor.close();

                return out;
            }
        }

        cursor.close();

        return null;
    }

    public String findDevice(String device){
        cursor = database.query(
                DictionarySchema.DevicesTable.NAME,
                new String[]{
                        DictionarySchema.DevicesTable.DEVICE_VARIANTS,
                        DictionarySchema.DevicesTable.DEVICE_ID
                },
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()){
            if (cursor.getString(cursor.getColumnIndex(DictionarySchema.DevicesTable.DEVICE_VARIANTS)).contains(device)){
                device = cursor.getString(cursor.getColumnIndex(DictionarySchema.DevicesTable.DEVICE_ID));

                cursor.close();

                return device;
            }
        }

        return null;
    }

    public String findApp(String app){
        cursor = database.query(
                DictionarySchema.AppsTable.NAME,
                new String[]{
                        DictionarySchema.AppsTable.APP_VARIANTS,
                        DictionarySchema.AppsTable.PACKAGE
                },
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()){
            if (cursor.getString(cursor.getColumnIndex(DictionarySchema.AppsTable.APP_VARIANTS)).contains(app)){
                app = cursor.getString(cursor.getColumnIndex(DictionarySchema.AppsTable.PACKAGE));

                cursor.close();

                return app;
            }
        }

        return null;
    }

    public String findArgOfChange(String argOfChange){
        cursor = database.query(
                DictionarySchema.ArgsOfChangeTable.NAME,
                new String[]{
                        DictionarySchema.ArgsOfChangeTable.ARG_OF_CHANGE
                },
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()){
            if (argOfChange.contains(cursor.getString(cursor.getColumnIndex(DictionarySchema.ArgsOfChangeTable.ARG_OF_CHANGE)))){
                argOfChange = cursor.getString(cursor.getColumnIndex(DictionarySchema.ArgsOfChangeTable.ARG_OF_CHANGE));
                cursor.close();

                return argOfChange;
            }
        }

        cursor.close();
        return null;
    }

    public String findExtraArg(String extraArg){
        cursor = database.query(
                DictionarySchema.ExtraArgsTable.NAME,
                new String[]{
                        DictionarySchema.ExtraArgsTable.EXTRA_ARG
                },
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()){
            if (extraArg.equals(cursor.getString(cursor.getColumnIndex(DictionarySchema.ExtraArgsTable.EXTRA_ARG)))){

                cursor.close();

                return extraArg;
            }
        }

        cursor.close();

        return null;
    }

    public void closeDatabase(){
        database.close();
    }

}
