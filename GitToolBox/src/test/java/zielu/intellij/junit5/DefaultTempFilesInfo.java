package zielu.intellij.junit5;

import java.nio.file.Path;

class DefaultTempFilesInfo implements TempFilesInfo {
    private final Path myTempDir;

    DefaultTempFilesInfo(Path tempDir) {
        this.myTempDir = tempDir;
    }

    @Override
    public Path tempDir() {
        return myTempDir;
    }
}
