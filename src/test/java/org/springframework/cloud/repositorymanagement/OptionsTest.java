package org.springframework.cloud.repositorymanagement;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcin Grzejszczak
 */
class OptionsTest {

	@Test
	void should_return_true_when_project_ignored() {
		Options build = OptionsBuilder.builder()
				.exclude("^.*github\\.io$").build();

		BDDAssertions.then(build.isIgnored("foo.github.io")).isTrue();
		BDDAssertions.then(build.isIgnored("foo")).isFalse();
	}

	@Test void should_override_the_project_name() {
		Options build = OptionsBuilder.builder()
				.projectName("foo", "bar")
				.build();

		BDDAssertions.then(build.projectName("foo"))
				.isEqualTo("bar");
	}
}