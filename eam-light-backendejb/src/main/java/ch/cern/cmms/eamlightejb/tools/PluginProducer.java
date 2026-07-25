package ch.cern.cmms.eamlightejb.tools;

import ch.cern.cmms.eamlightejb.data.ApplicationData;
import ch.cern.cmms.plugins.LDAPPlugin;
import ch.cern.cmms.plugins.LDAPPluginImpl;
import ch.cern.cmms.plugins.SharedPlugin;
import ch.cern.cmms.plugins.SharedPluginImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PluginProducer {

    @Autowired
    private ApplicationData applicationData;

    @Bean
    public SharedPlugin sharedPlugin() {
        return new SharedPluginImpl();
    }

    @Bean
    public LDAPPlugin ldapPlugin() {
        LDAPPlugin ldapPlugin = new LDAPPluginImpl();
        ldapPlugin.init(applicationData.getLDAPServer(), applicationData.getLDAPPort());
        return ldapPlugin;
    }
}
