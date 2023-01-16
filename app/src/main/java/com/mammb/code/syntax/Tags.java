package com.mammb.code.syntax;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;

public class Tags {

    private TreeMap<Integer, List<Tag>> map = new TreeMap<>();


    public void add(int line, Tag tag) {
        map.computeIfAbsent(line, k -> new ArrayList<>()).add(tag);

        if (tag instanceof Tag.Start) {
            for (Integer i = map.ceilingKey(line); i != null; i = map.higherKey(i)) {

                Predicate<Tag> predicate = (i == line)
                    ? t -> t.position() > tag.position()
                    : t -> true;

                Optional<Tag> pair = map.get(i).stream()
                    .filter(t -> t instanceof Tag.End)
                    .filter(t -> !t.isLinked())
                    .filter(t -> t.name().equals(tag.name()))
                    .filter(predicate)
                    .min(Comparator.comparing(Tag::position));

                if (pair.isPresent()) {
                    tag.link(pair.get());
                    break;
                }
            }
        }

        if (tag instanceof Tag.End) {
            for (Integer i = map.floorKey(line); i != null; i = map.lowerKey(i)) {

                Predicate<Tag> predicate = (i == line)
                    ? t -> t.position() < tag.position()
                    : t -> true;

                Optional<Tag> pair = map.get(i).stream()
                        .filter(t -> t instanceof Tag.Start)
                        .filter(t -> !t.isLinked())
                        .filter(t -> t.name().equals(tag.name()))
                        .filter(predicate)
                        .max(Comparator.comparing(Tag::position));

                if (pair.isPresent()) {
                    tag.link(pair.get());
                    break;
                }
            }
        }

    }


    public List<Tag> get(int line) {
        return map.get(line);
    }


    public void remove(int line) {
        List<Tag> tags = map.remove(line);
        if (tags != null) {
            tags.stream().filter(Tag::isLinked).forEach(Tag::unlink);
        }
    }

}
