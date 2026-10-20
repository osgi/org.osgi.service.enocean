/*******************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
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
 *
 * SPDX-License-Identifier: Apache-2.0 
 *******************************************************************************/

package org.osgi.test.cases.enocean;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.osgi.framework.ServiceReference;
import org.osgi.service.enocean.EnOceanDevice;
import org.osgi.test.cases.enocean.messages.MessageExample2;
import org.osgi.test.cases.enocean.serial.EspRadioPacket;
import org.osgi.test.cases.enocean.utils.Fixtures;
import org.osgi.test.cases.enocean.utils.ServiceListener;
import org.osgi.test.cases.enocean.utils.Utils;

/**
 * ManualRegistrationTestCase:
 * 
 * - testManualDeviceRegistration, tests initial device registration from a raw
 * Radio teach-in packet that doesn’t contain any profile data, and that is
 * triggered through the step service. This test thus requires the end-user
 * involvement. Finally, the TCK checks that the device's profile has been
 * properly updated.
 * 
 * @author $Id$
 */
public class ManualRegistrationTestCase extends AbstractEnOceanTestCase {

    /**
     * Tests initial device registration from a raw Radio teach-in packet.
     * 
     * @throws Exception
     */
    @Test
    public void testManualDeviceRegistration() throws Exception {

	/* Insert a device */
	MessageExample2 teachIn = new MessageExample2(1, true, 1, true);
	teachIn.setSenderId(Fixtures.HOST_ID_2);
	EspRadioPacket pkt = new EspRadioPacket(teachIn);
	// Push everything in the command...
	String params = Utils.bytesToHex(pkt.serialize());
	super.testStepProxy.execute(MSG_EXAMPLE_2 + params, "Insert a device.");

	String lastServiceEvent = devices.waitForService();
	tlog("lastServiceEvent: " + lastServiceEvent);
	assertThat(lastServiceEvent).as("Timeout reached.").isNotNull();
	ServiceReference<EnOceanDevice> ref = devices.getServiceReference();
	tlog("ref: " + ref);

	assertThat(lastServiceEvent).as("Event mismatch").isEqualTo(ServiceListener.SERVICE_ADDED);
	assertThat(ref.getProperty(EnOceanDevice.CHIP_ID)).as("CHIP_ID mismatch").isEqualTo(Fixtures.STR_HOST_ID_2);
	assertThat(ref.getProperty(EnOceanDevice.RORG)).as("RORG mismatch").isEqualTo(Fixtures.STR_RORG_RPS);
	assertThat(ref.getProperty(EnOceanDevice.FUNC)).as("ref.getProperty(EnOceanDevice.FUNC) must not be null.").isNull();

	EnOceanDevice dev = context.getService(ref);
	dev.setFunc(Fixtures.FUNC);
	lastServiceEvent = devices.waitForService();
	assertThat(lastServiceEvent).as("Timeout reached.").isNotNull();
	assertThat(lastServiceEvent).as("Event mismatch").isEqualTo(ServiceListener.SERVICE_MODIFIED);

	dev.setType(Fixtures.TYPE_1);
	lastServiceEvent = devices.waitForService();
	assertThat(lastServiceEvent).as("Timeout reached.").isNotNull();
	assertThat(lastServiceEvent).as("Event mismatch").isEqualTo(ServiceListener.SERVICE_MODIFIED);

	assertThat(ref.getProperty(EnOceanDevice.FUNC)).as("FUNC mismatch").isEqualTo(Fixtures.STR_FUNC);
	assertThat(ref.getProperty(EnOceanDevice.TYPE)).as("TYPE mismatch").isEqualTo(Fixtures.STR_TYPE_1);

	tlog("Unget service with service reference: " + ref);
	context.ungetService(ref);
    }

}
