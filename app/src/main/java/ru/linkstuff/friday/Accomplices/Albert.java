package ru.linkstuff.friday.Accomplices;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.linkstuff.friday.HelperClasses.DictionarySchema;
import ru.linkstuff.friday.HelperClasses.FridayDatabase;
import ru.linkstuff.friday.HelperClasses.ItemDecoration;
import ru.linkstuff.friday.R;
import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.Recognition;
import ru.yandex.speechkit.Recognizer;
import ru.yandex.speechkit.RecognizerListener;
import ru.yandex.speechkit.SpeechKit;

import static android.Manifest.permission.RECORD_AUDIO;

public class Albert extends Activity implements RecognizerListener{

    //Глобальные переменные
    private final String API_KEY = "6f29195e-0ac8-4979-ad8f-b70398be65b8";
    private Recognizer recognizer;
    private Context context;
    private String result;
    private LinearLayoutManager layoutManager;
    private boolean haveDbRecords;
    private String MESSAGE;

    //Переменные статуса загрузки вариантов
    String STATUS;
    String STATUS_LOADING;
    String STATUS_NOT_FOUND;
    String STATUS_LOADED;
    String STATUS_SELECT;
    String STATUS_SAVED;

    //Переменные Toolbar'а
    private ImageView appIcon;
    private TextView appName;
    private Toolbar toolbar;

    private RecyclerView appList;
    private AppAdapter adapter;

    private float toToolbarBottom;
    private float toToolbarTop;
    private float startY;
    private float startInfoLayoutY;
    private float heightToToolbar;
    private float maxY;

    private int statusBarHeight = 0;

    private Drawable[] toolbarBackgrounds;

    //Переменные layout'а информации о приложении
    private RelativeLayout informationLayout;
    private TextView infoTitle;
    Button addVariant;

    private RecyclerView appVariants;
    private AppVariantAdapter variantAdapter;
    private List<String> variantItems = new ArrayList<>();

    private AlertDialog addDialog;

    private String packageName;

    //Переменные базы данных
    private SQLiteDatabase database;
    private Cursor cursor;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.albert_layout);

        context = getApplicationContext();
        statusBarHeight = getStatusBarHeight();

        loadToolbarBackgrounds();

        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        maxY = point.y;

    }

    @Override
    protected void onStart() {
        super.onStart();

        MESSAGE = getResources().getString(R.string.albert_add_message_help);
        STATUS = getResources().getString(R.string.albert_title_status);
        STATUS_LOADING = getResources().getString(R.string.albert_status_loading);
        STATUS_NOT_FOUND = getResources().getString(R.string.albert_status_fail);
        STATUS_LOADED = getResources().getString(R.string.albert_status_success);
        STATUS_SELECT = getResources().getString(R.string.albert_status_noselect);
        STATUS_SAVED = getResources().getString(R.string.albert_status_saved);

        appList = findViewById(R.id.app_list);
        appIcon = findViewById(R.id.app_icon);
        appName = findViewById(R.id.app_name);
        appName.setText(getResources().getString(R.string.albert_toolbar_help));
        toolbar = findViewById(R.id.albert_toolbar);
        informationLayout = findViewById(R.id.info_layout);
        appVariants = findViewById(R.id.albert_app_variants);
        infoTitle = findViewById(R.id.albert_status_title);
        appList.getLayoutParams().height = (int) maxY - statusBarHeight - toolbar.getHeight();
        appList.setY(0 - maxY - statusBarHeight - toolbar.getHeight());
        addVariant = findViewById(R.id.albert_add_variant);
        layoutManager = new LinearLayoutManager(this);

        appList.setLayoutManager(layoutManager);
        appList.addItemDecoration(new ItemDecoration(getResources().getDrawable(R.drawable.item_line_dark)));
        infoTitle.setText(STATUS + " " + STATUS_SELECT);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        toolbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, MotionEvent motionEvent) {
                float y = motionEvent.getRawY();

                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        startY = view.getY();
                        toToolbarTop = y - startY;
                        toToolbarBottom = view.getY() + view.getHeight() - y + statusBarHeight;
                        if (heightToToolbar == 0){
                            startInfoLayoutY = informationLayout.getY();
                            heightToToolbar = startInfoLayoutY - startY - view.getHeight();
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (y - toToolbarTop >= 0 && view.getY() >= 0 && y + toToolbarBottom <= maxY && view.getY() + view.getHeight() <= maxY){
                            view.setY(y - toToolbarTop);
                            informationLayout.setY(view.getY() + view.getHeight() + heightToToolbar + (informationLayout.getY() - startInfoLayoutY) / 3);
                        } else if (view.getY() > 0 && y - toToolbarTop <= 0){
                            view.setY(0);
                            informationLayout.setY(0 + view.getHeight() + heightToToolbar);
                        } else if (view.getY() + view.getHeight() < maxY && y + toToolbarBottom >= maxY){
                            view.setY(maxY - view.getHeight() - statusBarHeight);
                            informationLayout.setY(maxY - statusBarHeight - heightToToolbar);
                        }

                        appList.setY(view.getY() - appList.getHeight());

                        //toolbar.setBackground(toolbarBackgrounds[(int) ((toolbar.getY() + toolbar.getHeight()) / ((maxY - statusBarHeight) / 11)) - 1]);

                        break;

                    case MotionEvent.ACTION_UP:
                        if (startY < y && (view.getY() + view.getHeight()) / maxY * 100 > 20) animateToolbar(maxY - view.getHeight() - statusBarHeight, 350);
                        else animateToolbar(0, 350);

                        break;
                }

                return false;

            }
        });

        addVariant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (appIcon.getVisibility() == View.GONE) Toast.makeText(context, getResources().getString(R.string.albert_add_request), Toast.LENGTH_SHORT).show();
                else {
                    SpeechKit.getInstance().configure(context, API_KEY);

                    AlertDialog.Builder builder = new AlertDialog.Builder(Albert.this);
                    builder.setTitle(getResources().getString(R.string.albert_add_message) + " " + getResources().getString(R.string.albert_add_recognizer_loading));
                    builder.setMessage(getResources().getString(R.string.albert_add_message_help));
                    builder.setPositiveButton(getResources().getString(R.string.albert_add_button_positive), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            resetRecognizer();
                            variantItems.add(result);
                            variantAdapter.notifyDataSetChanged();
                            saveVariants();
                            dialogInterface.dismiss();
                        }
                    });
                    builder.setNegativeButton(getResources().getString(R.string.albert_add_button_negative), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            resetRecognizer();
                            dialogInterface.dismiss();
                        }
                    });

                    addDialog = builder.create();
                    addDialog.show();

                    createAndStart();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                adapter = new AppAdapter(loadAppList(), Albert.this);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                appList.setAdapter(adapter);
                super.onPostExecute(aVoid);
            }

        }.execute();

    }

    @Override
    protected void onStop() {
        super.onStop();

        resetRecognizer();
    }

    private void changeDialogButtonsState(int id, boolean state){
       int color = getResources().getColor(R.color.colorAccent);
        if (!state) color = Color.LTGRAY;

        switch (id){
            case 0:
                addDialog.getButton(DialogInterface.BUTTON_POSITIVE).setClickable(state);
                addDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
                break;

            case 1:
                addDialog.getButton(DialogInterface.BUTTON_POSITIVE).setClickable(state);
                addDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color);
                break;
        }
    }

    private void saveVariants(){
        if (!database.isOpen()) database = new FridayDatabase(context).getWritableDatabase();
        ContentValues envelope = new ContentValues();
        String mail = "";

        for (String i: variantItems){
            mail += i + ";";
        }

        envelope.put(DictionarySchema.AppsTable.APP_VARIANTS, mail);
        envelope.put(DictionarySchema.AppsTable.PACKAGE, packageName);

        if (!haveDbRecords){
            database.insert(
                    DictionarySchema.AppsTable.NAME,
                    null,
                    envelope
            );
            haveDbRecords = true;
            setStatus(STATUS_SAVED, 2);
        } else {
            database.update(
                    DictionarySchema.AppsTable.NAME,
                    envelope,
                    DictionarySchema.AppsTable.PACKAGE + "=?",
                    new String[] {packageName}
            );
        }

        database.close();
    }

    private void createAndStart(){
        if (ActivityCompat.checkSelfPermission(
                this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{RECORD_AUDIO}, 1);
            }
        }
        resetRecognizer();
        recognizer = Recognizer.create(
                Recognizer.Language.RUSSIAN,
                Recognizer.Model.NOTES,
                Albert.this
        );
        recognizer.start();
    }

    private void resetRecognizer(){
        if (recognizer != null){
            recognizer.cancel();
            recognizer = null;
        }
    }

    private void animateToolbar(float y, int duration){
        toolbar.animate()
                .y(y)
                .setDuration(duration)
                .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        appList.setY(toolbar.getY() - appList.getHeight());
                        informationLayout.setY(toolbar.getY() + toolbar.getHeight() + heightToToolbar + (informationLayout.getY() - startInfoLayoutY) / 3);
                        //toolbar.setBackground(toolbarBackgrounds[(int) ((toolbar.getY() + toolbar.getHeight()) / ((maxY - statusBarHeight) / 11)) - 1]);

                    }
                })
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        appList.setY(toolbar.getY() - appList.getHeight());
                        informationLayout.setY(toolbar.getY() + toolbar.getHeight() + heightToToolbar);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                })
                .start();
    }

    public void setChosenApp(AppItem chosenAppInfo){
        if (appIcon.getVisibility() != View.VISIBLE) appIcon.setVisibility(View.VISIBLE);
        appIcon.setImageDrawable(chosenAppInfo.getIcon());
        appName.setText(chosenAppInfo.getApp());

        animateToolbar(0, 350);

        packageName = chosenAppInfo.getPackageName();
        loadAppInfo(chosenAppInfo);
    }

    private void loadAppInfo(final AppItem item){
        haveDbRecords = false;
        database = new FridayDatabase(context).getReadableDatabase();
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

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPreExecute() {
                setStatus(STATUS_LOADING, 1);
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                while (cursor.moveToNext()){
                    if (cursor.getString(cursor.getColumnIndex(DictionarySchema.AppsTable.PACKAGE)).equals(item.getPackageName())){
                        haveDbRecords = true;
                        break;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (!haveDbRecords){
                    connectAdapterToRecycler(null);
                    setStatus(STATUS_NOT_FOUND, 3);
                } else {
                    connectAdapterToRecycler(cursor.getString(cursor.getColumnIndex(DictionarySchema.AppsTable.APP_VARIANTS)));
                    setStatus(STATUS_LOADED, 2);
                }

                cursor.close();
                database.close();
                super.onPostExecute(aVoid);
            }

        }.execute();

    }

    private void connectAdapterToRecycler(String variants){
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        appVariants.setLayoutManager(layoutManager);
        appVariants.addItemDecoration(new ItemDecoration(context.getResources().getDrawable(R.drawable.item_line)));

        if (variants != null) variantItems = new ArrayList<>(Arrays.asList(variants.split(";")));
        else variantItems = new ArrayList<>();

        variantAdapter = new AppVariantAdapter(variantItems);
        appVariants.setAdapter(variantAdapter);
    }

    private void setStatus(String status, int colorCode){
        infoTitle.setText(STATUS + " " + status);

        switch (colorCode){
            case 0:
                infoTitle.setTextColor(context.getResources().getColor(R.color.black));
                break;
            case 1:
                infoTitle.setTextColor(context.getResources().getColor(R.color.orange));
                break;
            case 2:
                infoTitle.setTextColor(context.getResources().getColor(R.color.green));
                break;
            case 3:
                infoTitle.setTextColor(context.getResources().getColor(R.color.red));
                break;
        }
    }

    private int getStatusBarHeight(){
        int height = 0;
        int statusBarId = getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (statusBarId > 0) height = getResources().getDimensionPixelSize(statusBarId);

        return height;
    }

    private List<AppItem> loadAppList(){
        List<AppItem> out = new ArrayList<>();

        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo info: packages){
            if (!info.packageName.contains("com.android.") && !info.packageName.contains("com.qualcomm")){
                out.add(new AppItem(packageManager.getApplicationIcon(info), String.valueOf(info.loadLabel(packageManager)), info.packageName));
            }
        }

        Collections.sort(out);

        packages = null;

        return out;
    }

    private void loadToolbarBackgrounds(){
        toolbarBackgrounds = new Drawable[11];
        toolbarBackgrounds[10] = getResources().getDrawable(R.drawable.toolbar_upper_0);
        toolbarBackgrounds[9] = getResources().getDrawable(R.drawable.toolbar_upper_1);
        toolbarBackgrounds[8] = getResources().getDrawable(R.drawable.toolbar_upper_2);
        toolbarBackgrounds[7] = getResources().getDrawable(R.drawable.toolbar_upper_3);
        toolbarBackgrounds[6] = getResources().getDrawable(R.drawable.toolbar_upper_4);
        toolbarBackgrounds[5] = getResources().getDrawable(R.drawable.toolbar_upper_5);
        toolbarBackgrounds[4] = getResources().getDrawable(R.drawable.toolbar_upper_6);
        toolbarBackgrounds[3] = getResources().getDrawable(R.drawable.toolbar_upper_7);
        toolbarBackgrounds[2] = getResources().getDrawable(R.drawable.toolbar_upper_8);
        toolbarBackgrounds[1] = getResources().getDrawable(R.drawable.toolbar_upper_9);
        toolbarBackgrounds[0] = getResources().getDrawable(R.drawable.toolbar_upper_10);
    }

    @Override
    public void onBackPressed() {
        if (toolbar.getY() + toolbar.getHeight() == maxY - statusBarHeight) animateToolbar(0, 350);
        else if (addDialog != null && addDialog.isShowing()){
            addDialog.dismiss();
            resetRecognizer();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.albert_close_title));
            builder.setMessage(getResources().getString(R.string.albert_close_message));
            builder.setPositiveButton(getResources().getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    finish();
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public void onRecordingBegin(Recognizer recognizer) {
        addDialog.setTitle(getResources().getString(R.string.albert_add_message));
        changeDialogButtonsState(1, false);
    }

    @Override
    public void onSpeechDetected(Recognizer recognizer) {

    }

    @Override
    public void onSpeechEnds(Recognizer recognizer) {

    }

    @Override
    public void onRecordingDone(Recognizer recognizer) {

    }

    @Override
    public void onSoundDataRecorded(Recognizer recognizer, byte[] bytes) {

    }

    @Override
    public void onPowerUpdated(Recognizer recognizer, float v) {

    }

    @Override
    public void onPartialResults(Recognizer recognizer, Recognition recognition, boolean b) {
        result = recognition.getBestResultText();
        addDialog.setMessage(MESSAGE + " " + result);
    }

    @Override
    public void onRecognitionDone(Recognizer recognizer, Recognition recognition) {
        addDialog.setMessage(result);
        changeDialogButtonsState(0, true);

        resetRecognizer();
    }

    @Override
    public void onError(Recognizer recognizer, Error error) {

    }
}

class AppAdapter extends RecyclerView.Adapter<AppViewHolder>{
    private List<AppItem> items = new ArrayList<>();
    private Albert albert;

    AppAdapter(List<AppItem> items, Albert albert) {
        this.albert = albert;
        this.items = items;
    }

    private AppItem getItem(int position){
        return items.get(position);
    }

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.albert_app_item, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AppViewHolder holder, final int position) {
        AppItem appItem = items.get(position);

        holder.icon.setImageDrawable(appItem.getIcon());
        holder.appName.setText(appItem.getApp());
        holder.packageName.setText(appItem.getPackageName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                albert.setChosenApp(getItem(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

class AppViewHolder extends RecyclerView.ViewHolder{
    ImageView icon;
    TextView appName;
    TextView packageName;

    AppViewHolder(View itemView) {
        super(itemView);

        icon = itemView.findViewById(R.id.app_list_icon);
        appName = itemView.findViewById(R.id.app_list_name);
        packageName = itemView.findViewById(R.id.app_list_package);
    }
}

class AppVariantAdapter extends RecyclerView.Adapter<AppVariantsViewHolder>{
    private List<String> items = new ArrayList<>();

    AppVariantAdapter(List<String> items) {
        this.items = items;
    }

    @Override
    public AppVariantsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.default_recycle_item1, parent, false);
        return new AppVariantsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AppVariantsViewHolder holder, int position) {
        holder.variant.setText(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

class AppVariantsViewHolder extends RecyclerView.ViewHolder{
    TextView variant;

    AppVariantsViewHolder(final View itemView) {
        super(itemView);

        variant = itemView.findViewById(R.id.text_view);
    }
}

class AppItem implements Comparable<AppItem>{
    private Drawable icon;
    private String app;
    private String packageName;

    AppItem(Drawable icon, String app, String packageName){
        this.icon = icon;
        this.app = app;
        this.packageName = packageName;
    }

    Drawable getIcon() {
        return icon;
    }

    public String getApp() {
        return app;
    }

    String getPackageName() {
        return packageName;
    }

    public int compareTo(@NonNull AppItem another) {
        if (this.app != null)
            return this.app.toLowerCase().compareTo(another.getApp().toLowerCase());
        else
            throw new IllegalArgumentException();
    }
}
