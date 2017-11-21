package zielu.junit5.intellij;

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
