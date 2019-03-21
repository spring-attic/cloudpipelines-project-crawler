package io.cloudpipelines.projectcrawler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
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
		then(sut.build(OptionsBuilder.builder()
				.token("foo")
				.rootUrl("foo").build())).isNull();
	}

	@Test
	void should_return_true_when_repositories_is_bitbucket_as_enum() {
		then(builder().build(OptionsBuilder.builder()
				.token("foo")
				.rootUrl("foo")
				.repository(Repositories.BITBUCKET).build())).isNotNull();
	}

	@Test
	void should_return_true_when_repositories_is_bitbucket() {
		then(builder().build(OptionsBuilder.builder()
				.username("foo").password("bar")
				.rootUrl("foo")
				.repository("bitbucket").build())).isNotNull();
	}

	@Test
	void should_return_true_when_url_contains_gitlab() {
		then(builder().build(OptionsBuilder.builder()
				.token("foo")
				.rootUrl("https://bitbucket").build())).isNotNull();
	}

	@Test
	void should_fetch_the_repos() {
		then(builder().build(OptionsBuilder.builder()
				.token("foo")
				.rootUrl("https://bitbucket").build())
				.repositories("scpipelines")).isNotNull();
	}

	@Test
	void should_fetch_the_file() {
		then(builder().build(OptionsBuilder.builder()
				.token("foo")
				.rootUrl("https://bitbucket").build())
				.fileContent("scpipelines",
						"github-webhook", "master", "sc-pipelines.yml")).isNotEmpty();
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
				return new BitbucketRepositoryManagement(options) {
					@Override Response callRepositories(String org, int page) throws IOException {
						File file = new File(BitbucketRepositoryManagementBuilderTests.class.getResource("/bitbucket/projects.json").getFile());
						String body = new String(Files.readAllBytes(file.toPath()));
						return new Response.Builder()
								.request(new Request.Builder()
										.url("http://www.foo.com/")
										.get()
										.build())
								.protocol(Protocol.HTTP_1_1)
								.code(200)
								.message(body)
								.body(ResponseBody.create(MediaType.get("application/json"), body))
								.build();
					}

					@Override String getDescriptor(String org, String repo, String branch,
							String filePath) {
						return "hello";
					}
				};
			}
		};
	}

}