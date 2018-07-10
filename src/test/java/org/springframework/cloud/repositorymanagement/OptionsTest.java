package org.springframework.cloud.repositorymanagement;

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
}