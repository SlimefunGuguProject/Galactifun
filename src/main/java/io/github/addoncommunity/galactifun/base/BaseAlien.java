package io.github.addoncommunity.galactifun.base;

import lombok.experimental.UtilityClass;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;

import io.github.addoncommunity.galactifun.api.aliens.BossBarStyle;
import io.github.addoncommunity.galactifun.base.aliens.Firestorm;
import io.github.addoncommunity.galactifun.base.aliens.Leech;
import io.github.addoncommunity.galactifun.base.aliens.Martian;
import io.github.addoncommunity.galactifun.base.aliens.MutantCreeper;
import io.github.addoncommunity.galactifun.base.aliens.Skywhale;
import io.github.addoncommunity.galactifun.base.aliens.TitanAlien;
import io.github.addoncommunity.galactifun.base.aliens.TitanKing;
import io.github.addoncommunity.galactifun.core.managers.AlienManager;

@UtilityClass
public final class BaseAlien {

    public static final Firestorm FIRESTORM = new Firestorm("FIRESTORM", "火爆者", 40, 50);
    public static final MutantCreeper MUTANT_CREEPER = new MutantCreeper("MUTANT_CREEPER", "变异爬行者", 40, 40);
    public static final Martian MARTIAN = new Martian("MARTIAN", "&4火星土著人", 32, 50);
    public static final Leech LEECH = new Leech("LEECH", "&e水蛭", 10, 1);
    public static final Skywhale SKYWHALE = new Skywhale("SKYWHALE", "&f飞鲸", 100, 3);
    public static final TitanAlien TITAN = new TitanAlien("TITAN", "泰坦", 32, 40);
    public static final TitanKing TITAN_KING = new TitanKing("TITAN_KING", "泰坦王", 300, 1,
            new BossBarStyle(BarColor.BLUE, BarStyle.SOLID, BarFlag.CREATE_FOG, BarFlag.DARKEN_SKY), LEECH);

    public static void setup(AlienManager alienManager) {
        FIRESTORM.register(alienManager);
        MUTANT_CREEPER.register(alienManager);
        MARTIAN.register(alienManager);
        LEECH.register(alienManager);
        SKYWHALE.register(alienManager);
        TITAN_KING.register(alienManager);
        TITAN.register(alienManager);
    }

}
