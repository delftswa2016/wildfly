//JBoss, Home of Professional Open Source.
//This class contains the method testRuntime that is shared by undertow subsystem test cases
public class UndertowSubsystemTest extends AbstractSubsystemBaseTest {

    @Test
    public void testRuntime() throws Exception {
        System.setProperty("server.data.dir", System.getProperty("java.io.tmpdir"));
        System.setProperty("jboss.home.dir", System.getProperty("java.io.tmpdir"));
        System.setProperty("jboss.home.dir", System.getProperty("java.io.tmpdir"));
        System.setProperty("jboss.server.server.dir", System.getProperty("java.io.tmpdir"));
        KernelServicesBuilder builder = createKernelServicesBuilder(UndertowSubsystemTestCase.RUNTIME)
                .setSubsystemXml(getSubsystemXml());
        KernelServices mainServices = builder.build();
        if (!mainServices.isSuccessfulBoot()) {
            Assert.fail(mainServices.getBootError().toString());
        }
        ServiceController<FilterService> connectionLimiter = (ServiceController<FilterService>) mainServices.getContainer().getService(UndertowService.FILTER.append("limit-connections"));
        connectionLimiter.setMode(ServiceController.Mode.ACTIVE);
        FilterService connectionLimiterService = connectionLimiter.getService().getValue();
        HttpHandler result = connectionLimiterService.createHttpHandler(Predicates.truePredicate(), new PathHandler());
        Assert.assertNotNull("handler should have been created", result);


        ServiceController<FilterService> headersFilter = (ServiceController<FilterService>) mainServices.getContainer().getService(UndertowService.FILTER.append("headers"));
        headersFilter.setMode(ServiceController.Mode.ACTIVE);
        FilterService headersService = headersFilter.getService().getValue();
        HttpHandler headerHandler = headersService.createHttpHandler(Predicates.truePredicate(), new PathHandler());
        Assert.assertNotNull("handler should have been created", headerHandler);

        final ServiceName hostServiceName = UndertowService.virtualHostName("default-server", "other-host");
        ServiceController<Host> hostSC = (ServiceController<Host>) mainServices.getContainer().getService(hostServiceName);
        Assert.assertNotNull(hostSC);
        hostSC.setMode(ServiceController.Mode.ACTIVE);
        Host host = hostSC.getValue();
        Assert.assertEquals(1, host.getFilters().size());


        final ServiceName locationServiceName = UndertowService.locationServiceName("default-server", "default-host", "/");
        ServiceController<LocationService> locationSC = (ServiceController<LocationService>) mainServices.getContainer().getService(locationServiceName);
        Assert.assertNotNull(locationSC);
        locationSC.setMode(ServiceController.Mode.ACTIVE);
        LocationService locationService = locationSC.getValue();
        Assert.assertNotNull(locationService);
        connectionLimiter.setMode(ServiceController.Mode.REMOVE);
        final ServiceName jspServiceName = UndertowService.SERVLET_CONTAINER.append("myContainer");
        ServiceController<ServletContainerService> jspServiceServiceController = (ServiceController<ServletContainerService>) mainServices.getContainer().getService(jspServiceName);
        Assert.assertNotNull(jspServiceServiceController);
        JSPConfig jspConfig = jspServiceServiceController.getService().getValue().getJspConfig();
        Assert.assertNotNull(jspConfig);
        Assert.assertNotNull(jspConfig.createJSPServletInfo());

        final ServiceName filterRefName = UndertowService.filterRefName("default-server", "other-host", "/", "static-gzip");

        ServiceController<FilterRef> gzipFilterController = (ServiceController<FilterRef>) mainServices.getContainer().getService(filterRefName);
        gzipFilterController.setMode(ServiceController.Mode.ACTIVE);
        FilterRef gzipFilterRef = gzipFilterController.getService().getValue();
        HttpHandler gzipHandler = gzipFilterRef.createHttpHandler(new PathHandler());
        Assert.assertNotNull("handler should have been created", gzipHandler);


    }

}
