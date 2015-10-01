/*
 * Copyright (C) 2015 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.ui.navigator;

import java.util.List;

import org.titou10.jtb.jms.qm.QManager;

/**
 * Q Manager tree node
 * 
 * @author Denis Forveille
 * 
 */
public class NodeQManager extends NodeAbstract {

   public NodeQManager(QManager qManager) {
      super(qManager, null);
   }

   @Override
   public List<NodeFolder<?>> getChildren() {
      return null;
   }

   @Override
   public Boolean hasChildren() {
      return false;
   }
}
