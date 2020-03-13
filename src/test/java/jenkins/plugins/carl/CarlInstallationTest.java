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

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.EnvVars;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.DumbSlave;
import hudson.tools.InstallSourceProperty;
import hudson.util.StreamTaskListener;
import java.io.IOException;
import java.util.ArrayList;
import static org.hamcrest.Matchers.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;


public class CarlInstallationTest {
    
    static final String CONFIG_NAME = "test name";
    static final String CONFIG_HOME = "test\\Home";
    
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();
    
    private final CarlInstallation installation;
    
    public CarlInstallationTest()  {
        installation = new CarlInstallation(CONFIG_NAME, CONFIG_HOME, new ArrayList<InstallSourceProperty>());
        }
    
    @Test
    public void testGetName() {
        assertThat(installation.getName(), is(CONFIG_NAME));
        }
    
    @Test
    public void testGetHome() {
        assertThat(installation.getHome(), is(CONFIG_HOME));
        }
    
    @Test
    public void testFromName()  {
        initToolInstallation();
        assertThat(CarlInstallation.fromName(CONFIG_NAME), is(notNullValue()));
        }
    
    @Test
    public void testForNode() throws Exception {
        DumbSlave agent = jenkinsRule.createSlave();
        agent.setMode(Node.Mode.EXCLUSIVE);
        TaskListener log = StreamTaskListener.fromStdout();
        CarlInstallation install = installation.forNode(agent, log);
        assertEquals(installation.getHome(), install.getHome());
    }

    @Test
    public void testForEnvironment() throws Exception  {
        CarlInstallation installation2 = new CarlInstallation("TestName2", "$VAR1\\$VAR2", new ArrayList<InstallSourceProperty>());
        EnvVars env = new EnvVars();
        env.put("VAR1", "path 1");
        env.put("VAR2", "path 2");
        CarlInstallation install = installation2.forEnvironment(env);
        assertEquals(install.getHome(), "path 1\\path 2");
        }
    
    @Test
    public void testPresenceInJenkinsConfig() throws IOException, SAXException  {
        HtmlPage page = jenkinsRule.createWebClient().goTo("configureTools");
        assertEquals("Expect to find one instance of Carl", page.getElementsByName("jenkins-plugins-carl-CarlInstallation").size(), 1);
        }
    
    private void initToolInstallation()  {
        CarlInstallation.DescriptorImpl descriptor = (CarlInstallation.DescriptorImpl) jenkinsRule.jenkins.getDescriptor(CarlInstallation.class);
        descriptor.setInstallations(installation);
        }
    
    }
