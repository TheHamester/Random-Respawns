package com.rosymaple.randomrespawns;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> EnableRandomRespawns;
    public static final ForgeConfigSpec.ConfigValue<Integer> RandomRespawnRadius;

    public static final ForgeConfigSpec.ConfigValue<Boolean> DeathPositionIsOrigin;

    public static final ForgeConfigSpec.ConfigValue<Boolean> AvoidOceans;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SetTimeToZeroOnDeath;
    public static final ForgeConfigSpec.ConfigValue<Boolean> AvoidHazardsOnRespawn;

    static {
        BUILDER.push("Random Respawns Config");

        EnableRandomRespawns = BUILDER.comment("Enables random respawns.")
                .translation("randomrespawns.configgui.enable_random_respawns")
                .define("Enables Random Respawns", true);

        RandomRespawnRadius = BUILDER.comment("Radius within which respawn occurs.")
                .translation("randomrespawns.configgui.random_respawn_radius")
                .defineInRange("Random Respawn Radius", 5000, 1, Integer.MAX_VALUE);

        DeathPositionIsOrigin = BUILDER.comment("When true, respawn will occur with respect to your death position, rather than the world origin.")
                .translation("randomrespawns.configgui.death_position_is_origin")
                .define("Death Position is Origin", false);

        AvoidOceans = BUILDER.comment("Will avoid ocean biomes when respawning")
                .translation("randomrespawns.configgui.avoid_oceans")
                .define("Avoid Oceans", true);

        SetTimeToZeroOnDeath = BUILDER.comment("Sets time to 0 on death.")
                .translation("randomrespawns.configgui.set_time_to_zero_on_death")
                .define("Set Time To Zero On Death", false);

        AvoidHazardsOnRespawn = BUILDER.comment("Avoids hazards like Lava or Powder Snow on respawn.")
                .translation("randomrespawns.configgui.avoid_hazards_on_respawn")
                .define("Avoid Hazards on Respawn", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
