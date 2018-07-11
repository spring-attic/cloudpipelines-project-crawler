package org.springframework.cloud.repositorymanagement;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public class OptionsBuilder {
	private String username;
	private String password;
	private String token;
	private String rootUrl;
	private Repositories repository = Repositories.OTHER;
	private final Set<ProjectAndBranch> projects = new HashSet<>();
	private final Set<ProjectAndBranch> renamedProjects = new HashSet<>();
	private final Set<String> excludedProjectsRegex = new HashSet<>();

	public static OptionsBuilder builder() {
		return new OptionsBuilder();
	}

	public OptionsBuilder username(String username) {
		this.username = username;
		return this;
	}

	public OptionsBuilder password(String password) {
		this.password = password;
		return this;
	}

	public OptionsBuilder token(String token) {
		this.token = token;
		return this;
	}

	public OptionsBuilder repository(Repositories repository) {
		this.repository = repository;
		return this;
	}

	public OptionsBuilder repository(String repository) {
		this.repository = Repositories.valueOf(repository.toUpperCase());
		return this;
	}

	public OptionsBuilder project(String projectName, String branch) {
		this.projects.add(new ProjectAndBranch(projectName, projectName, branch));
		return this;
	}

	public OptionsBuilder project(String project, String projectName, String branch) {
		this.projects.add(new ProjectAndBranch(project, projectName, branch));
		return this;
	}

	public OptionsBuilder projectName(String projectName, String newProjectName) {
		this.renamedProjects.add(new ProjectAndBranch(projectName, newProjectName));
		return this;
	}

	public OptionsBuilder project(String projectName) {
		this.projects.add(new ProjectAndBranch(projectName));
		return this;
	}

	public OptionsBuilder exclude(String regex) {
		this.excludedProjectsRegex.add(regex);
		return this;
	}

	public OptionsBuilder rootUrl(String rootUrl) {
		this.rootUrl = rootUrl;
		return this;
	}

	public Options build() {
		return new Options(this.username, this.password,
				this.token, this.rootUrl, this.repository, this.projects,
				this.renamedProjects,
				this.excludedProjectsRegex);
	}
}