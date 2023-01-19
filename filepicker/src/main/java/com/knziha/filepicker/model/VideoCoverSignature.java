package com.knziha.filepicker.model;

import com.bumptech.glide.load.Key;

import java.io.File;
import java.security.MessageDigest;

public class VideoCoverSignature implements Key , PathAffordable{
	private final File file;
	private StringBuilder stringBuilder;

	public VideoCoverSignature(String path) {
		this.file = new File(path);
		stringBuilder = new StringBuilder();
	}

	@Override
	public void updateDiskCacheKey(MessageDigest messageDigest) {
		stringBuilder.append(file.lastModified()).append(file.getName());
		byte[] bs = stringBuilder.toString().getBytes();
		messageDigest.update(bs, 0, bs.length);
	}

	@Override
	public String AffordPath() {
		return file.getAbsolutePath();
	}
}