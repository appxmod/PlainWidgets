package com.knziha.filepicker.model;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.io.InputStream;

public class VideoCoverLoaderFactory implements ModelLoaderFactory<VideoCover, Bitmap> {

	@NonNull
	@Override
	public ModelLoader<VideoCover, Bitmap> build(@NonNull MultiModelLoaderFactory multiFactory) {
		return new VideoModelLoader();
	}

	@Override
	public void teardown() {

	}
}