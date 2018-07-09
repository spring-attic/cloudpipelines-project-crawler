package org.springframework.cloud.repositorymanagement;

/**
 * Builder for {@link RepositoryManagement}. Might be 
 * required to pass the credentials, tokens etc.
 * 
 * @author Marcin Grzejszczak
 * @since 0.0.1
 */
public interface RepositoryManagementBuilder {
	/**
	 * @param options - repository options
	 * @return {@code null} if {@link RepositoryManagement} can't be built for the given options
	 */
	default RepositoryManagement build(Options options) {
		return null;
	}
}
