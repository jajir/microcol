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
import org.microcol.test.TC_06_buy_and_sell_goods_test;

public class Helper_TC06_Test {


    @Test
    @Tag("wrapper")
    void start_TC06_test() throws Exception {
	SummaryGeneratingListener listener = new SummaryGeneratingListener();
	LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
		.selectors(selectClass(TC_06_buy_and_sell_goods_test.class)).build();
	Launcher launcher = LauncherFactory.create();
	launcher.registerTestExecutionListeners(listener);
	launcher.execute(request);

	listener.getSummary().getFailures().forEach(fail -> {
	    System.err.println(fail.getTestIdentifier());
	});
	if (listener.getSummary().getTotalFailureCount() > 0) {
	    fail("There are failed test");
	}
    }

}
