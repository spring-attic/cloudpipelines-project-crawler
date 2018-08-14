package io.cloudpipelines.projectcrawler;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Marcin Grzejszczak
 */
class ProjectCrawlerTests {

	@BeforeEach
	void before() {
		TestRepositoryManagementBuilder.EXECUTED = false;
	}

	@AfterEach
	void after() {
		TestRepositoryManagementBuilder.EXECUTED = false;
	}

	@Test
	void should_call_the_test_repo_manager_for_repositories() {
		new ProjectCrawler(OptionsBuilder.builder()
				.repository(Repositories.OTHER)
				.build())
				.repositories("foo");

		BDDAssertions.then(TestRepositoryManagementBuilder.EXECUTED).isTrue();
	}

	@Test
	void should_call_the_test_repo_manager_for_path() {
		new ProjectCrawler(OptionsBuilder.builder()
				.repository(Repositories.OTHER)
				.build())
				.fileContent("org", "repo", "branch", "path");

		BDDAssertions.then(TestRepositoryManagementBuilder.EXECUTED).isTrue();
	}

}