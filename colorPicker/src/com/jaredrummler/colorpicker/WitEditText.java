package com.jaredrummler.colorpicker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class WitEditText extends EditText {
	public WitEditText(Context context) {
		super(context);
	}

	public WitEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WitEditText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public boolean onTextContextMenuItem(int id) {
		switch (id){
			case android.R.id.cut:
				if(mContextTextChangeListener!=null)
					mContextTextChangeListener.beforeContextTextChange();
			break;
			case android.R.id.paste:
				if(mContextTextChangeListener!=null)
					mContextTextChangeListener.beforeContextTextChange();
			break;
		}
		boolean consumed = super.onTextContextMenuItem(id);
		return consumed;
	}

	public interface ContextTextChangeListener{
		void beforeContextTextChange();
	}

	public ContextTextChangeListener mContextTextChangeListener;

	public void setContextTextChangeListener(ContextTextChangeListener _ContextTextChangeListener){
		mContextTextChangeListener=_ContextTextChangeListener;
	}
}
