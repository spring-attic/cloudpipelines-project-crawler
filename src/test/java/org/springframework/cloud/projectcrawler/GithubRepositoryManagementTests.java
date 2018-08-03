package org.springframework.cloud.projectcrawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;

import com.jcabi.github.Repo;
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.github.mock.MkStorage;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.nio.file.Files.createTempDirectory;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */

class GithubRepositoryManagementTests {

	MkGithub github;
	Repo repo;
	File folder;
	File repoXml;
	GithubRepositoryManagement sut;

	@BeforeEach
	void setup() throws IOException  {
		this.folder = createTempDirectory("foo").toFile();
		this.repoXml = new File(this.folder, "foo.xml");
		this.github = new MkGithub(new MkStorage.InFile(this.repoXml), "jeff");
		this.repo = createSleuthRepo(this.github);
		this.sut = new GithubRepositoryManagement(this.github, OptionsBuilder.builder().build());
	}

	@AfterEach
	void cleanup() throws IOException {
		FileUtils.deleteDirectory(this.folder);
	}

	@Test
	void should_return_a_list_of_names_of_repos_for_an_org() {
		then(new GithubRepositoryManagement(this.github,
				OptionsBuilder.builder().exclude("^.*github\\.io$").build()) {
			@Override String orgRepos(String org) throws IOException {
				URL resource = GithubRepositoryManagementTests.class
						.getResource("/spring_cloud_repos.json");
				return new String(Files.readAllBytes(new File(resource.getFile()).toPath()));
			}
		}.repositories("jeff")).hasSize(29)
				.extracting("name").doesNotContain("spring-cloud.github.io");
	}

	@Test
	void should_return_a_list_of_names_of_repos_for_an_org_with_manual() {
		then(new GithubRepositoryManagement(this.github,
				OptionsBuilder.builder()
						.project("spring-cloud-gdpr")
						.projectName("spring-cloud-kubernetes-connector", "foo")
						.exclude("^.*github\\.io$").build()) {
			@Override String orgRepos(String org) throws IOException {
				URL resource = GithubRepositoryManagementTests.class
						.getResource("/spring_cloud_repos.json");
				return new String(Files.readAllBytes(new File(resource.getFile()).toPath()));
			}
		}.repositories("jeff")).hasSize(30)
				.extracting("name", "ssh_url", "clone_url")
				.contains(tuple("spring-cloud-gdpr",
						"git@github.com:jeff/spring-cloud-gdpr.git",
						"https://github.com/jeff/spring-cloud-gdpr.git"))
				.contains(tuple("foo",
						"git@github.com:spring-cloud/spring-cloud-kubernetes-connector.git",
						"https://github.com/spring-cloud/spring-cloud-kubernetes-connector.git"))
				.doesNotContain((tuple("spring-cloud-kubernetes-connector",
						"git@github.com:spring-cloud/spring-cloud-kubernetes-connector.git",
						"https://github.com/spring-cloud/spring-cloud-kubernetes-connector.git")));
	}

	@Test
	void should_return_a_list_of_only_manually_added_projects() {
		then(new GithubRepositoryManagement(this.github,
				OptionsBuilder.builder()
						.project("spring-cloud-gdpr")
						.exclude("^.*$").build()) {
			@Override String orgRepos(String org) throws IOException {
				URL resource = GithubRepositoryManagementTests.class
						.getResource("/spring_cloud_repos.json");
				return new String(Files.readAllBytes(new File(resource.getFile()).toPath()));
			}
		}.repositories("jeff")).hasSize(1)
				.extracting("name").contains("spring-cloud-gdpr");
	}

	@Test
	void should_return_empty_when_file_does_not_exist() {
		then(sut.fileContent("jeff",
				"spring-cloud-sleuth", "master", "sc-pipelines")).isEmpty();
	}

	@Test
	void should_return_file_contents_when_file_exists() throws IOException {
		File file = new File(this.folder, "sc-pipelines.yml");
		file.createNewFile();
		Files.write(file.toPath(), "hello: world".getBytes());

		then(new GithubRepositoryManagement(this.github, OptionsBuilder.builder().build()) {
			@Override InputStream getFileContent(String org, String repo, String branch,
					String filePath) throws IOException {
				return new FileInputStream(file);
			}

			@Override boolean descriptorExists(String org, String repo, String branch,
					String filePath) throws IOException {
				return true;
			}
		}.fileContent("jeff",
				"spring-cloud-sleuth", "master", "sc-pipelines.yml")).isEqualTo("hello: world");
	}

	private Repo createSleuthRepo(MkGithub github) throws IOException {
		return github.repos().create(new Repos.RepoCreate("spring-cloud-sleuth", false));
	}
}