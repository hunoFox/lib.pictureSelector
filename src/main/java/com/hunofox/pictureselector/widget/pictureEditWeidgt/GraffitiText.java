package com.hunofox.pictureselector.widget.pictureEditWeidgt;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by huangziwei on 2017/3/16.
 */
public class GraffitiText extends GraffitiSelectableItem {

    private static final int GRAFFITI_PIXEL_UNIT = 1;
    private final static Paint sPaint = new Paint();
    private String mText;

    private int orX;
    private int orY;

    public GraffitiText(GraffitiView.Pen pen, String text, float size, GraffitiColor color, int textRotate, int rotateDegree, float x, float y, float px, float py) {
        super(pen, size, color, textRotate, rotateDegree, x, y, px, py);
        this.mText = text;
        resetBounds(getBounds());
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
        resetBounds(getBounds());
    }

    @Override
    public void resetBounds(Rect rect) {
        if (TextUtils.isEmpty(mText)) {
            return;
        }
        sPaint.setTextSize(getSize());
        sPaint.setStyle(Paint.Style.FILL);
        sPaint.getTextBounds(mText, 0, mText.length(), rect);
        rect.left -= 10 * GRAFFITI_PIXEL_UNIT;
        rect.top -= 10 * GRAFFITI_PIXEL_UNIT;
        rect.right += 10 * GRAFFITI_PIXEL_UNIT;
        rect.bottom += 10 * GRAFFITI_PIXEL_UNIT;

        orX = rect.right - rect.left;
        orY = rect.bottom - rect.top;
        rect.left -= orX/2;
        rect.right -= orX/2;
        rect.top += orY/2;
        rect.bottom += orY/2;
    }

    @Override
    public void draw(Canvas canvas, GraffitiView graffitiView, Paint paint) {
        paint.setTextSize(getSize());
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText(mText, -orX/2, orY/2, paint);
    }

}


