package io.github.addoncommunity.galactifun.api.items.spacesuit;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
@SuppressWarnings("ClassCanBeRecord")
public final class SpaceSuitStat {

    public static final SpaceSuitStat HEAT_RESISTANCE = new SpaceSuitStat("&c抗热");
    public static final SpaceSuitStat COLD_RESISTANCE = new SpaceSuitStat("&b抗寒");
    public static final SpaceSuitStat RADIATION_RESISTANCE = new SpaceSuitStat("&4抗辐射");

    private final String name;

}
