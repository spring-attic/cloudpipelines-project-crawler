package org.springframework.cloud.repositorymanagement;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */
class BitbucketRepositoryManagementBuilderTests {

	BitbucketRepositoryManagementBuilder sut = new BitbucketRepositoryManagementBuilder();

	@Test
	void should_return_false_when_url_is_empty() {
		then(sut.build(OptionsBuilder.builder().build())).isNull();
	}

	@Test
	void should_return_false_when_url_does_not_contain_bitbucket() {
		then(sut.build(OptionsBuilder.builder().rootUrl("foo").build())).isNull();
	}

	@Test
	void should_return_true_when_repositories_is_bitbucket_as_enum() {
		then(builder().build(OptionsBuilder.builder().rootUrl("foo")
				.repository(Repositories.BITBUCKET).build())).isNotNull();
	}

	@Test
	void should_return_true_when_repositories_is_bitbucket() {
		then(builder().build(OptionsBuilder.builder().rootUrl("foo")
				.repository("bitbucket").build())).isNotNull();
	}

	@Test
	void should_return_true_when_url_contains_gitlab() {
		then(builder().build(OptionsBuilder.builder()
				.rootUrl("http://bitbucket").build())).isNotNull();
	}

	@Test
	@Disabled
	void should_call_the_real_thing_via_org() {
		then(new BitbucketRepositoryManagementBuilder().build(
				OptionsBuilder.builder()
						.username("foo")
						.password("bar")
						.exclude(".*")
						.project("github-webhook")
						.rootUrl("https://api.bitbucket.org")
						.build())
				.repositories("scpipelines")).isNotNull();
	}

	@Test
	@Disabled
	void should_call_the_real_thing_to_get_a_file() {
		then(new BitbucketRepositoryManagementBuilder().build(
				OptionsBuilder.builder()
						.token("foo")
						.rootUrl("https://api.bitbucket.org")
						.build())
				.fileContent("scpipelines",
						"github-webhook", "master", "sc-pipelines.yml")).isNotEmpty();
	}

	private BitbucketRepositoryManagementBuilder builder() {
		return new BitbucketRepositoryManagementBuilder() {
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