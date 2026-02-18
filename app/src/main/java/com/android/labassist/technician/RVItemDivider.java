package com.android.labassist.technician;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.labassist.R;

public class RVItemDivider extends RecyclerView.ItemDecoration {
    private final Drawable divider;

    public RVItemDivider(Context context) {
        divider = ContextCompat.getDrawable(context, R.drawable.grey_item_divider);
    }

    @Override
    public void onDrawOver(@NonNull Canvas canvas,
                           @NonNull RecyclerView parent,
                           @NonNull RecyclerView.State state) {

        if (divider == null) return;

        int childCount = parent.getChildCount();
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params =
                    (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + divider.getIntrinsicHeight();

            divider.setBounds(left, top, right, bottom);
            divider.draw(canvas);
        }
    }
}
