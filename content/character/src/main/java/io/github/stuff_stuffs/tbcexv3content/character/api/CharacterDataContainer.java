package io.github.stuff_stuffs.tbcexv3content.character.api;

import io.github.stuff_stuffs.tbcexv3content.character.api.job.CharacterJobData;
import io.github.stuff_stuffs.tbcexv3content.character.api.race.CharacterRacialData;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface CharacterDataContainer {
    CharacterRacialData racialData();

    CharacterJobData jobData();
}
