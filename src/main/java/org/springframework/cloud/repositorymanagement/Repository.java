package org.springframework.cloud.repositorymanagement;

/**
 * @author Marcin Grzejszczak
 */
public class Repository {
	public String name, ssh_url, clone_url, requestedBranch;

	public Repository(String name, String ssh_url, String clone_url, String requestedBranch) {
		this.name = name;
		this.ssh_url = ssh_url;
		this.clone_url = clone_url;
		this.requestedBranch = requestedBranch;
	}
}
