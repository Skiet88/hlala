package com.hlala.screening;

import com.hlala.domain.ChecklistItemResult;
import com.hlala.domain.Enums;
import com.hlala.domain.GeoPoint;
import com.hlala.domain.Room;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultRoomCheckScreeningTest {

    private final DefaultRoomCheckScreening screening = new DefaultRoomCheckScreening();
    private final GeoPoint pin = new GeoPoint(-34.0383, 18.6778); // Khayelitsha area

    private Room room() {
        return new Room(UUID.randomUUID(), UUID.randomUUID(), "Khayelitsha Site C", "Near the taxi rank", pin,
                Enums.RoomType.BACKYARD_ROOM, Set.of(), Enums.OccupancyMode.SOLE, Instant.now());
    }

    private List<ChecklistItemResult> fullChecklist() {
        return DefaultRoomCheckScreening.REQUIRED_ITEMS.stream()
                .map(i -> new ChecklistItemResult(i, true, null)).toList();
    }

    private List<URI> photos(int n) {
        return IntStream.range(0, n).mapToObj(i -> URI.create("photo://" + i)).toList();
    }

    @Test
    void passesWhenOnSiteWithAllPhotosAndAnswers() {
        List<ChecklistItemResult> lines = screening.screen(room(), fullChecklist(), photos(6),
                new GeoPoint(-34.0384, 18.6779));
        assertThat(lines).allMatch(ChecklistItemResult::isPassed);
    }

    @Test
    void failsWhenCheckerWasNotAtTheRoom() {
        GeoPoint langa = new GeoPoint(-33.9447, 18.5307);
        List<ChecklistItemResult> lines = screening.screen(room(), fullChecklist(), photos(6), langa);
        assertThat(lines).anyMatch(l -> l.getItem().equals("Location match") && !l.isPassed());
    }

    @Test
    void failsWithTooFewPhotosOrMissingAnswers() {
        List<ChecklistItemResult> lines = screening.screen(room(), List.of(new ChecklistItemResult("Room exists", true, null)),
                photos(3), pin);
        assertThat(lines).anyMatch(l -> l.getItem().equals("Required photos") && !l.isPassed());
        assertThat(lines).anyMatch(l -> l.getItem().equals("Checklist complete") && !l.isPassed());
    }
}
