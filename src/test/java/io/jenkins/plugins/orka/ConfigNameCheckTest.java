package io.jenkins.plugins.orka;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.util.FormValidation;
import io.jenkins.plugins.orka.client.OrkaClient;
import io.jenkins.plugins.orka.client.VMResponse;
import io.jenkins.plugins.orka.helpers.ClientFactory;

@RunWith(Parameterized.class)
public class ConfigNameCheckTest {
    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Parameterized.Parameters(name = "{index}: Test with name={0}, configName={1}, validation={2}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] { { "second", "uniqueName", FormValidation.Kind.OK },
                { "second", "second", FormValidation.Kind.ERROR }, { "second", "s", FormValidation.Kind.ERROR }, });
    }

    private ClientFactory factory;
    private final String vmName;
    private final String configName;
    private final FormValidation.Kind validationKind;

    public ConfigNameCheckTest(String vmName, String newConfigName, FormValidation.Kind validationKind) {
        this.vmName = vmName;
        this.configName = newConfigName;
        this.validationKind = validationKind;
    }

    @Before
    public void initialize() throws IOException {
        VMResponse firstVM = new VMResponse("first", "deployed", 12, "Mojave.img", "firstImage", "default");
        VMResponse secondVM = new VMResponse(this.vmName, "not deployed", 24, "Mojave.img", "secondImage", "default");
        List<VMResponse> response = Arrays.asList(firstVM, secondVM);
        OrkaClient client = mock(OrkaClient.class);

        this.factory = mock(ClientFactory.class);
        when(factory.getOrkaClient(anyString(), anyString())).thenReturn(client);
        when(client.getVMs()).thenReturn(response);
    }

    @Test
    public void when_check_config_name_in_orka_slave_should_return_correct_validation_kind() throws IOException {
        OrkaSlave.DescriptorImpl descriptor = new OrkaSlave.DescriptorImpl();
        descriptor.setClientFactory(this.factory);

        FormValidation validation = descriptor.doCheckConfigName(this.configName, "127.0.0.1", "credentialsId", true);

        assertEquals(this.validationKind, validation.kind);
    }

    @Test
    public void when_check_config_name_in_slave_template_should_return_correct_validation_kind() throws IOException {
        SlaveTemplate.DescriptorImpl descriptor = new SlaveTemplate.DescriptorImpl();
        descriptor.setClientFactory(this.factory);

        FormValidation validation = descriptor.doCheckConfigName(this.configName, "127.0.0.1", "credentialsId", true);

        assertEquals(this.validationKind, validation.kind);
    }
}