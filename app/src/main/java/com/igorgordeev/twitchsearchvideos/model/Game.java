package com.igorgordeev.twitchsearchvideos.model;

public class Game {

	private final String gameName;
	private final long gameId;

	public Game(String gameName, long gameId) {
		this.gameName = gameName;
		this.gameId = gameId;
	}

	public String getGameName() {
		return gameName;
	}

	public long getGameId() {
		return gameId;
	}
}
