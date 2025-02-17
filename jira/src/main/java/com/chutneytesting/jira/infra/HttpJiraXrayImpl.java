/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.jira.infra;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.chutneytesting.jira.domain.JiraTargetConfiguration;
import com.chutneytesting.jira.domain.JiraXrayApi;
import com.chutneytesting.jira.domain.exception.NoJiraConfigurationException;
import com.chutneytesting.jira.xrayapi.JiraIssueType;
import com.chutneytesting.jira.xrayapi.Xray;
import com.chutneytesting.jira.xrayapi.XrayTestExecTest;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class HttpJiraXrayImpl implements JiraXrayApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpJiraXrayImpl.class);

    private static final int MS_TIMEOUT = 10 * 1000; // 10 s

    private final JiraTargetConfiguration jiraTargetConfiguration;

    public HttpJiraXrayImpl(JiraTargetConfiguration jiraTargetConfiguration) {
        this.jiraTargetConfiguration = jiraTargetConfiguration;
        if (!jiraTargetConfiguration.isValid()) {
            throw new NoJiraConfigurationException();
        }
    }

    @Override
    public void updateRequest(Xray xray) {
        String updateUri = jiraTargetConfiguration.url() + "/rest/raven/1.0/import/execution";

        RestTemplate restTemplate = buildRestTemplate(jiraTargetConfiguration);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(updateUri, xray, String.class);
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                LOGGER.debug(response.toString());
                LOGGER.info("Xray successfully updated for " + xray.getTestExecutionKey());
            } else {
                LOGGER.error(response.toString());
            }
        } catch (RestClientException e) {
            throw new RuntimeException("Unable to update test execution [" + xray.getTestExecutionKey() + "] : ", e);
        }
    }

    @Override
    public List<XrayTestExecTest> getTestExecutionScenarios(String xrayId) {
        List<XrayTestExecTest> tests = new ArrayList<>();

        String uriTemplate = jiraTargetConfiguration.url() + "/rest/raven/1.0/api/%s/%s/test";
        String uri = String.format(uriTemplate, isTestPlan(xrayId) ? "testplan" : "testexec", xrayId);

        RestTemplate restTemplate = buildRestTemplate(jiraTargetConfiguration);
        try {
            ResponseEntity<XrayTestExecTest[]> response = restTemplate.getForEntity(uri, XrayTestExecTest[].class);
            if (response.getStatusCode().equals(HttpStatus.OK) && response.getBody() != null) {
                tests = Arrays.stream(response.getBody())
                    .toList();
            } else {
                LOGGER.error(response.toString());
            }
        } catch (RestClientException e) {
            throw new RuntimeException("Unable to get xray test execution[" + xrayId + "] scenarios : ", e);
        }
        return tests;
    }

    @Override
    public void updateStatusByTestRunId(String testRuntId, String executionStatus) {
        String uriTemplate = jiraTargetConfiguration.url() + "/rest/raven/1.0/api/testrun/%s/status?status=%s";
        String uri = String.format(uriTemplate, testRuntId, executionStatus);

        RestTemplate restTemplate = buildRestTemplate(jiraTargetConfiguration);
        try {
            restTemplate.put(uri, null);
        } catch (RestClientException e) {
            throw new RuntimeException("Unable to update xray testRuntId[" + testRuntId + "] with status[" + executionStatus + "] : ", e);
        }
    }

    @Override
    public void associateTestExecutionFromTestPlan(String testPlanId, String testExecutionId) {
        String uriTemplate = jiraTargetConfiguration.url() + "/rest/raven/1.0/api/testplan/%s/testexecution";
        String uri = String.format(uriTemplate, testPlanId);

        RestTemplate restTemplate = buildRestTemplate(jiraTargetConfiguration);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uri, Map.of("add", List.of(testExecutionId)), String.class);
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                LOGGER.debug(response.toString());
                LOGGER.info("Xray successfully associate test execution [" + testExecutionId + "] from test plan [" + testPlanId + "]");
            } else {
                LOGGER.error(response.toString());
                throw new RuntimeException("Unable to associate test execution [" + testExecutionId + "] from test plan [" + testPlanId + "]");
            }
        } catch (RestClientException e) {
            throw new RuntimeException("Unable to associate test execution [" + testExecutionId + "] from test plan [" + testPlanId + "] : ", e);
        }
    }

    @Override
    public String createTestExecution(String testPlanId) {
        Issue parentIssue = getIssue(testPlanId);
        IssueInputBuilder issueInputBuilder = new IssueInputBuilder();

        IssueInput issueInput = issueInputBuilder
            .setProjectKey(parentIssue.getProject().getKey())
            .setIssueTypeId(getIssueTypeByName("Test Execution").getId())
            .setSummary(parentIssue.getSummary())
            .build();

        try (JiraRestClient jiraRestClient = getJiraRestClient()) {
            BasicIssue issue = jiraRestClient.getIssueClient().createIssue(issueInput).claim();
            associateTestExecutionFromTestPlan(testPlanId, issue.getKey());
            return issue.getKey();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create test execution issue from test plan [" + testPlanId + "] : ", e);
        }
    }

    @Override
    public boolean isTestPlan(String issueId) {
        return getIssue(issueId).getIssueType().getId().equals(getIssueTypeByName("Test Plan").getId());
    }

    private Issue getIssue(String issueKey) {
        try (JiraRestClient jiraRestClient = getJiraRestClient()) {
            return jiraRestClient.getIssueClient().getIssue(issueKey).claim();
        } catch (Exception e) {
            throw new RuntimeException("Unable to get issue [" + issueKey + "] : ", e);
        }
    }

    private JiraIssueType getIssueTypeByName(String issueTypeName) {
        String uri = jiraTargetConfiguration.url() + "/rest/api/latest/issuetype";
        Optional<JiraIssueType> issueTypeOptional = Optional.empty();

        RestTemplate restTemplate = buildRestTemplate(jiraTargetConfiguration);
        try {
            ResponseEntity<JiraIssueType[]> response = restTemplate.getForEntity(uri, JiraIssueType[].class);
            if (response.getStatusCode().equals(HttpStatus.OK) && response.getBody() != null) {
                issueTypeOptional = Arrays.stream(response.getBody())
                    .filter(issueType -> issueType.getName().equals(issueTypeName))
                    .findFirst();
            } else {
                LOGGER.error(response.toString());
            }
        } catch (RestClientException e) {
            throw new RuntimeException("Unable to get issues type list : ", e);
        }
        return issueTypeOptional.orElseThrow(() -> new RuntimeException("Unable to get issue type [" + issueTypeName + "]"));
    }

    private SSLContext buildSslContext() {
        try {
            SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
            sslContextBuilder.loadTrustMaterial(null, (chain, authType) -> true);
            return sslContextBuilder.build();
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private RestTemplate buildRestTemplate(JiraTargetConfiguration jiraTargetConfiguration) {
        RestTemplate restTemplate;
        SSLContext sslContext = buildSslContext();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setSSLSocketFactory(socketFactory)
            .setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(MS_TIMEOUT, TimeUnit.MILLISECONDS).build())
            .build();

        HttpClientBuilder httpClientBuilder = HttpClients.custom().setConnectionManager(connectionManager);

        if (jiraTargetConfiguration.hasProxy()) {
            configureProxy(jiraTargetConfiguration, httpClientBuilder);
        }

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClientBuilder.build());
        requestFactory.setConnectTimeout(MS_TIMEOUT);

        restTemplate = new RestTemplate(requestFactory);
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(jiraTargetConfiguration.username(), jiraTargetConfiguration.password()));

        return restTemplate;
    }

    private void configureProxy(JiraTargetConfiguration jiraTargetConfiguration, HttpClientBuilder httpClientBuilder) {
        try {
            URI proxyUri = new URI(jiraTargetConfiguration.urlProxy());
            HttpHost proxyHttpHost = HttpHost.create(proxyUri);
            httpClientBuilder.setProxy(proxyHttpHost);

            if (jiraTargetConfiguration.hasProxyWithAuth()) {
                BasicCredentialsProvider credentialsProvider = getBasicCredentialsProvider(jiraTargetConfiguration, proxyHttpHost);
                String proxyAuthorization = Base64
                    .getEncoder()
                    .encodeToString((jiraTargetConfiguration.userProxy() + ":" + jiraTargetConfiguration.passwordProxy()).getBytes());
                httpClientBuilder
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .setDefaultHeaders(List.of(new BasicHeader("Proxy-Authorization", "Basic " + proxyAuthorization)));
            }
        } catch (URISyntaxException e) {
            LOGGER.error("Unexpected proxy url", e);
        }
    }

    private BasicCredentialsProvider getBasicCredentialsProvider(JiraTargetConfiguration jiraTargetConfiguration, HttpHost proxyHttpHost) {
    BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(
        new AuthScope(proxyHttpHost),
        new UsernamePasswordCredentials(jiraTargetConfiguration.userProxy(), jiraTargetConfiguration.passwordProxy().toCharArray())
    );
    return credentialsProvider;
  }

  private JiraRestClient getJiraRestClient() {
        try {
            AsynchronousJiraRestClientFactory asynchronousJiraRestClientFactory = new AsynchronousJiraRestClientFactory();
            return asynchronousJiraRestClientFactory
                .createWithBasicHttpAuthentication(
                    new URI(jiraTargetConfiguration.url()),
                    jiraTargetConfiguration.username(),
                    jiraTargetConfiguration.password());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to instantiate Jira rest client from url [" + jiraTargetConfiguration.url() + "] : ", e);
        }
    }
}
