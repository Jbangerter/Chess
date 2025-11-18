package model.gameservicerecords;

import java.util.Collection;

public record GameListData(Collection<ShortenedGameData> games) {
}
