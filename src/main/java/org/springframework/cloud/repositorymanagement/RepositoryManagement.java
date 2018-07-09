package org.springframework.cloud.repositorymanagement;

import java.util.Collections;
import java.util.List;

/**
 * Informs whether the given password management is applicable.
 * Also capable of fetching contents of a file.
 *
 * @author Marcin Grzejszczak
 * @since 0.0.1
 */
public interface RepositoryManagement {

	/**
	 *
	 * @param org - for the given organization
	 * @return list of corresponding repositories
	 */
	default List<String> repositories(String org) {
		return Collections.emptyList();
	}

	/**
	 * Fetches the contents of the file
	 * @param org - organization
	 * @param repo - repository
	 * @param branch - branch of the repository
	 * @param filePath - path to the file
	 * @return contents of the file or empty string if file not found
	 */
	default String fileContent(String org, String repo, String branch, String filePath) {
		return "";
	}
}
