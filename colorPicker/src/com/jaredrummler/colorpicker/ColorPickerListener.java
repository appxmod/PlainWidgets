/*
 * Copyright (C) 2017 Jared Rummler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jaredrummler.colorpicker;

/** Callback used for getting the selected color from a color picker dialog.*/
public interface ColorPickerListener {
	/** 选定时调用 */
  boolean onColorSelected(ColorPickerDialog dialog, int color, boolean doubleTap);

  /** 预览时调用 */
  void onPreviewSelectedColor(ColorPickerDialog dialog, int color);

  /** 取消才会被调用 */
  void onDialogDismissed(ColorPickerDialog dialog, int color);
}
