/*
 * Copyright (C) 2015-2016 Denis Forveille titou10.titou10@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.titou10.jtb.connector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.jms.TextMessage;

import org.eclipse.jface.preference.PreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.connector.transport.Message;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageType;
import org.titou10.jtb.jms.model.JTBQueue;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.model.JTBTopic;

/**
 * Exposes ConfigManager services to connector plugins
 * 
 * @author Denis Forveille
 *
 */
public class ExternalConfigManager {

   private static final Logger log = LoggerFactory.getLogger(ExternalConfigManager.class);

   private ConfigManager       cm;

   // -------------------------------
   // Constructors + basic properties
   // -------------------------------

   public ExternalConfigManager(ConfigManager cm) {
      this.cm = cm;
   }

   public PreferenceStore getPreferenceStore() {
      return cm.getPreferenceStore();
   }

   // ----------------------------
   // Services related to Messages
   // ----------------------------

   public Message getMessage(String sessionName, String queueName) {

      JTBSession jtbSession = cm.getJTBSessionByName(sessionName);
      try {
         if (!(jtbSession.isConnected())) {
            jtbSession.connectOrDisconnect();
         }

         JTBDestination jtbDestination = jtbSession.getJTBDestinationByName(queueName);
         if (jtbDestination == null) {
            return null;
         }
         if (!(jtbDestination instanceof JTBQueue)) {
            return null;
         }
         List<JTBMessage> jtbMessages = jtbSession.browseQueue((JTBQueue) jtbDestination, 1);
         if (jtbMessages.isEmpty()) {
            return null;
         } else {
            javax.jms.Message jmsMessage = jtbMessages.get(0).getJmsMessage();
            Message m = new Message();
            m.setJmsCorrelationID(jmsMessage.getJMSCorrelationID());
            m.setJmsExpiration(jmsMessage.getJMSExpiration());
            m.setJmsPriority(jmsMessage.getJMSPriority());
            m.setJmsType(jmsMessage.getJMSType());
            if (jmsMessage instanceof TextMessage) {
               m.setPayload(((TextMessage) jmsMessage).getText());
            }
            return m;
         }

      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public void postMessage(String sessionName, String destinationName, Message message) {
      log.warn("postMessage");

      JTBSession jtbSession = cm.getJTBSessionByName(sessionName);
      try {
         if (!(jtbSession.isConnected())) {
            jtbSession.connectOrDisconnect();
         }

         JTBDestination jtbDestination = jtbSession.getJTBDestinationByName(destinationName);

         // Reuse connection or connect

         // Create Message
         TextMessage jmsMessage = (TextMessage) jtbSession.createJMSMessage(JTBMessageType.TEXT);
         jmsMessage.setJMSCorrelationID(message.getJmsCorrelationID());
         jmsMessage.setJMSExpiration(message.getJmsExpiration());
         jmsMessage.setJMSPriority(message.getJmsPriority());
         jmsMessage.setJMSType(message.getJmsType());

         jmsMessage.setText(message.getPayload());

         if ((message.getProperties() != null) && (!message.getProperties().isEmpty())) {
            for (Entry<String, String> e : message.getProperties().entrySet()) {
               jmsMessage.setStringProperty(e.getKey(), e.getValue());
            }
         }

         JTBMessage jtbMessage = new JTBMessage(jtbDestination, jmsMessage);

         // Post Message
         jtbSession.sendMessage(jtbMessage);
      } catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

   }

   public void emptyQueue(String sessionName, String destinationName) {

   }

   public List<String> getDestinationNames(String sessionName) {

      List<String> destinations = new ArrayList<>();

      JTBSession jtbSession = cm.getJTBSessionByName(sessionName);
      if (jtbSession == null) {
         log.warn("'{}' does nto exist", sessionName);
         return null;
      }

      try {
         if (!(jtbSession.isConnected())) {
            jtbSession.connectOrDisconnect();
         }

         for (JTBQueue jtbQueue : jtbSession.getJtbQueues()) {
            destinations.add("QUEUE:" + jtbQueue.getName());
         }
         for (JTBTopic jtbTopic : jtbSession.getJtbTopics()) {
            destinations.add("TOPIC:" + jtbTopic.getName());
         }

         return destinations;

      } catch (Exception e) {
         log.error("Exception whene reading destinations of '{}'", sessionName, e);
         return null;
      }

   }

   // Services related to Scripts

}