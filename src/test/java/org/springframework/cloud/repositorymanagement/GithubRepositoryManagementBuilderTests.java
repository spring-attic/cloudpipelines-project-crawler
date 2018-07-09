package org.springframework.cloud.repositorymanagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

import com.jcabi.github.Repo;
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.github.mock.MkStorage;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.nio.file.Files.createTempDirectory;
import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */

class GithubRepositoryManagementBuilderTests {

	GithubRepositoryManagementBuilder sut = new GithubRepositoryManagementBuilder();

	@Test
	void should_return_false_when_url_is_empty() {
		then(sut.build(OptionsBuilder.builder().build())).isNull();
	}

	@Test
	void should_return_false_when_url_does_not_contain_github() {
		then(sut.build(OptionsBuilder.builder().rootUrl("foo").build())).isNull();
	}

	@Test
	void should_return_true_when_url_contains_github() {
		then(new GithubRepositoryManagementBuilder() {
			@Override RepositoryManagement createNewRepoManagement(Options options) {
				return new RepositoryManagement() {
					@Override public List<String> repositories(String org) {
						return null;
					}

					@Override public String fileContent(String org, String repo,
							String branch, String filePath) {
						return null;
					}
				};
			}
		}.build(OptionsBuilder.builder().rootUrl("http://github").build())).isNotNull();
	}

}