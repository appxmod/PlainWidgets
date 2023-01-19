package com.knziha.filepicker.model;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.knziha.filepicker.utils.CMNF;

import java.io.FileInputStream;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class VideoCoverFetcher implements DataFetcher<Bitmap> {
	private final VideoCover model;
	private FFmpegMediaMetadataRetriever mRetriever;

	public VideoCoverFetcher(VideoCover model) {
		this.model = model;
	}

	public VideoCover getModel() {
		return model;
	}

	@Override
	public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Bitmap> callback) {
//		mRetriever = new MediaMetadataRetriever();
//		try {
//			FileInputStream inputStream = new FileInputStream(model.path);
//			mRetriever.setDataSource(inputStream.getFD());
//			Bitmap picture = mRetriever.getFrameAtTime();
//			if (picture != null) {
//				callback.onDataReady(picture);
//			} else {
//				callback.onLoadFailed(new Exception("load audio cover fail"));
//			}
//		} catch (Exception e){
//			e.printStackTrace();
//		}

		try {
			if(mRetriever==null)
				mRetriever = new FFmpegMediaMetadataRetriever();

			mRetriever.setDataSource(model.path);
			Bitmap picture = mRetriever.getFrameAtTime();
			if (picture != null) {
				callback.onDataReady(picture);
			} else {
				callback.onLoadFailed(new Exception("load audio cover fail"));
			}
			mRetriever.release();
		} catch (Exception e){
			if(GlobalOptions.debug) CMNF.Log(e);
		}

	}

	@Override public void cleanup() {
	}

	@Override public void cancel() {
		// cannot cancel
	}

	@NonNull
	@Override
	public Class<Bitmap> getDataClass() {
		return Bitmap.class;
	}

	@NonNull
	@Override
	public DataSource getDataSource() {
		return DataSource.LOCAL;
	}
}