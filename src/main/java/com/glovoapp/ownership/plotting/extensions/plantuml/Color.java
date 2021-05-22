package com.glovoapp.ownership.plotting.extensions.plantuml;

import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode
final class Color {

    private final int red;
    private final int green;
    private final int blue;

    Color(final int red, final int green, final int blue) {
        this.red = checkRGBPart(red, "red");
        this.green = checkRGBPart(green, "green");
        this.blue = checkRGBPart(blue, "blue");
    }

    /**
     * Transforms CMYK representation to RGB representation.
     */
    Color(final int cyan, final int magenta, final int yellow, final int black) {
        this(
            (int) (255 * (1.0 - cyan / 100.0) * (1.0 - black / 100.0)),
            (int) (255 * (1.0 - magenta / 100.0) * (1.0 - black / 100.0)),
            (int) (255 * (1.0 - yellow / 100.0) * (1.0 - black / 100.0))
        );
    }

    public static Color black() {
        return new Color(0, 0, 0);
    }

    public static Color lightGrey() {
        return new Color(211, 211, 211);
    }

    public static Color darkGrey() {
        return new Color(169, 169, 169);
    }

    /**
     * @return random color that should look good on both light and dark themes
     */
    public static Color randomReadableColor(final Random random) {
        final List<Integer> cmyList = asList(77, 0, random.nextInt(77));
        Collections.shuffle(cmyList);
        return new Color(cmyList.get(0), cmyList.get(1), cmyList.get(2), 14);
    }

    /**
     * @return a PlantUML friendly hex representation of this color, e.g. #0AF54C
     */
    final String toHexString() {
        return "#" + toHexStringNoHash();
    }

    /**
     * @return same as {@link #toHexString()} but without the leading # character
     */
    final String toHexStringNoHash() {
        return hexPart(red) + hexPart(green) + hexPart(blue);
    }

    private static int checkRGBPart(final int value, final String name) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException(name + " color part must be between 0 and 255, received: " + value);
        } else {
            return value;
        }
    }

    private static String hexPart(final int value) {
        final String asHex = Integer.toHexString(value)
                                    .toUpperCase();
        if (asHex.length() > 2) {
            throw new IllegalArgumentException("cannot write " + value + " as hex part; result too long: " + asHex);
        }
        return asHex.length() != 2 ? '0' + asHex : asHex;
    }

}
