package com.glovoapp.ownership.plotting.plantuml;

import static com.glovoapp.ownership.plotting.plantuml.Color.randomReadableColor;
import static com.glovoapp.ownership.plotting.plantuml.Utils.RANDOM;
import static com.glovoapp.ownership.plotting.plantuml.Utils.repeat;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
@Builder(access = PACKAGE, builderClassName = "ArrowBuilder")
final class Arrow implements Renderable {

    @Builder.Default
    private final Direction direction = Direction.LEFT_TO_RIGHT;
    @Builder.Default
    private final LineStyle lineStyle = LineStyle.PLAIN;
    @Builder.Default
    private final HeadStyle headStyle = HeadStyle.POINTY;
    @Builder.Default
    private final Color color = Color.black();
    @Builder.Default
    private final int length = 1;
    @Builder.Default
    private final Set<Attribute> attributes = emptySet();

    public static Arrow randomHiddenArrow() {
        return Arrow.builder()
                    .attributes(singleton(Attribute.HIDDEN))
                    .length(RANDOM.nextInt(5))
                    .build();
    }

    @SuppressWarnings("unused")
    static class ArrowBuilder {

        ArrowBuilder randomColor() {
            return color(randomReadableColor());
        }

        Arrow build() {
            final Direction direction = direction$set ? direction$value : $default$direction();
            final LineStyle lineStyle = lineStyle$set ? lineStyle$value : $default$lineStyle();
            final HeadStyle headStyle = headStyle$set ? headStyle$value : $default$headStyle();
            final Color color = color$set ? color$value : $default$color();
            final int length = length$set ? length$value : $default$length();
            final Set<Attribute> attributes = attributes$set ? attributes$value : $default$attributes();

            if (length < 0) {
                throw new IllegalArgumentException("length of an arrow must be greater than 0");
            }
            requireNonNull(attributes).forEach(Objects::requireNonNull);
            return new Arrow(
                requireNonNull(direction),
                requireNonNull(lineStyle),
                requireNonNull(headStyle),
                requireNonNull(color),
                length,
                attributes
            );
        }

    }

    @Override
    public final String render() {
        return (direction == Direction.RIGHT_TO_LEFT || direction == Direction.BOTH_WAYS ? headStyle.left : "")
            + lineStyle.character
            + '[' + concat(
            Stream.of(color.toHexString()),
            attributes.stream()
                      .map(Attribute::getValue))
            .collect(joining(",")) + ']'
            + repeat(length, lineStyle.character)
            + (direction == Direction.LEFT_TO_RIGHT || direction == Direction.BOTH_WAYS ? headStyle.right : "");
    }

    @Getter
    @RequiredArgsConstructor
    enum Attribute {

        BOLD("bold"),
        DASHED("dashed"),
        HIDDEN("hidden");

        private final String value;

    }

    @RequiredArgsConstructor
    enum LineStyle {

        PLAIN('-'),
        DOTTED('.'),
        DASHED('=');

        private final char character;

    }

    @RequiredArgsConstructor
    enum HeadStyle {

        POINTY("<", ">"),
        FULL("<|", "|>"),
        DIAMOND("*", "*");

        private final String left;
        private final String right;

    }

    enum Direction {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT,
        BOTH_WAYS
    }

}
