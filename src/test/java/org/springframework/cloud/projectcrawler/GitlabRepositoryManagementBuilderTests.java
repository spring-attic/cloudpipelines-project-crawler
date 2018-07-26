package org.springframework.cloud.projectcrawler;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabProject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */
class GitlabRepositoryManagementBuilderTests {

	GitlabRepositoryManagementBuilder sut = new GitlabRepositoryManagementBuilder();

	@Test
	void should_return_false_when_url_is_empty() {
		then(sut.build(OptionsBuilder.builder()
				.username("foo").password("bar")
				.build())).isNull();
	}

	@Test
	void should_return_false_when_url_does_not_contain_gitlab() {
		then(sut.build(OptionsBuilder.builder()
				.username("foo").password("bar")
				.rootUrl("foo").build())).isNull();
	}

	@Test
	void should_return_true_when_repositories_is_gitlab_as_enum() {
		then(builder().build(OptionsBuilder.builder().rootUrl("http://foo.com")
				.username("foo").password("bar")
				.repository(Repositories.GITLAB).build())).isNotNull();
	}

	@Test
	void should_return_true_when_repositories_is_gitlab() {
		then(builder().build(OptionsBuilder.builder().rootUrl("foo")
				.token("foo")
				.repository("gitlab").build())).isNotNull();
	}

	@Test
	void should_return_true_when_url_contains_gitlab() {
		then(builder().build(OptionsBuilder.builder()
				.token("foo")
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
				return new GitlabRepositoryManagement(options) {

					@Override GitlabAPI connect(Options options) {
						return Mockito.mock(GitlabAPI.class);
					}

					@Override List<GitlabProject> groupRepos(String org)
							throws IOException {
						return Collections.singletonList(new GitlabProject());
					}

					@Override byte[] getDescriptor(String org, String repo, String branch,
							String filePath) throws IOException {
						return "".getBytes();
					}
				};
			}
		};
	}

}