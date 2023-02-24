package io.github.stuff_stuffs.tbcexv3content.character.impl;

import io.github.stuff_stuffs.tbcexv3content.character.api.CharacterDataContainer;
import io.github.stuff_stuffs.tbcexv3content.character.api.job.CharacterJobData;
import io.github.stuff_stuffs.tbcexv3content.character.api.race.CharacterRacialData;

public class CharacterDataContainerImpl implements CharacterDataContainer {
    private final CharacterRacialData racialData;
    private final CharacterJobData jobData;

    public CharacterDataContainerImpl(final CharacterRacialData data, final CharacterJobData jobData) {
        racialData = data;
        this.jobData = jobData;
    }

    @Override
    public CharacterRacialData racialData() {
        return racialData;
    }

    @Override
    public CharacterJobData jobData() {
        return jobData;
    }
}
