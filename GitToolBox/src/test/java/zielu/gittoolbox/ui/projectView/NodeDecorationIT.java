package zielu.gittoolbox.ui.projectView;

import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import zielu.intellij.junit5.ContainsTempFiles;
import zielu.intellij.junit5.IdeaLightExtension;

import java.nio.file.Paths;

@ExtendWith(IdeaLightExtension.class)
@ContainsTempFiles
class NodeDecorationIT {

    private static CodeInsightTestFixture myFixture;

    static LightProjectDescriptor getDescriptor() {
        LightProjectDescriptor descriptor = new DefaultLightProjectDescriptor();
        return descriptor;
    }

    @BeforeAll
    static void beforeAll(IdeaProjectTestFixture fixture) throws Exception {
        myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(fixture, new LightTempDirTestFixtureImpl(true));

        myFixture.setUp();
        myFixture.setTestDataPath(Paths.get(".", "testRun", "it").toAbsolutePath().toString());
    }

    @AfterAll
    static void afterAll() throws Exception {
        try {
            myFixture.tearDown();
        }
        finally {
            myFixture = null;
        }
    }

    @Test
    void test() {

    }
}
