package com.forum.fiend.osp;

import android.content.Context;
import android.graphics.Canvas;
import android.widget.TextView;

public class OutlineTextView extends TextView {

	public OutlineTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		
	}

	@Override
    public void draw(Canvas canvas) {
        for (int i = 0; i < 5; i++) {
            super.draw(canvas);
        }
    }
}
