package org.springframework.cloud.repositorymanagement;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcin Grzejszczak
 */
class RepositoryManagementBuilderTests {

	@Test
	void should_do_nothing_by_default() {
		BDDAssertions.then(new RepositoryManagementBuilder() {

		}.build(OptionsBuilder.builder().build())).isNull();
	}
}