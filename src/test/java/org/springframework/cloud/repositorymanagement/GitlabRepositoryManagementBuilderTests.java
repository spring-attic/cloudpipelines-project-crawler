package org.springframework.cloud.repositorymanagement;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */
class GitlabRepositoryManagementBuilderTests {

	GitlabRepositoryManagementBuilder sut = new GitlabRepositoryManagementBuilder();

	@Test
	void should_return_false_when_url_is_empty() {
		then(sut.build(OptionsBuilder.builder().build())).isNull();
	}

	@Test
	void should_return_false_when_url_does_not_contain_gitlab() {
		then(sut.build(OptionsBuilder.builder().rootUrl("foo").build())).isNull();
	}

	@Test
	void should_return_true_when_repositories_is_gitlab_as_enum() {
		then(builder().build(OptionsBuilder.builder().rootUrl("foo")
				.repository(Repositories.GITLAB).build())).isNotNull();
	}

	@Test
	void should_return_true_when_repositories_is_gitlab() {
		then(builder().build(OptionsBuilder.builder().rootUrl("foo")
				.repository("gitlab").build())).isNotNull();
	}

	@Test
	void should_return_true_when_url_contains_gitlab() {
		then(builder().build(OptionsBuilder.builder()
				.rootUrl("http://gitlab").build())).isNotNull();
	}

	@Test
	@Disabled
	void should_call_the_real_thing_via_org() {
		then(new GitlabRepositoryManagementBuilder().build(
				OptionsBuilder.builder()
						.token("foo")
						.exclude(".*")
						.project("github-webook")
						.rootUrl("https://gitlab.com")
						.build())
				.repositories("sc-pipelines")).isNotNull();
	}

	@Test
	@Disabled
	void should_call_the_real_thing_to_get_a_file() {
		then(new GitlabRepositoryManagementBuilder().build(
				OptionsBuilder.builder()
						.token("foo")
						.rootUrl("https://gitlab.com")
						.build())
				.fileContent("sc-pipelines",
						"github-webhook", "master", "sc-pipelines.yml")).isNotEmpty();
	}

	private GitlabRepositoryManagementBuilder builder() {
		return new GitlabRepositoryManagementBuilder() {
			@Override RepositoryManagement createNewRepoManagement(Options options) {
				return new RepositoryManagement() {
					@Override public List<Repository> repositories(String org) {
						return null;
					}

					@Override public String fileContent(String org, String repo,
							String branch, String filePath) {
						return null;
					}
				};
			}
		};
	}

}