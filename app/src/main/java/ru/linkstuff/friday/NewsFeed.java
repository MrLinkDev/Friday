package ru.linkstuff.friday;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import ru.linkstuff.friday.PhraseService.PhraseService;

/**
 * Created by alexander on 10.09.17.
 */

public class NewsFeed extends Activity {
    private SharedPreferences preferences;

    private RelativeLayout chatLineLayout;
    private RelativeLayout chatLayout;
    private ScrollView chatWindow;
    private EditText chatLine;
    private ImageView micIcon;
    private ImageView enterButton;

    private WidthAnimator widthAnimator;
    private Animation.AnimationListener alphaAnimation;

    private Toolbar bottomToolbar;
    private Toolbar upperToolbar;

    private int statusBarHeight;
    private float toToolbarBottom;
    private float toToolbarTop;
    private float startY;
    private float startChatWindowY;
    private float startYofAnotherToolbar;
    private float heightToToolbar;
    private float maxY;
    private float percent;

    private float startChatLineWidth;

    private Drawable[] upperToolbarBackgrounds;
    private Drawable[] bottomToolbarBackgrounds;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_feed_layout);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("FIRST_START", true)){
            startActivity(new Intent(this, FirstStart.class));
            finish();
        } else {
            startService(new Intent(this, PhraseService.class));

            Point point = new Point();
            getWindowManager().getDefaultDisplay().getSize(point);
            maxY = point.y;

            statusBarHeight = getStatusBarHeight();
            loadToolbarBackgrounds();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        enterButton = findViewById(R.id.enter_button);
        micIcon = findViewById(R.id.mic_icon);
        chatLineLayout = findViewById(R.id.chat_line_layout);
        chatLine = findViewById(R.id.chat_line);
        chatLayout = findViewById(R.id.chat_layout);
        chatWindow = findViewById(R.id.chat_window);
        bottomToolbar = findViewById(R.id.friday_toolbar_bottom);
        upperToolbar = findViewById(R.id.friday_toolbar_upper);

        chatLayout.getLayoutParams().height = (int) maxY - statusBarHeight - upperToolbar.getLayoutParams().height;
        chatLayout.setY(maxY - statusBarHeight);

        bottomToolbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float y = motionEvent.getRawY();

                percent = view.getY() / ((maxY - view.getHeight() - statusBarHeight) / 100);

                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        startY = view.getY();
                        startYofAnotherToolbar = upperToolbar.getY();

                        toToolbarTop = y - startY;
                        toToolbarBottom = view.getY() + view.getHeight() - y + statusBarHeight;

                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (y -toToolbarTop >= 0 && view.getY() >= 0 && y + toToolbarBottom <= maxY && view.getY() + view.getHeight() <= maxY){
                            view.setY(y - toToolbarTop);
                        } else if (view.getY() > 0 && y - toToolbarTop <= 0){
                            view.setY(0);
                        } else if (view.getY() + view.getHeight() < maxY && y + toToolbarBottom >= maxY){
                            view.setY(maxY - view.getHeight() - statusBarHeight);
                        }

                        upperToolbar.setY(percent * ((float) upperToolbar.getHeight() / 100) - upperToolbar.getHeight());
                        chatLayout.setY(view.getY() + view.getHeight());

                        bottomToolbar.setBackground(bottomToolbarBackgrounds[(int) ((view.getY() + view.getHeight()) / ((maxY - statusBarHeight) / 11)) - 1]);

                        break;

                    case MotionEvent.ACTION_UP:
                        if (startY >= y && percent <= 80) animateToolbar(view, 0, 350);
                        else animateToolbar(view, maxY - view.getHeight() - statusBarHeight, 350);
                        break;
                }

                return false;
            }
        });

        micIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startChatLineWidth == 0) startChatLineWidth = chatLineLayout.getWidth();

                if (startChatLineWidth == chatLineLayout.getWidth()) {
                    widthAnimator = new WidthAnimator(chatLineLayout, chatLineLayout.getHeight(), 150);
                    alphaAnimation = new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            micIcon.animate().alpha(0f).setDuration(100).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    micIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_typing));
                                    micIcon.animate().alpha(1f).setDuration(100);
                                }
                            });

                            chatLine.animate().alpha(0f).setDuration(100);
                            enterButton.animate().alpha(0f).setDuration(100);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    };
                } else {
                    widthAnimator = new WidthAnimator(chatLineLayout, startChatLineWidth, 150);
                    alphaAnimation = new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            micIcon.animate().alpha(0f).setDuration(100).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    micIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_microphone));
                                    micIcon.animate().alpha(1f).setDuration(100);
                                }
                            });

                            chatLine.animate().alpha(1f).setDuration(100);
                            enterButton.animate().alpha(1f).setDuration(100);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    };
                    enterButton.setVisibility(View.VISIBLE);
                    chatLine.setVisibility(View.VISIBLE);
                }

                widthAnimator.setAnimationListener(alphaAnimation);
                chatLineLayout.startAnimation(widthAnimator);
            }
        });

    }

    private void animateToolbar(final View toolbar, float y, int duration){
        toolbar.animate()
                .y(y)
                .setDuration(duration)
                .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        percent = toolbar.getY() / ((maxY - toolbar.getHeight() - statusBarHeight) / 100);

                        upperToolbar.setY(percent * ((float) upperToolbar.getHeight() / 100) - upperToolbar.getHeight());
                        chatLayout.setY(toolbar.getY() + toolbar.getHeight());

                        //bottomToolbar.setBackground(bottomToolbarBackgrounds[(int) ((toolbar.getY() + toolbar.getHeight()) / ((maxY - statusBarHeight) / 11)) - 1]);

                    }
                });
    }

    private int getStatusBarHeight(){
        int height = 0;
        int statusBarId = getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (statusBarId > 0) height = getResources().getDimensionPixelSize(statusBarId);

        return height;
    }

    private void loadToolbarBackgrounds(){
        upperToolbarBackgrounds = new Drawable[11];
        bottomToolbarBackgrounds = new Drawable[11];

        upperToolbarBackgrounds[10] = getResources().getDrawable(R.drawable.toolbar_upper_0);
        upperToolbarBackgrounds[9] = getResources().getDrawable(R.drawable.toolbar_upper_1);
        upperToolbarBackgrounds[8] = getResources().getDrawable(R.drawable.toolbar_upper_2);
        upperToolbarBackgrounds[7] = getResources().getDrawable(R.drawable.toolbar_upper_3);
        upperToolbarBackgrounds[6] = getResources().getDrawable(R.drawable.toolbar_upper_4);
        upperToolbarBackgrounds[5] = getResources().getDrawable(R.drawable.toolbar_upper_5);
        upperToolbarBackgrounds[4] = getResources().getDrawable(R.drawable.toolbar_upper_6);
        upperToolbarBackgrounds[3] = getResources().getDrawable(R.drawable.toolbar_upper_7);
        upperToolbarBackgrounds[2] = getResources().getDrawable(R.drawable.toolbar_upper_8);
        upperToolbarBackgrounds[1] = getResources().getDrawable(R.drawable.toolbar_upper_9);
        upperToolbarBackgrounds[0] = getResources().getDrawable(R.drawable.toolbar_upper_10);

        bottomToolbarBackgrounds[0] = getResources().getDrawable(R.drawable.toolbar_bottom_0);
        bottomToolbarBackgrounds[1] = getResources().getDrawable(R.drawable.toolbar_bottom_1);
        bottomToolbarBackgrounds[2] = getResources().getDrawable(R.drawable.toolbar_bottom_2);
        bottomToolbarBackgrounds[3] = getResources().getDrawable(R.drawable.toolbar_bottom_3);
        bottomToolbarBackgrounds[4] = getResources().getDrawable(R.drawable.toolbar_bottom_4);
        bottomToolbarBackgrounds[5] = getResources().getDrawable(R.drawable.toolbar_bottom_5);
        bottomToolbarBackgrounds[6] = getResources().getDrawable(R.drawable.toolbar_bottom_6);
        bottomToolbarBackgrounds[7] = getResources().getDrawable(R.drawable.toolbar_bottom_7);
        bottomToolbarBackgrounds[8] = getResources().getDrawable(R.drawable.toolbar_bottom_8);
        bottomToolbarBackgrounds[9] = getResources().getDrawable(R.drawable.toolbar_bottom_9);
        bottomToolbarBackgrounds[10] = getResources().getDrawable(R.drawable.toolbar_bottom_10);
    }

    @Override
    public void onBackPressed() {
        if (bottomToolbar.getY() == 0) animateToolbar(bottomToolbar, maxY - bottomToolbar.getHeight() - statusBarHeight, 350);
        //super.onBackPressed();
    }
}

class WidthAnimator extends Animation{
    private View view;
    private float startWidth;
    private float endWidth;

    public WidthAnimator(final View view, float endWidth, int duration){
        this.view = view;
        this.endWidth = endWidth;
        startWidth = view.getWidth();
        setDuration(duration);

    }

    @Override
    public void setAnimationListener(AnimationListener listener) {
        super.setAnimationListener(listener);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        view.getLayoutParams().width = (int) ((endWidth - startWidth) * interpolatedTime + startWidth);
        view.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
