package ru.linkstuff.friday.HelperClasses;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * Created by alexander on 24.09.17.
 */

public class ItemDecoration extends RecyclerView.ItemDecoration{
    private Drawable divider;

    public ItemDecoration(Drawable line){
        divider = line;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        int itemsCount = parent.getChildCount();

        for (int i = 0; i < itemsCount; ++i){
            View view = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)view.getLayoutParams();

            int top = view.getBottom() + params.bottomMargin;
            int bottom = top + divider.getIntrinsicHeight();

            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
    }
}
