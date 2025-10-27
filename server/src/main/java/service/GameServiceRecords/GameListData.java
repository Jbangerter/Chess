package service.GameServiceRecords;

import java.util.Collection;

public record GameListData(Collection<ShortenedGameData> games) {
}
