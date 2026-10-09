package com.aurorabakery.catalog;

import com.aurorabakery.configuration.DevelopmentSeedConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DevelopmentSeedConfigurationTest {
    private final ApplicationContextRunner context = new ApplicationContextRunner()
        .withUserConfiguration(DevelopmentSeedConfiguration.class);

    @Test void disabledByDefault() {
        context.run(ctx -> assertThat(ctx).doesNotHaveBean(ApplicationRunner.class));
    }

    @Test void legacyFlagCannotSilentlyInsertCatalogData() {
        context.withPropertyValues("app.seed.enabled=true").run(ctx ->
            assertThatThrownBy(() -> ctx.getBean(ApplicationRunner.class)
                .run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("reconciliation fixtures are not implemented"));
    }
}
