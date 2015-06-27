package com.github.s0nerik.betterknife.util
import android.app.Application
import org.junit.runners.model.InitializationError
import org.robolectric.annotation.Config
import pl.polidea.robospock.internal.RoboSputnik

class SampleSputnik extends RoboSputnik {
    public SampleSputnik(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    public Config getConfig(Class<?> clazz) {
        Config classConfig = clazz.getAnnotation(Config.class);
        Config.Implementation config = new Config.Implementation(18,
                "sample/src/main/AndroidManifest.xml",
                "",
                "sample/src/main/res",
                18,
                new Class[0],
                Application.class,
                new String[0]);
        if (classConfig != null) {
            config = new Config.Implementation(config, classConfig);
        }

        return config;
    }
}