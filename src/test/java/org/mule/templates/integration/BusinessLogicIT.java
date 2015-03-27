/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import static org.junit.Assert.assertEquals;
import static org.mule.templates.builders.SfdcObjectBuilder.anLead;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.context.notification.NotificationException;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.templates.test.utils.ListenerProbe;

import com.mulesoft.module.batch.BatchTestHelper;
import com.sforce.soap.partner.SaveResult;

/**
 * The objective of this class is to validate the correct behavior of the
 * Anypoint Template that make calls to external systems.
 * 
 */
public class BusinessLogicIT extends AbstractTemplateTestCase {

	private BatchTestHelper helper;
	private List<Map<String, Object>> createdLeadsInA = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> createdLeadInB = new ArrayList<Map<String, Object>>();

	private SubflowInterceptingChainLifecycleWrapper createLeadInAFlow;
	private SubflowInterceptingChainLifecycleWrapper createLeadInBFlow;
	
	private SubflowInterceptingChainLifecycleWrapper deleteLeadFromAflow;
	private SubflowInterceptingChainLifecycleWrapper deleteLeadFromBflow;
	
	@BeforeClass
	public static void init() {
		// Set the frequency between polls to 10 seconds
		System.setProperty("polling.frequency", "10000");

		// Set the poll starting delay to 20 seconds
		System.setProperty("polling.startDelayMillis", "20000");
	}
	@Before
	public void setUp() throws Exception {
		stopFlowSchedulers(POLL_FLOW_NAME);
		registerListeners();

		helper = new BatchTestHelper(muleContext);

		// Flow to retrieve leads from target system after syncing
		retrieveLeadFromBFlow = getSubFlow("retrieveLeadFlow");
		retrieveLeadFromBFlow.initialise();

		// Create object in target system to be update
		createLeadInBFlow = getSubFlow("createLeadFlowB");
		createLeadInBFlow.initialise();

		// Create leads in source system to be or not to be synced
		createLeadInAFlow = getSubFlow("createLeadFlowA");
		createLeadInAFlow.initialise();

		// Delete the created leads in A
		deleteLeadFromAflow = getSubFlow("deleteLeadFromAFlow");
		deleteLeadFromAflow.initialise();

		// Delete the created leads in B
		deleteLeadFromBflow = getSubFlow("deleteLeadFromBFlow");
		deleteLeadFromBflow.initialise();

		createEntities();
	}

	@After
	public void tearDown() throws Exception {
		stopFlowSchedulers(POLL_FLOW_NAME);
		deleteEntities(createdLeadsInA);
		deleteEntities(createdLeadInB);
	}

	@Test
	public void testMainFlow() throws Exception {
		// Run poll and wait for it to run
		runSchedulersOnce(POLL_FLOW_NAME);
		waitForPollToRun();

		// Wait for the batch job executed by the poll flow to finish
		helper.awaitJobTermination(TIMEOUT_SEC * 1000, 500);
		helper.assertJobWasSuccessful();

		// Assert first object was sync to target system
		Map<String, Object> payload = invokeRetrieveFlow(retrieveLeadFromBFlow, createdLeadsInA.get(0));
		assertEquals("The lead should have been sync", createdLeadsInA.get(0).get("Email"), payload.get("Email"));

		// Assert fourth object was sync to target system
		Map<String, Object> secondLead = createdLeadsInA.get(1);
		payload = invokeRetrieveFlow(retrieveLeadFromBFlow, secondLead);
		assertEquals("The lead should have been sync (Email)", secondLead.get("Email"), payload.get("Email"));
	}

	private void registerListeners() throws NotificationException {
		muleContext.registerListener(pipelineListener);
	}

	private void waitForPollToRun() {
		pollProber.check(new ListenerProbe(pipelineListener));
	}

	@SuppressWarnings("unchecked")
	private void createEntities() throws MuleException, Exception {

		createdLeadInB.add(anLead().with("Email", buildUniqueName(TEMPLATE_NAME, "b.") + "@test.com").with("FirstName", "FirstName").with("LastName", "LastName").with("Country", "California").with("Company", "Good one Ltd.").build());
		createLeadInBFlow.process(getTestEvent(createdLeadInB, MessageExchangePattern.REQUEST_RESPONSE));

		createdLeadsInA.add(anLead().with("Email", buildUniqueName(TEMPLATE_NAME, "a1.") + "@test.com").with("FirstName", "FirstName1").with("LastName", "LastName1").with("Country", "California").with("Company", "Not so good one Ltd.").build());
		createdLeadsInA.add(anLead().with("Email", buildUniqueName(TEMPLATE_NAME, "a2.") + "@test.com").with("FirstName", "FirstName2").with("LastName", "LastName2").with("Country", "North Dakota").with("Company", "Best one Ltd.").build());

		final MuleEvent event = createLeadInAFlow.process(getTestEvent(createdLeadsInA, MessageExchangePattern.REQUEST_RESPONSE));
		final List<SaveResult> results = (List<SaveResult>) event.getMessage().getPayload();
		int i = 0;
		for (SaveResult result : results) {
			Map<String, Object> leadInA = createdLeadsInA.get(i);
			leadInA.put("Id", result.getId());
			i++;
		}
	}

	private void deleteEntities(List<Map<String, Object>> leads) throws MuleException, Exception {

		final List<Object> idList = new ArrayList<Object>();
		for (final Map<String, Object> c : leads) {
			idList.add(c.get("Id"));
		}
		deleteLeadFromAflow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));


		idList.clear();
		for (final Map<String, Object> createdLead : leads) {
			final Map<String, Object> lead = invokeRetrieveFlow(retrieveLeadFromBFlow, createdLead);
			if (lead != null) {
				idList.add(lead.get("Id"));
			}
		}
		deleteLeadFromBflow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
	}

}
