package org.springframework.cloud.repositorymanagement;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcin Grzejszczak
 */
class OptionsBuilderTest {

	@Test void should_contain_project_names() {
		Options options = OptionsBuilder.builder()
				.project("foo", "foo", "foo")
				.project("bar", "bar", "bar")
				.build();

		BDDAssertions.then(options.projects)
				.extracting("projectName")
				.contains("foo", "bar");
	}
}