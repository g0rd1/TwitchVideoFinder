package com.igorgordeev.twitchsearchvideos.model;

import java.io.Serializable;

public class Video implements Serializable {

	public static final long NO_GROUP = 0;
	private static final long serialVersionUID = 108826263L;
	private final long _id;
	private final long groupId;
	private final String channelName;
	private final String gameName;
	private final String title;
	private final long views;
	private final String duration;
	private final String type;
	private final String createdAt;
	private final String thumbnailUrl;
	private final String Url;

	public Video(long _id, long groupId, String channelName, String gameName, String title, long views,
				 String duration, String type, String createdAt, String thumbnailUrl, String Url) {
		this._id = _id;
		this.groupId = groupId;
		this.channelName = channelName;
		this.gameName = gameName;
		this.title = title;
		this.views = views;
		this.duration = duration;
		this.type = type;
		this.createdAt = createdAt;
		this.thumbnailUrl = thumbnailUrl;
		this.Url = Url;
	}

	public long getId() {
		return _id;
	}

	public long getGroupId() {
		return groupId;
	}

	public String getChannelName() {
		return channelName;
	}

	public String getGameName() {
		return gameName;
	}

	public String getTitle() {
		return title;
	}

	public long getViews() {
		return views;
	}

	public String getDuration() {
		return duration;
	}

	public String getType() {
		return type;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public String getUrl() {
		return Url;
	}
}
