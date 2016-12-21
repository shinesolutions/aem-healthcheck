
package com.github.shinesolutions.healthcheck.servlets;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import junitx.framework.Assert;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.hc.api.Result;
import org.apache.sling.hc.api.execution.HealthCheckExecutionOptions;
import org.apache.sling.hc.api.execution.HealthCheckExecutionResult;
import org.apache.sling.hc.api.execution.HealthCheckExecutor;
import org.apache.sling.hc.util.HealthCheckMetadata;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;

public class HealthCheckExecutorServletTest {

    protected HealthCheckExecutorServlet servlet;
    
    @Mock
    protected SlingHttpServletRequest mockRequest;
    @Mock
    protected SlingHttpServletResponse mockResponse;
    @Mock
    protected HealthCheckExecutor mockHCExecutor;
    @Mock
    protected PrintWriter printWriter;
    
    @Captor
    protected ArgumentCaptor<String> stringTagsCaptor;
    @Captor
    protected ArgumentCaptor<HealthCheckExecutionOptions> optionsCaptor;
    @Captor
    protected ArgumentCaptor<String> stringResultCaptor;
        
    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        
        when(mockRequest.getParameter(anyString())).thenReturn(null);
        when(mockResponse.getWriter()).thenReturn(printWriter);
        List<HealthCheckExecutionResult> results = new ArrayList<>();
        when(mockHCExecutor.execute(any(HealthCheckExecutionOptions.class), anyString())).thenReturn(results);

        this.servlet = spy(new HealthCheckExecutorServlet());
        Whitebox.setInternalState(servlet, "healthCheckExecutor", mockHCExecutor);
    }
    
    @Test
    public void testHeadersAreSet() throws Exception {
        servlet.doGet(mockRequest, mockResponse);
        
        verify(mockResponse, times(1)).setContentType("application/json");
        verify(mockResponse, times(1)).setHeader(eq("Cache-Control"), anyString());
    }
    
    /**
     * /system/health
     */
    @Test
    public void testNoTagsAndNoResults() throws Exception {
        when(mockRequest.getParameter("tags")).thenReturn(null);
        when(mockRequest.getParameter("combineTagsOr")).thenReturn(null);

        servlet.doGet(mockRequest, mockResponse);
        
        verify(mockRequest,    times(1)).getParameter("tags");
        verify(mockRequest,    times(1)).getParameter("combineTagsOr");
        verify(mockHCExecutor, times(1)).execute(optionsCaptor.capture(), stringTagsCaptor.capture());
        verify(mockResponse,   times(1)).getWriter();
        verify(printWriter,    times(1)).write(stringResultCaptor.capture());
        
        Assert.assertEquals("Should combine with OR", true, optionsCaptor.getValue().isCombineTagsWithOr());
        Assert.assertEquals("Should not provide tags", Arrays.asList(""), stringTagsCaptor.getAllValues());
        Assert.assertEquals("Should be blank JSON array", "{\"results\":[]}", stringResultCaptor.getValue());
    }
    
    /**
     * /system/health?tags=devops
     */
    @Test
    public void testSingleTagAndOneResult() throws Exception {
        List<HealthCheckExecutionResult> results = new ArrayList<>();
        results.add(new TestHealthCheckResult());
        
        when(mockRequest.getParameter("tags")).thenReturn("devops");
        when(mockRequest.getParameter("combineTagsOr")).thenReturn(null);
        when(mockHCExecutor.execute(any(HealthCheckExecutionOptions.class), anyString())).thenReturn(results);

        servlet.doGet(mockRequest, mockResponse);
        
        verify(mockRequest,    times(1)).getParameter("tags");
        verify(mockRequest,    times(1)).getParameter("combineTagsOr");
        verify(mockHCExecutor, times(1)).execute(optionsCaptor.capture(), stringTagsCaptor.capture());
        verify(mockResponse,   times(1)).getWriter();
        verify(printWriter,    times(1)).write(stringResultCaptor.capture());
        
        Assert.assertEquals("Should combine with OR", true, optionsCaptor.getValue().isCombineTagsWithOr());
        Assert.assertEquals("Should provide a single tag", Arrays.asList("devops"), stringTagsCaptor.getAllValues());
        Assert.assertEquals("Should be JSON object with one result", "{\"results\":[{\"name\":\"\",\"status\":\"OK\",\"timeMs\":0}]}", stringResultCaptor.getValue());
    }
    
    /**
     * /system/health?tags=devops,security
     */
    @Test
    public void testMultipleTags() throws Exception {
        when(mockRequest.getParameter("tags")).thenReturn("devops,security");
        when(mockRequest.getParameter("combineTagsOr")).thenReturn(null);

        servlet.doGet(mockRequest, mockResponse);
        
        verify(mockRequest,    times(1)).getParameter("tags");
        verify(mockRequest,    times(1)).getParameter("combineTagsOr");
        verify(mockHCExecutor, times(1)).execute(optionsCaptor.capture(), stringTagsCaptor.capture());
        verify(mockResponse,   times(1)).getWriter();
        verify(printWriter,    times(1)).write(stringResultCaptor.capture());
        
        Assert.assertEquals("Should combine with OR", true, optionsCaptor.getValue().isCombineTagsWithOr());
        Assert.assertEquals("Should provide two tags", Arrays.asList("devops", "security"), stringTagsCaptor.getAllValues());
        Assert.assertEquals("Should be blank JSON array", "{\"results\":[]}", stringResultCaptor.getValue());
    }
    
    /**
     * /system/health?tags=devops&combineTagsOr=false
     */
    @Test
    public void testCombineTagsOrIsFalse() throws Exception {        
        when(mockRequest.getParameter("tags")).thenReturn("devops");
        when(mockRequest.getParameter("combineTagsOr")).thenReturn("false");

        servlet.doGet(mockRequest, mockResponse);
        
        verify(mockRequest,    times(1)).getParameter("tags");
        verify(mockRequest,    times(1)).getParameter("combineTagsOr");
        verify(mockHCExecutor, times(1)).execute(optionsCaptor.capture(), stringTagsCaptor.capture());
        verify(mockResponse,   times(1)).getWriter();
        verify(printWriter,    times(1)).write(stringResultCaptor.capture());
        
        Assert.assertEquals("Should not combine with OR", false, optionsCaptor.getValue().isCombineTagsWithOr());
        Assert.assertEquals("Should provide a single tags", Arrays.asList("devops"), stringTagsCaptor.getAllValues());
        Assert.assertEquals("Should be blank JSON array", "{\"results\":[]}", stringResultCaptor.getValue());
    }
    
    private static class TestHealthCheckResult implements HealthCheckExecutionResult {

        public TestHealthCheckResult() {}
        
        @Override
        public Result getHealthCheckResult() {
            return new Result(Result.Status.OK, "");
        }

        @Override
        public long getElapsedTimeInMs() {
            return 0l;
        }

        @Override
        public Date getFinishedAt() {
            return new Date();
        }

        @Override
        public boolean hasTimedOut() {
            return false;
        }

        @Override
        public HealthCheckMetadata getHealthCheckMetadata() {
            return null;
        }
    }
}
