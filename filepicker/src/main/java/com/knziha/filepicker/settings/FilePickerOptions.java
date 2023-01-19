package com.knziha.filepicker.settings;

public abstract class FilePickerOptions {
	public enum FilePickerOption{
		bShowBookmarks
		, bShowBottombar
		, bShowCreateFile
		, bPinSortDialog
		, bShowThumbnails
		, bFFMRThumbnails
		, bCropThumbnails
		, bListMode
		, bLRU
		, bAutoThumbsHeight
		, bRegexSch
		, bRoot
		, bSlideshowMode
		, nIconSize
		, nGridSize
		, nSortMode
	}
	
	public abstract int getOpt(FilePickerOption opt, boolean get, int val);
	
	public static FilePickerOptions opt = new FilePickerOptions() {
		@Override
		public int getOpt(FilePickerOption opt, boolean get, int val) {
			switch (opt) {
				default: return 0;
				case bShowBottombar:
				case bListMode:
				case bLRU:
					return 1;
				case nIconSize: return 0;
				case nGridSize: return 3;
				case nSortMode: return 7;
			}
		}
	};
	
	public static boolean getRoot;
}
