package jenkins.plugins.castlite;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.model.EnvironmentSpecific;
import hudson.model.Item;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolInstaller;
import hudson.tools.ToolProperty;
import hudson.util.ListBoxModel;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.annotation.CheckForNull;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class CastLiteInstallation extends ToolInstallation implements NodeSpecific<CastLiteInstallation>, EnvironmentSpecific<CastLiteInstallation> {
    
    private final String dashboardAddress;
    private final String credentialsId;
    
    @DataBoundConstructor
    public CastLiteInstallation(String name, String home, String dashboardAddress, String credentialsId, List<? extends ToolProperty<?>> properties)  {
        super(name, home, properties);
        this.dashboardAddress   = dashboardAddress;
        this.credentialsId      = credentialsId;
        }

    @Override
    public CastLiteInstallation forNode(Node node, TaskListener listener) throws IOException, InterruptedException  {
        return new CastLiteInstallation(getName(), translateFor(node, listener), getDashboardAddress(), getCredentialsId(), getProperties().toList());
        }

    @Override
    public CastLiteInstallation forEnvironment(EnvVars environment)  {
        return new CastLiteInstallation(getName(), environment.expand(getHome()), getDashboardAddress(), getCredentialsId(), getProperties().toList());
        }

    @Override
    public void buildEnvVars(EnvVars env)  {
        String home = Util.fixEmpty(getHome());
        if (home != null)
            env.put("PATH+CASTLITE", home);
        }
    
    public String getDashboardAddress()  {
        return dashboardAddress;
        }
    
    public String getCredentialsId()  {
        return credentialsId;
        }
    
    public IdCredentials getDashboardCredential(Run run)  {
        return CredentialsProvider.findCredentialById(credentialsId, IdCredentials.class, run);
        }

    static public CastLiteInstallation[] list()  {
        DescriptorImpl descriptor = ToolInstallation.all().get(DescriptorImpl.class);
        return (descriptor == null) ? new CastLiteInstallation[0] : descriptor.getInstallations();
        }
    
    static public @CheckForNull CastLiteInstallation fromName(String name)  {
        for (CastLiteInstallation installation : list())  {
            if (installation.getName().equals(name))
                return installation;
            }
        return null;
        }
    
    static public @CheckForNull FilePath getExecutableFile(String installationName, Node node, EnvVars env, TaskListener listener) throws IOException, InterruptedException  {
        CastLiteInstallation installation = fromName(installationName);
        if (installation == null)
            return null;
        if (node != null)
            installation = installation.forNode(node, listener);
        if (env != null)
            installation = installation.forEnvironment(env);
        String home = installation.getHome();
        if (home == null)
            return null;
        
        File homeFile = new File(home);
        FilePath homeFilePath = (node == null) ? new FilePath(homeFile) : node.createPath(home);
        return (homeFilePath == null) ? null : homeFilePath.child("CastEchoQG.exe");
        }
    
    @Extension @Symbol("castlite")
    public static class DescriptorImpl extends ToolDescriptor<CastLiteInstallation>  {
        public DescriptorImpl()  {
            load();
            }
        
        @Override
        public String getDisplayName() {
            return "CastLite";
            }

        @Override
        public CastLiteInstallation[] getInstallations() {
            return super.getInstallations();
            }

        @Override
        public void setInstallations(CastLiteInstallation... installations) {
            super.setInstallations(installations);
            save();
            }

        @Override
        public List<? extends ToolInstaller> getDefaultInstallers() {
            return super.getDefaultInstallers();
            }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item context, @QueryParameter String credentialsId) {
            Jenkins jenkins = Jenkins.getInstanceOrNull();
            if (jenkins == null)
                return null;
            
            if (!jenkins.hasPermission(Jenkins.ADMINISTER))
                return new StandardListBoxModel().includeCurrentValue(credentialsId);

            return new StandardUsernameListBoxModel()
                    .includeEmptyValue()
                    .includeAs(ACL.SYSTEM, jenkins, UsernamePasswordCredentialsImpl.class)
                    .includeCurrentValue(credentialsId);
            }    
        }
    
    }
