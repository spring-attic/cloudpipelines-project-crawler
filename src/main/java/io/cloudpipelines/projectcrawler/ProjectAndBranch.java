package io.cloudpipelines.projectcrawler;

import java.util.Objects;

/**
 * @author Marcin Grzejszczak
 * @since 1.0.0
 */
public class ProjectAndBranch {
	public final String project, projectName, branch;

	public ProjectAndBranch(String project, String projectName, String branch) {
		this.project = project;
		this.projectName = projectName;
		this.branch = branch;
	}

	public ProjectAndBranch(String project) {
		this(project, project, "master");
	}

	public ProjectAndBranch(String project, String newProjectName) {
		this(project, newProjectName, "master");
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ProjectAndBranch that = (ProjectAndBranch) o;
		return Objects.equals(project, that.project) && Objects
				.equals(projectName, that.projectName) && Objects
				.equals(branch, that.branch);
	}

	@Override public int hashCode() {
		return Objects.hash(project, projectName, branch);
	}
}