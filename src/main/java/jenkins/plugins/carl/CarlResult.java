package jenkins.plugins.carl;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CarlResult implements Serializable {
    
    long checkedRuleCount;
    long fileCount;
    long issueCount;
    
    static final class Collect implements FilePath.FileCallable<CarlResult>  {
        @Override
        public CarlResult invoke(File f, VirtualChannel channel)  {
            JSONParser jsonParser = new JSONParser();
            CarlResult result = null;
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))  {
                JSONObject summary = (JSONObject)jsonParser.parse(reader);
                result = new CarlResult();
                result.checkedRuleCount     = (long)summary.get("Count of affected Rules");
                result.fileCount            = (long)summary.get("Total count of Files");
                result.issueCount           = (long)summary.get("Total number of issues");
                }
            catch (IOException | ParseException e)  { }
            return result;
            }

        @Override
        public void checkRoles(org.jenkinsci.remoting.RoleChecker checker) throws SecurityException  { }
        }
    
    protected CarlResult()  { }

    }
