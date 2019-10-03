package com.igorgordeev.twitchsearchvideos.model;

import java.io.Serializable;

public class Group implements Serializable {

	public static final boolean USERS_GROUPS_DELETABLE = true;
	private static final long serialVersionUID = 108826233L;
	private final long _id;
	private final String name;
	private final boolean deletable;

	public Group(long _id, String name, boolean deletable) {
		this._id = _id;
		this.name = name;
		this.deletable = deletable;
	}

	public long getId() {
		return _id;
	}

	public String getName() {
		return name;
	}

	public boolean isDeletable() {
		return deletable;
	}
}
