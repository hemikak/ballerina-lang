/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.projects.test;

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.PackageResolution;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.bala.BalaProject;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.repos.TempDirCompilationCache;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains cases to test package resolution logic.
 *
 * @since 2.0.0
 */
public class PackageResolutionTests extends BaseTest {
    private static final Path RESOURCE_DIRECTORY = Paths.get(
            "src/test/resources/projects_for_resolution_tests").toAbsolutePath();
    private static final Path testBuildDirectory = Paths.get("build").toAbsolutePath();

    @BeforeTest
    public void setup() throws IOException {
        // Compile and cache dependency for custom repo tests
        cacheDependencyToLocalRepo(RESOURCE_DIRECTORY.resolve("package_c_with_pkg_private_function"));
    }

    @Test(description = "tests resolution with zero direct dependencies")
    public void testProjectWithZeroDependencies() {
        // package_c --> {}
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_c");
        BuildProject buildProject = BuildProject.load(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.errors().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected compilation diagnostics");

        // Check direct package dependencies
        Assert.assertEquals(buildProject.currentPackage().packageDependencies().size(), 0,
                "Unexpected number of dependencies");
    }

    @Test(description = "tests resolution with one direct dependency")
    public void testProjectWithOneDependency() {
        // package_b --> package_c
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_b");
        BuildProject buildProject = BuildProject.load(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.errors().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected compilation diagnostics");

        // Check direct package dependencies
        Assert.assertEquals(buildProject.currentPackage().packageDependencies().size(), 1,
                "Unexpected number of dependencies");
    }

    @Test(description = "tests resolution with one transitive dependency")
    public void testProjectWithOneTransitiveDependency() {
        // package_a --> package_b --> package_c
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_a");
        BuildProject buildProject = BuildProject.load(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.errors().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected compilation diagnostics");

        // Check direct package dependencies
        Assert.assertEquals(buildProject.currentPackage().packageDependencies().size(), 1,
                "Unexpected number of dependencies");
    }

    @Test(description = "tests resolution with two direct dependencies and one transitive")
    public void testProjectWithTwoDirectDependencies() {
        // package_d --> package_b --> package_c
        // package_d --> package_e
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_d");
        BuildProject buildProject = BuildProject.load(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.errors().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected compilation diagnostics");

        // Check direct package dependencies
        Assert.assertEquals(buildProject.currentPackage().packageDependencies().size(), 2,
                "Unexpected number of dependencies");
    }

    @Test(description = "tests resolution with one transitive dependency",
            expectedExceptions = ProjectException.class,
            expectedExceptionsMessageRegExp = "Transitive dependency cannot be found: " +
                    "org=samjs, package=package_missing, version=1.0.0")
    public void testProjectWithMissingTransitiveDependency() throws IOException {
        // package_missing_transitive_dep --> package_b --> package_c
        // package_missing_transitive_dep --> package_k --> package_z (this is missing)
        Path balaPath = RESOURCE_DIRECTORY.resolve("balas").resolve("missing_transitive_deps")
                .resolve("samjs-package_kk-any-1.0.0.bala");
        BCompileUtil.copyBalaToDistRepository(balaPath, "samjs", "package_kk", "1.0.0");

        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_missing_transitive_dep");
        BuildProject buildProject = BuildProject.load(projectDirPath);
        buildProject.currentPackage().getResolution();
    }

    @Test(description = "Test dependencies should not be stored in bala archive")
    public void testProjectWithTransitiveTestDependencies() throws IOException {
        // package_with_test_dependency --> package_c
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_with_test_dependency");
        BuildProject buildProject = BuildProject.load(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Dependency graph should contain two entries here
        DependencyGraph<ResolvedPackageDependency> depGraphOfSrcProject =
                compilation.getResolution().dependencyGraph();
        Assert.assertEquals(depGraphOfSrcProject.getNodes().size(), 2);

        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = jBallerinaBackend.diagnosticResult();
        diagnosticResult.errors().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected compilation diagnostics");

        String balaName = ProjectUtils.getBalaName(buildProject.currentPackage().manifest());
        Path balaDir = testBuildDirectory.resolve("test_gen_balas");
        Path balaPath = balaDir.resolve(balaName);
        Files.createDirectories(balaDir);
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALA, balaDir);

        // Load the bala file now.
        BalaProject balaProject = BalaProject.loadProject(BCompileUtil.getTestProjectEnvironmentBuilder(), balaPath);
        PackageResolution resolution = balaProject.currentPackage().getResolution();

        // Dependency graph should contain only one entry
        DependencyGraph<ResolvedPackageDependency> depGraphOfBala = resolution.dependencyGraph();
        Assert.assertEquals(depGraphOfBala.getNodes().size(), 1);
    }

    @Test(description = "Ultimate test case")
    public void testProjectWithManyDependencies() {
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_runtime");
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_jsonutils");
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_io_1_4_2");
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_io_1_5_0");
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_cache");

        Project project = BCompileUtil.loadProject(
                "projects_for_resolution_tests/ultimate_package_resolution/package_http");

        PackageCompilation compilation = project.currentPackage().getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = jBallerinaBackend.diagnosticResult();
        diagnosticResult.errors().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected compilation diagnostics");


        Package currentPkg = project.currentPackage();
        Assert.assertEquals(currentPkg.packageDependencies().size(), 3);
        DependencyGraph<ResolvedPackageDependency> dependencyGraph = compilation.getResolution().dependencyGraph();

        for (ResolvedPackageDependency graphNode : dependencyGraph.getNodes()) {
            Collection<ResolvedPackageDependency> directDeps = dependencyGraph.getDirectDependencies(graphNode);
            PackageManifest manifest = graphNode.packageInstance().manifest();
            switch (manifest.name().value()) {
                case "io":
                    // Version conflict resolution has happened
                    Assert.assertEquals(manifest.version().toString(), "1.5.0");
                    break;
                case "http":
                    Assert.assertEquals(directDeps.size(), 3);
                    break;
                case "cache":
                    // No test dependencies are available in the graph
                    Assert.assertEquals(directDeps.size(), 1);
                    break;
                case "jsonutils":
                    Assert.assertEquals(graphNode.scope(), PackageDependencyScope.TEST_ONLY);
                    break;
                default:
                    throw new IllegalStateException("Unexpected dependency");
            }
        }
    }

    @Test(description = "tests loading a valid bala project")
    public void testBalaProjectDependencyResolution() {
        Path balaPath = getBalaPath("samjs", "package_b", "0.1.0");
        ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
        defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
        BalaProject balaProject = BalaProject.loadProject(defaultBuilder, balaPath);
        PackageResolution resolution = balaProject.currentPackage().getResolution();
        DependencyGraph<ResolvedPackageDependency> dependencyGraph = resolution.dependencyGraph();
        List<ResolvedPackageDependency> nodeInGraph = dependencyGraph.toTopologicallySortedList();
        Assert.assertEquals(nodeInGraph.size(), 2);
    }

    @Test(dependsOnMethods = "testResolveDependencyFromUnsupportedCustomRepo")
    public void testResolveDependencyFromCustomRepo() {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_b");
        String dependencyContent = "[[dependency]]\n" +
                "org = \"samjs\"\n" +
                "name = \"package_c\"\n" +
                "version = \"0.1.0\"\n" +
                "repository = \"local\"";

        // 1) load the build project
        Environment environment = EnvironmentBuilder.getBuilder().setUserHome(USER_HOME).build();
        ProjectEnvironmentBuilder projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);
        BuildProject project = BuildProject.load(projectEnvironmentBuilder, projectDirPath);

        // 2) set local repository to dependency
        project.currentPackage().dependenciesToml().orElseThrow().modify().withContent(dependencyContent).apply();

        // 3) Compile and check the diagnostics
        PackageCompilation compilation = project.currentPackage().getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();

        // 4) The dependency is expected to load from distribution cache, hence zero diagnostics
        Assert.assertEquals(diagnosticResult.errorCount(), 2);
    }

    @Test(enabled = false)
    public void testResolveDependencyAfterDependencyTomlEdit() {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_b");
        String dependencyContent = "[[dependency]]\n" +
                "org = \"samjs\"\n" +
                "name = \"package_c\"\n" +
                "version = \"0.1.0\"\n" +
                "repository = \"local\"";

        // 1) load the build project
        Environment environment = EnvironmentBuilder.getBuilder().setUserHome(USER_HOME).build();
        ProjectEnvironmentBuilder projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);
        BuildProject project = BuildProject.load(projectEnvironmentBuilder, projectDirPath);

        // 2) set local repository to dependency
        project.currentPackage().dependenciesToml().orElseThrow().modify().withContent(dependencyContent).apply();

        // 3) Compile and check the diagnostics
        PackageCompilation compilation = project.currentPackage().getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();

        // 4) The dependency is expected to load from distribution cache, hence zero diagnostics
        Assert.assertEquals(diagnosticResult.errorCount(), 2);
    }

    @Test
    public void testResolveDependencyFromUnsupportedCustomRepo() {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_b");
        String dependencyContent = "[[dependency]]\n" +
                "org = \"samjs\"\n" +
                "name = \"package_c\"\n" +
                "version = \"0.1.0\"\n" +
                "repository = \"stdlib.local\"";

        // 2) load the build project
        Environment environment = EnvironmentBuilder.getBuilder().setUserHome(USER_HOME).build();
        ProjectEnvironmentBuilder projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);
        BuildProject project = BuildProject.load(projectEnvironmentBuilder, projectDirPath);

        // 3) set local repository to dependency compile the package and check diagnostics
        project.currentPackage().dependenciesToml().get().modify().withContent(dependencyContent).apply();
        PackageCompilation compilation = project.currentPackage().getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();

        // 4) The dependency is expected to load from distribution cache, hence zero diagnostics
        Assert.assertEquals(diagnosticResult.errorCount(), 3);
        List<String> diagnosticMsgs = diagnosticResult.errors().stream()
                .map(Diagnostic::message).collect(Collectors.toList());
        Assert.assertTrue(diagnosticMsgs.contains("cannot resolve module 'samjs/package_c.mod_c1 as mod_c1'"));
    }
}
