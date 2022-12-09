package com.knziha.filepicker.widget;

import android.content.Context;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by KnIfER on 2017/11/4.
 */

public class TextViewmy extends AppCompatTextView {
	public boolean sc;
	public ClickableSpan span;
	public OnLongClickListener longClick;
	
	public TextViewmy(Context c) {
        super(c);
        init();
    }
    public TextViewmy(Context c, AttributeSet attrs) {
        super(c, attrs);
        init();
    }
    public TextViewmy(Context c, AttributeSet attrs, int defStyle) {
        super(c, attrs, defStyle);
        init();
    }
    @Override
    public boolean isFocused() {
        return true;
    }
    
    @Override
    protected void onScrollChanged(int horiz, int vert, int oldHoriz, int oldVert){
    	super.onScrollChanged(horiz, vert, oldHoriz, oldVert);
    	if(oldVert!=vert)sc=true;
    }
    
    private void init(){}

	
	
}
