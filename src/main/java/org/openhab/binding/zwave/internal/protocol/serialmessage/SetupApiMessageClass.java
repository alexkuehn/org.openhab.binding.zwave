/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.zwave.internal.protocol.serialmessage;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveTransactionMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class SetupApiMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(SetupApiMessageClass.class);

    public static final byte SETUPAPI_SUBCMD_QUERY = 0x01;
    public static final byte SETUPAPI_SUBCMD_TXREPORT = 0x02;

    private static final byte SETUPAPI_FLAG_TXREPORT = 0x02;

    private boolean supportTXReport = false;
    private boolean enableTXReport = false;
    private int subcommand;

    public ZWaveSerialPayload doRequestQuery() {
        logger.debug("KPIACQ Request Setup API subcommand Query");

        byte[] payload = { SETUPAPI_SUBCMD_QUERY
        };

        // Create the request
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.SetupApi).withPayload(payload).build();
    }

    public ZWaveSerialPayload doRequestSetTXReport(boolean enable ) {
        logger.debug("ALEX Request Setup API subcommand TX Status report enabling {}", enable);

        byte txreq = 0x00;

        if( enable ) {
            txreq = 0x01;
        }

        byte[] payload = { SETUPAPI_SUBCMD_TXREPORT, txreq
        };

        // Create the request
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.SetupApi).withPayload(payload).build();
    }

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        subcommand = incomingMessage.getMessagePayloadByte(0);

        logger.debug("KPIACQ Got SubCommand Response for subcommand: {}", subcommand );


        switch( subcommand ){
            case SETUPAPI_SUBCMD_QUERY:
                supportTXReport = ((incomingMessage.getMessagePayloadByte(1) & SETUPAPI_FLAG_TXREPORT) != 0) ? true : false;
                break;
            case SETUPAPI_SUBCMD_TXREPORT:
                enableTXReport = (incomingMessage.getMessagePayloadByte(1)  != 0) ? true : false;
                break;
            default:
                logger.error("SetupAPI subcommand unknown response");
                break;
        }           

        transaction.setTransactionComplete();
        return true;
    }

    public boolean getSerialApiTxReportEnabled() {
        return enableTXReport;
    }

    public boolean getSerialApiSupportTxReport() {
        return supportTXReport;
    }

    public int getProcessedSubcommand() {
        return subcommand;
    }
}
