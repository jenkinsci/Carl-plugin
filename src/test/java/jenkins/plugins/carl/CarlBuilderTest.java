/*
 * The MIT License
 *
 * Copyright 2020 TPO.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.plugins.carl;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tools.InstallSourceProperty;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.WithTimeout;

public class CarlBuilderTest {

    static final String INSTALLATION_NAME = "Carl v1";
    static final String CONFIG_SOURCEPATH = "PEGASUSADMIN";
    static final String CONFIG_APPLINAME = "Pegasus";

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();
    
    private final CarlInstallation installation;
    private final boolean runIntegration;
    
    public CarlBuilderTest()  {
        String installationHome = System.getProperty("tests.installPath");
        installation = new CarlInstallation(INSTALLATION_NAME, installationHome, new ArrayList<InstallSourceProperty>());
        runIntegration = !installationHome.isEmpty();
        if (!runIntegration)
            System.out.println("WARNING:  'carlTests.installPath' property not defined defined: skipping integration tests!");
        }

    @Before
    public void setUp()  {
        CarlInstallation.DescriptorImpl descriptor = (CarlInstallation.DescriptorImpl) jenkinsRule.jenkins.getDescriptor(CarlInstallation.class);
        descriptor.setInstallations(installation);
        }

    @Test
    public void testRoundTripConfig() throws Exception  {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();
        CarlBuilder beforeBuilder = new CarlBuilder(INSTALLATION_NAME, "test\\path", CONFIG_APPLINAME);
        beforeBuilder.setQualityGate("test, quality, gate");
        project.getBuildersList().add(beforeBuilder);
        jenkinsRule.submit( jenkinsRule.createWebClient().getPage(project, "configure").getFormByName("config") );
        
        CarlBuilder afterBuilder = project.getBuildersList().get(CarlBuilder.class);
        jenkinsRule.assertEqualBeans(beforeBuilder, afterBuilder, "installationName,sourcePath,applicationName,qualityGate,logPath,displayLog");
        }
    
    @Test
    @WithTimeout(10*60)
    public void testIntegration_AnalysisFailure() throws Exception  {
        assumeTrue(runIntegration);
        String sourcesPath = System.getProperty("tests.badSourcesPath");
        assumeFalse("'carlTests.badSourcesPath' property must be defined", sourcesPath.isEmpty());
        
        CarlBuilder carlBuilder = new CarlBuilder(INSTALLATION_NAME, sourcesPath, CONFIG_APPLINAME);
        carlBuilder.setQualityGate("critical, tpv, CISQ");
        FreeStyleBuild build = getNewBuild(carlBuilder);
        jenkinsRule.assertBuildStatus(Result.FAILURE, build);
        jenkinsRule.assertLogContains("ERROR: Too much errors found by Carl analysis!", build);
        }
        
    @Test
    @WithTimeout(10*60)
    public void testIntegration_AnalysisSuccess() throws Exception  {
        assumeTrue(runIntegration);
        String sourcesPath = System.getProperty("tests.correctSourcesPath");
        assumeFalse("'carlTests.correctSourcesPath' property must be defined", sourcesPath.isEmpty());
        
        CarlBuilder carlBuilder = new CarlBuilder(INSTALLATION_NAME, sourcesPath, CONFIG_APPLINAME);
        carlBuilder.setQualityGate("critical");
        FreeStyleBuild build = getNewBuild(carlBuilder);
        jenkinsRule.assertBuildStatus(Result.SUCCESS, build);
        jenkinsRule.assertLogNotContains("ERROR: Too much errors found by Carl analysis!", build);
        assertEquals(build.getArtifacts().get(0).getFileName(), CarlBuilder.PDF_FILENAME);
        }
    
    private FreeStyleBuild getNewBuild(CarlBuilder builder) throws IOException, InterruptedException, ExecutionException  {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();
        project.getBuildersList().add(builder);
        return project.scheduleBuild2(0).get();
        }
    
    }
