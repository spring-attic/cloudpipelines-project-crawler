package org.springframework.cloud.repositorymanagement;

/**
 * @author Marcin Grzejszczak
 */
public class TestRepositoryManagementBuilder implements RepositoryManagementBuilder {

	static boolean EXECUTED = false;

	@Override public RepositoryManagement build(Options options) {
		EXECUTED = true;
		return new RepositoryManagement() {
		};
	}
}
