package wakfulib.ui.tv.porst.splib.file;

import java.io.File;

/**
 * Class for recursive directory traversal.
 */
public final class DirectoryTraverser {

	/**
	 * Traverses a directory.
	 * 
	 * @param directory The directory to traverse.
	 * @param callback The callback object to invoked for each encountered file.
	 */
	public static void traverse(File directory, final IDirectoryTraversalVisitor callback) {

		if (directory.isDirectory()) {
			for (File file : directory.listFiles()) {
				if (!file.isDirectory()) {
					callback.visit(file);
				}
			}

			for (File file : directory.listFiles()) {
				if (file.isDirectory()) {
					traverse(file, callback);
				}
			}
		}

	}
}