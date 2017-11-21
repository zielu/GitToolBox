package zielu.junit5.intellij;

import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;

public class IdeaLightExtension extends IdeaExtension implements BeforeAllCallback, AfterAllCallback,
        ParameterResolver {
    private static final Namespace NAMESPACE = Namespace.create(IdeaLightExtension.class);

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        super.beforeAll(context);

        IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
        LightProjectDescriptor projectDescriptor = getProjectDescriptor(context);
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder = factory.createLightFixtureBuilder(
                projectDescriptor);
        IdeaProjectTestFixture fixture = fixtureBuilder.getFixture();
        Store store = getStore(context);
        store.put(IdeaProjectTestFixture.class, fixture);
    }

    private Store getStore(ExtensionContext context) {
        return context.getStore(NAMESPACE);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        getStore(context).remove(IdeaProjectTestFixture.class);
        super.afterAll(context);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return supportsParameter(parameterContext, IdeaProjectTestFixture.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (IdeaProjectTestFixture.class.isAssignableFrom(parameterContext.getParameter().getType())) {
            return getStore(extensionContext).get(IdeaProjectTestFixture.class, IdeaProjectTestFixture.class);
        }
        return null;
    }

    private LightProjectDescriptor getProjectDescriptor(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        List<Method> methods = ReflectionUtils.findMethods(testClass, method ->
                ReflectionUtils.isStatic(method) &&
                        method.getParameterCount() == 0 &&
                        LightProjectDescriptor.class.isAssignableFrom(method.getReturnType())
        );
        if (methods.size() == 1) {
            return (LightProjectDescriptor) ReflectionUtils.invokeMethod(methods.get(0), null);
        }
        return null;
    }
}
