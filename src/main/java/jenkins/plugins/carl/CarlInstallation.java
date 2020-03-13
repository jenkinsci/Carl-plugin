package jenkins.plugins.carl;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolInstaller;
import hudson.tools.ToolProperty;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.annotation.CheckForNull;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class CarlInstallation extends ToolInstallation implements NodeSpecific<CarlInstallation>, EnvironmentSpecific<CarlInstallation> {
    
    @DataBoundConstructor
    public CarlInstallation(String name, String home, List<? extends ToolProperty<?>> properties)  {
        super(name, home, properties);
        }

    @Override
    public CarlInstallation forNode(Node node, TaskListener listener) throws IOException, InterruptedException  {
        return new CarlInstallation(getName(), translateFor(node, listener), getProperties().toList());
        }

    @Override
    public CarlInstallation forEnvironment(EnvVars environment)  {
        return new CarlInstallation(getName(), environment.expand(getHome()), getProperties().toList());
        }

    @Override
    public void buildEnvVars(EnvVars env)  {
        String home = Util.fixEmpty(getHome());
        if (home != null)
            env.put("PATH+CARL", home);
        }
    
    static public CarlInstallation[] list()  {
        DescriptorImpl descriptor = ToolInstallation.all().get(DescriptorImpl.class);
        return (descriptor == null) ? new CarlInstallation[0] : descriptor.getInstallations();
        }
    
    static public @CheckForNull CarlInstallation fromName(String name)  {
        for (CarlInstallation installation : list())  {
            if (installation.getName().equals(name))
                return installation;
            }
        return null;
        }
    
    static public @CheckForNull FilePath getExecutableFile(String installationName, Node node, EnvVars env, TaskListener listener) throws IOException, InterruptedException  {
        CarlInstallation installation = fromName(installationName);
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
        return (homeFilePath == null) ? null : homeFilePath.child("CarlQG.exe");
        }
    
    @Extension @Symbol("carl")
    public static class DescriptorImpl extends ToolDescriptor<CarlInstallation>  {
        public DescriptorImpl()  {
            load();
            }
        
        @Override
        public String getDisplayName() {
            return "Carl";
            }

        @Override
        public CarlInstallation[] getInstallations() {
            return super.getInstallations();
            }

        @Override
        public void setInstallations(CarlInstallation... installations) {
            super.setInstallations(installations);
            save();
            }

        @Override
        public List<? extends ToolInstaller> getDefaultInstallers() {
            return super.getDefaultInstallers();
            }
        }
    
    }
