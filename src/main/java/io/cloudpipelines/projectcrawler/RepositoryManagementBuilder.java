package io.cloudpipelines.projectcrawler;

/**
 * Builder for {@link RepositoryManagement}. Might be 
 * required to pass the credentials, tokens etc.
 * 
 * @author Marcin Grzejszczak
 * @since 1.0.0
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
