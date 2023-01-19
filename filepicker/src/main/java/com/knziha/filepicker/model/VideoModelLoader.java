package com.knziha.filepicker.model;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;

import java.io.InputStream;

public class VideoModelLoader implements ModelLoader<VideoCover, Bitmap> {
	@Nullable
	@Override
	public LoadData<Bitmap> buildLoadData(@NonNull VideoCover audioCover, int width, int height, @NonNull Options options) {
		return new LoadData<>(new VideoCoverSignature(audioCover.path), new VideoCoverFetcher(audioCover));
	}

	@Override
	public boolean handles(@NonNull VideoCover audioCover) {
		return true;
	}
}