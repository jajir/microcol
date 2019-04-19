package org.microcol.test.executer;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.microcol.test.TC_02_founding_colony_test;

public class Helper_TC02_Test {


    @Test
    @Tag("local")
    void start_TC02_test() throws Exception {
	SummaryGeneratingListener listener = new SummaryGeneratingListener();
	LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
		.selectors(selectClass(TC_02_founding_colony_test.class)).build();
	Launcher launcher = LauncherFactory.create();
	launcher.registerTestExecutionListeners(listener);
	launcher.execute(request);

	listener.getSummary().getFailures().forEach(fail -> {
	    System.err.println(fail.getTestIdentifier());
	});
	if (listener.getSummary().getFailures().size() > 0) {
	    fail("There are failed test");
	}
    }

}